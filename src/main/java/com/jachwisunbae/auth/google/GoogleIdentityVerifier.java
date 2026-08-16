package com.jachwisunbae.auth.google;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Component
public class GoogleIdentityVerifier {
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityVerifier(@Value("${auth.google.client-id}") String clientId) throws Exception {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId)).build();
    }

    public GoogleProfile verify(String rawToken, String nonce) {
        try {
            var token = verifier.verify(rawToken);
            var payload = token == null ? null : token.getPayload();
            if (payload == null || !Boolean.TRUE.equals(payload.getEmailVerified())
                    || !nonce.equals(payload.get("nonce"))) {
                throw new IllegalArgumentException();
            }
            return new GoogleProfile(payload.getSubject(), payload.getEmail(), (String) payload.get("name"));
        } catch (Exception exception) {
            throw new BusinessException(DomainErrorCode.GOOGLE_IDENTITY_INVALID,
                    "Google ID Token 검증에 실패했습니다.");
        }
    }
}
