package com.jachwisunbae.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.auth.controller.dto.LoginResponse;
import com.jachwisunbae.auth.controller.dto.OAuthLoginRequest;
import com.jachwisunbae.auth.provider.OAuthProfile;
import com.jachwisunbae.auth.provider.OAuthProviderRegistry;
import com.jachwisunbae.auth.provider.OAuthProviderType;
import com.jachwisunbae.auth.token.JwtTokenProvider;
import com.jachwisunbae.member.entity.Member;
import com.jachwisunbae.member.repository.MemberRepository;

@Service
public class AuthService {
    private final OAuthProviderRegistry providerRegistry;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtProvider;
    private final Clock clock;

    public AuthService(
            OAuthProviderRegistry providerRegistry,
            MemberRepository memberRepository,
            JwtTokenProvider jwtProvider,
            Clock clock) {
        this.providerRegistry = providerRegistry;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.clock = clock;
    }

    @Transactional
    public LoginResponse login(OAuthProviderType providerType, OAuthLoginRequest request) {
        OAuthProfile profile = authenticate(providerType, request);
        Member member = findOrCreateMember(profile);
        return createLoginResponse(member);
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
        return new LoginResponse(
                jwtProvider.createAccessToken(member.getId()),
                "Bearer",
                3600,
                new LoginResponse.MemberResponse(
                        member.getId(),
                        member.getName(),
                        member.getEmail()));
    }
}
