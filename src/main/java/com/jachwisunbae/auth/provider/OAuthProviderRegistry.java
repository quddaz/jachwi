package com.jachwisunbae.auth.provider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jachwisunbae.common.exception.BusinessException;
import com.jachwisunbae.common.exception.DomainErrorCode;

@Component
public class OAuthProviderRegistry {

    private final Map<OAuthProviderType, OAuthProvider> providers = new EnumMap<>(OAuthProviderType.class);

    public OAuthProviderRegistry(List<OAuthProvider> providers) {
        providers.forEach(provider -> this.providers.put(provider.type(), provider));
    }

    public OAuthProvider get(OAuthProviderType type) {
        OAuthProvider provider = providers.get(type);
        if (provider == null) {
            throw new BusinessException(DomainErrorCode.OAUTH_PROVIDER_UNSUPPORTED,
                    "등록되지 않은 OAuth 공급자입니다.");
        }
        return provider;
    }
}
