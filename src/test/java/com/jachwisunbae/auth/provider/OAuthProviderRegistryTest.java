package com.jachwisunbae.auth.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.common.exception.BusinessException;

class OAuthProviderRegistryTest {

    @Test
    void returnsStrategyMatchingProviderType() {
        OAuthProvider google = mock(OAuthProvider.class);
        when(google.type()).thenReturn(OAuthProviderType.GOOGLE);
        OAuthProviderRegistry registry = new OAuthProviderRegistry(List.of(google));

        assertThat(registry.get(OAuthProviderType.GOOGLE)).isSameAs(google);
    }

    @Test
    void rejectsProviderWithoutRegisteredStrategy() {
        OAuthProviderRegistry registry = new OAuthProviderRegistry(List.of());

        assertThatThrownBy(() -> registry.get(OAuthProviderType.GOOGLE))
                .isInstanceOf(BusinessException.class);
    }
}
