package com.jachwisunbae.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.auth.controller.dto.LoginResponse;
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
        OAuthProfile profile = authenticate(providerType, request);
        Member member = findOrCreateMember(profile);
        return createLoginResponse(member);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public TokenResponse rotate(String rawRefreshToken) {
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
        return memberRepository.findBySubject(profile.subject())
                .map(member -> updateMember(member, profile, now))
                .orElseGet(() -> createMember(profile, now));
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
                new LoginResponse.MemberResponse(
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
        return refreshTokenRepository.findByHashForUpdate(refreshTokenHasher.hash(rawRefreshToken))
                .orElseThrow(() -> new BusinessException(
                        DomainErrorCode.REFRESH_TOKEN_INVALID,
                        "등록되지 않은 Refresh Token입니다."));
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
