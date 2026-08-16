package com.jachwisunbae.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jachwisunbae.auth.controller.dto.GoogleLoginRequest;
import com.jachwisunbae.auth.controller.dto.LoginResponse;
import com.jachwisunbae.auth.google.GoogleIdentityVerifier;
import com.jachwisunbae.auth.google.GoogleOAuthClient;
import com.jachwisunbae.auth.token.JwtTokenProvider;
import com.jachwisunbae.member.entity.Member;
import com.jachwisunbae.member.repository.MemberRepository;

@Service
public class AuthService {
    private final GoogleOAuthClient googleClient; private final GoogleIdentityVerifier identityVerifier;
    private final MemberRepository memberRepository; private final JwtTokenProvider jwtProvider; private final Clock clock;
    public AuthService(GoogleOAuthClient googleClient, GoogleIdentityVerifier identityVerifier,
            MemberRepository memberRepository, JwtTokenProvider jwtProvider, Clock clock) {
        this.googleClient=googleClient; this.identityVerifier=identityVerifier; this.memberRepository=memberRepository;
        this.jwtProvider=jwtProvider; this.clock=clock;
    }

    @Transactional
    public LoginResponse login(GoogleLoginRequest request) {
        var tokens = googleClient.exchange(request.authorizationCode(), request.codeVerifier(), request.redirectUri());
        var profile = identityVerifier.verify(tokens.idToken(), request.nonce());
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = memberRepository.findBySubject(profile.subject()).map(existing -> {
            existing.updateLoginProfile(profile.email(), profile.name(), now); return memberRepository.save(existing);
        }).orElseGet(() -> memberRepository.save(Member.create(
                profile.subject(), profile.email(), profile.name(), now)));
        return new LoginResponse(jwtProvider.createAccessToken(member.getId()), "Bearer", 3600,
                new LoginResponse.MemberResponse(member.getId(), member.getName(), member.getEmail()));
    }
}
