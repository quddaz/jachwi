package com.jachwisunbae.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jachwisunbae.checklist.entity.SystemCheckItem;
import com.jachwisunbae.checklist.entity.UserChecklist;
import com.jachwisunbae.checklist.repository.SystemCheckItemRepository;
import com.jachwisunbae.checklist.repository.UserChecklistRepository;
import com.jachwisunbae.checklist.service.dto.CreateUserChecklistCommand;
import com.jachwisunbae.checklist.service.dto.UserChecklistDetailResult;
import com.jachwisunbae.checklist.type.ItemType;
import com.jachwisunbae.checklist.type.Stage;
import com.jachwisunbae.member.entity.Member;
import com.jachwisunbae.member.repository.MemberRepository;

class UserChecklistServiceTest {

    private final UserChecklistRepository checklistRepository = mock(UserChecklistRepository.class);
    private final SystemCheckItemRepository systemItemRepository = mock(SystemCheckItemRepository.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final UserChecklistService service = new UserChecklistService(
            checklistRepository,
            systemItemRepository,
            memberRepository);

    @Test
    void createAutomaticallyIncludesMissingActiveCoreItems() {
        Member member = Member.restore(1L, "subject", "user@example.com", "회원", java.time.LocalDateTime.now());
        SystemCheckItem core = SystemCheckItem.restore(
                10L, Stage.ON_SITE, ItemType.CORE, "핵심 질문", null, true);
        SystemCheckItem optional = SystemCheckItem.restore(
                20L, Stage.ON_SITE, ItemType.OPTIONAL, "선택 질문", null, true);
        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(systemItemRepository.findActiveCoreByStage(Stage.ON_SITE)).thenReturn(List.of(core));
        when(systemItemRepository.findAllByIds(List.of(20L))).thenReturn(List.of(optional));
        when(checklistRepository.save(org.mockito.ArgumentMatchers.any(UserChecklist.class)))
                .thenAnswer(invocation -> {
                    UserChecklist checklist = invocation.getArgument(0);
                    return UserChecklist.restore(
                            100L,
                            checklist.getMemberId(),
                            checklist.getName(),
                            checklist.getStage(),
                            null);
                });
        when(checklistRepository.findItems(100L)).thenReturn(List.of());

        UserChecklistDetailResult result = service.create(
                1L,
                new CreateUserChecklistCommand("  내 체크  ", Stage.ON_SITE, List.of(20L)));

        assertThat(result.checklistId()).isEqualTo(100L);
        verify(checklistRepository).replaceItems(100L, List.of(10L, 20L));
    }
}
