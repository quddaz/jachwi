package com.jachwisunbae.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.auth.controller.dto.LoginResponse;
import com.jachwisunbae.auth.controller.dto.LoginMemberResponse;
import com.jachwisunbae.auth.controller.dto.OAuthLoginRequest;
import com.jachwisunbae.auth.controller.dto.TokenResponse;
import com.jachwisunbae.auth.provider.OAuthProfile;
import com.jachwisunbae.auth.provider.OAuthProviderRegistry;
import com.jachwisunbae.auth.provider.OAuthProviderType;
import com.jachwisunbae.auth.repository.RefreshTokenRepository;
import com.jachwisunbae.auth.token.JwtTokenProvider;
import com.jachwisunbae.auth.token.RefreshToken;
import com.jachwisunbae.auth.token.RefreshTokenGenerator;
import com.jachwisunbae.auth.token.RefreshTokenHasher;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;
import com.jachwisunbae.member.entity.Member;
import com.jachwisunbae.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final OAuthProviderRegistry providerRegistry;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final Clock clock;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;

    public AuthService(
            OAuthProviderRegistry providerRegistry,
            MemberRepository memberRepository,
            JwtTokenProvider jwtProvider,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            Clock clock,
            @Value("${auth.jwt.access-token-seconds}") long accessTokenSeconds,
            @Value("${auth.jwt.refresh-token-seconds}") long refreshTokenSeconds) {
        this.providerRegistry = providerRegistry;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenHasher = refreshTokenHasher;
        this.clock = clock;
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    @Transactional
    public LoginResponse login(OAuthProviderType providerType, OAuthLoginRequest request) {
        // 공급자 인증을 먼저 완료한 뒤 subject 기준으로 회원을 생성하거나 로그인 정보를 갱신한다.
        // 회원 저장과 Refresh Token 발급은 같은 트랜잭션에서 처리해 불완전한 로그인을 남기지 않는다.
        OAuthProfile profile = authenticate(providerType, request);
        Member member = findOrCreateMember(profile);
        return createLoginResponse(member);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public TokenResponse rotate(String rawRefreshToken) {
        // 토큰 행을 잠금 조회해 동시 회전을 직렬화하고, 기존 토큰 폐기 후 새 토큰 쌍을 발급한다.
        // 폐기 토큰 재사용 시 발생하는 BusinessException은 롤백하지 않아 전체 활성 토큰 폐기를 유지한다.
        LocalDateTime now = LocalDateTime.now(clock);
        RefreshToken current = findRefreshToken(rawRefreshToken);
        validateRotatable(current, now);
        refreshTokenRepository.revoke(current.getId(), now);
        return issueTokenPair(current.getMemberId(), now);
    }

    @Transactional
    public void logout(Long memberId, String rawRefreshToken) {
        RefreshToken token = findRefreshToken(rawRefreshToken);
        if (!token.getMemberId().equals(memberId) || token.isRevoked()) {
            throw new BusinessException(
                    DomainErrorCode.REFRESH_TOKEN_INVALID,
                    "로그아웃할 Refresh Token이 올바르지 않습니다.");
        }
        refreshTokenRepository.revoke(token.getId(), LocalDateTime.now(clock));
    }

    private OAuthProfile authenticate(OAuthProviderType providerType, OAuthLoginRequest request) {
        return providerRegistry.get(providerType).authenticate(request.toCommand());
    }

    private Member findOrCreateMember(OAuthProfile profile) {
        LocalDateTime now = LocalDateTime.now(clock);
        Optional<Member> existingMember = memberRepository.findBySubject(profile.subject());
        if (existingMember.isPresent()) {
            return updateMember(existingMember.get(), profile, now);
        }
        return createMember(profile, now);
    }

    private Member updateMember(Member member, OAuthProfile profile, LocalDateTime loginAt) {
        member.updateLoginProfile(profile.email(), profile.name(), loginAt);
        return memberRepository.save(member);
    }

    private Member createMember(OAuthProfile profile, LocalDateTime loginAt) {
        return memberRepository.save(Member.create(
                profile.subject(),
                profile.email(),
                profile.name(),
                loginAt));
    }

    private LoginResponse createLoginResponse(Member member) {
        TokenResponse tokens = issueTokenPair(member.getId(), LocalDateTime.now(clock));
        return new LoginResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.tokenType(),
                tokens.expiresIn(),
                new LoginMemberResponse(
                        member.getId(),
                        member.getName(),
                        member.getEmail()));
    }

    private RefreshToken findRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException(
                    DomainErrorCode.REFRESH_TOKEN_INVALID,
                    "Refresh Token이 비어 있습니다.");
        }
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByHashForUpdate(
                refreshTokenHasher.hash(rawRefreshToken));
        if (refreshToken.isEmpty()) {
            throw new BusinessException(
                    DomainErrorCode.REFRESH_TOKEN_INVALID,
                    "등록되지 않은 Refresh Token입니다.");
        }
        return refreshToken.get();
    }

    private void validateRotatable(RefreshToken token, LocalDateTime now) {
        if (token.isRevoked()) {
            refreshTokenRepository.revokeAllActiveByMemberId(token.getMemberId(), now);
            throw new BusinessException(
                    DomainErrorCode.REFRESH_TOKEN_REUSED,
                    "폐기된 Refresh Token이 재사용되었습니다.");
        }
        if (token.isExpired(now)) {
            throw new BusinessException(
                    DomainErrorCode.REFRESH_TOKEN_EXPIRED,
                    "Refresh Token이 만료되었습니다.");
        }
    }

    private TokenResponse issueTokenPair(Long memberId, LocalDateTime now) {
        String rawRefreshToken = refreshTokenGenerator.generate();
        refreshTokenRepository.save(RefreshToken.issue(
                memberId,
                refreshTokenHasher.hash(rawRefreshToken),
                now.plusSeconds(refreshTokenSeconds)));
        return new TokenResponse(
                jwtProvider.createAccessToken(memberId),
                rawRefreshToken,
                "Bearer",
                accessTokenSeconds);
    }
}
