package com.jachwisunbae;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.jachwisunbae.auth.controller.dto.OAuthLoginRequest;
import com.jachwisunbae.auth.provider.OAuthProviderType;
import com.jachwisunbae.auth.service.AuthService;
import com.jachwisunbae.checklist.service.SystemCheckItemService;
import com.jachwisunbae.checklist.service.UserChecklistService;
import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UpdateUserChecklistCommand;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.member.service.MemberService;
import com.jachwisunbae.property.checklist.service.AppliedChecklistService;
import com.jachwisunbae.property.checklist.type.CheckStatus;
import com.jachwisunbae.property.memo.service.PropertyMemoService;
import com.jachwisunbae.property.memo.service.dto.ReplacePropertyMemoCommand;
import com.jachwisunbae.property.service.PropertyService;
import com.jachwisunbae.property.service.dto.CreatePropertyCommand;
import com.jachwisunbae.property.service.dto.UpdatePropertyCommand;

class ServiceTransactionConventionTest {

    private static final List<Class<?>> SERVICE_TYPES = List.of(
            AuthService.class,
            SystemCheckItemService.class,
            UserChecklistService.class,
            MemberService.class,
            PropertyService.class,
            PropertyMemoService.class,
            AppliedChecklistService.class);

    @Test
    void 모든_서비스는_읽기_전용_트랜잭션을_기본값으로_사용한다() {
        for (Class<?> serviceType : SERVICE_TYPES) {
            Transactional transactional = serviceType.getAnnotation(Transactional.class);

            assertThat(transactional)
                    .as("%s의 클래스 수준 @Transactional", serviceType.getSimpleName())
                    .isNotNull();
            assertThat(transactional.readOnly())
                    .as("%s의 클래스 수준 readOnly", serviceType.getSimpleName())
                    .isTrue();
        }
    }

    @Test
    void 쓰기_서비스_메서드는_읽기_전용_설정을_덮어쓴다() throws NoSuchMethodException {
        assertWriteTransaction(
                AuthService.class, "login", OAuthProviderType.class, OAuthLoginRequest.class);
        assertWriteTransaction(AuthService.class, "rotate", String.class);
        assertWriteTransaction(AuthService.class, "logout", Long.class, String.class);
        assertWriteTransaction(
                UserChecklistService.class, "create", Long.class, CreateUserChecklistCommand.class);
        assertWriteTransaction(UserChecklistService.class, "update", Long.class, Long.class,
                UpdateUserChecklistCommand.class);
        assertWriteTransaction(UserChecklistService.class, "delete", Long.class, Long.class);
        assertWriteTransaction(
                PropertyService.class, "create", Long.class, CreatePropertyCommand.class);
        assertWriteTransaction(PropertyService.class, "update", Long.class, Long.class,
                UpdatePropertyCommand.class);
        assertWriteTransaction(PropertyService.class, "delete", Long.class, Long.class);
        assertWriteTransaction(PropertyMemoService.class, "replace", Long.class, Long.class,
                ReplacePropertyMemoCommand.class);
        assertWriteTransaction(AppliedChecklistService.class, "applyOrReplace",
                Long.class, Long.class, Stage.class, Long.class);
        assertWriteTransaction(AppliedChecklistService.class, "updateStatus",
                Long.class, Long.class, Long.class, Long.class, CheckStatus.class);
        assertWriteTransaction(AppliedChecklistService.class, "updateMemo",
                Long.class, Long.class, Long.class, Long.class, String.class);
    }

    private void assertWriteTransaction(
            Class<?> serviceType,
            String methodName,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = serviceType.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional)
                .as("%s.%s의 @Transactional", serviceType.getSimpleName(), methodName)
                .isNotNull();
        assertThat(transactional.readOnly())
                .as("%s.%s의 readOnly", serviceType.getSimpleName(), methodName)
                .isFalse();
    }
}
