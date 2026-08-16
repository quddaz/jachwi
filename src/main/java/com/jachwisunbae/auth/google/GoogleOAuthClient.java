package com.jachwisunbae.auth.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Component
public class GoogleOAuthClient {
    private final RestClient restClient = RestClient.create("https://oauth2.googleapis.com");
    private final String clientId;
    private final String clientSecret;

    public GoogleOAuthClient(@Value("${auth.google.client-id}") String clientId,
            @Value("${auth.google.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public GoogleTokenResponse exchange(String code, String verifier, String redirectUri) {
        try {
            var form = new LinkedMultiValueMap<String, String>();
            form.add("code", code); form.add("client_id", clientId); form.add("client_secret", clientSecret);
            form.add("code_verifier", verifier); form.add("redirect_uri", redirectUri);
            form.add("grant_type", "authorization_code");
            return restClient.post().uri("/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(GoogleTokenResponse.class);
        } catch (Exception exception) {
            throw new BusinessException(DomainErrorCode.GOOGLE_AUTHENTICATION_FAILED,
                    "Google authorization code 교환에 실패했습니다.");
        }
    }
}
