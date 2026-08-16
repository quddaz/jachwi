package com.jachwisunbae.auth.service;

import com.jachwisunbae.auth.controller.dto.LoginResponse;
import com.jachwisunbae.auth.controller.dto.OAuthLoginRequest;
import com.jachwisunbae.auth.provider.OAuthProviderRegistry;
import com.jachwisunbae.auth.provider.OAuthProviderType;
import com.jachwisunbae.auth.token.JwtTokenProvider;
import com.jachwisunbae.member.entity.Member;
import com.jachwisunbae.member.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var profile = providerRegistry.get(providerType).authenticate(request.toCommand());
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = memberRepository.findBySubject(profile.subject()).map(existing -> {
            existing.updateLoginProfile(profile.email(), profile.name(), now);
            return memberRepository.save(existing);
        }).orElseGet(() -> memberRepository.save(Member.create(
                profile.subject(), profile.email(), profile.name(), now)));
        return new LoginResponse(jwtProvider.createAccessToken(member.getId()), "Bearer", 3600,
                new LoginResponse.MemberResponse(member.getId(), member.getName(), member.getEmail()));
    }
}
