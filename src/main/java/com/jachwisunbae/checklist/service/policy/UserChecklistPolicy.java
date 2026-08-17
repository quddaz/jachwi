package com.jachwisunbae.checklist.service.policy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jachwisunbae.checklist.entity.SystemCheckItem;

@Component
public class UserChecklistPolicy {

    public List<Long> resolveCreateItemIds(
            List<Long> requestedIds,
            List<SystemCheckItem> activeCoreItems) {
        // 생성 시 CORE를 먼저 배치하고, 사용자가 고른 선택 항목의 상대 순서는 그대로 유지한다.
        List<Long> finalIds = new ArrayList<>();
        activeCoreItems.stream().map(SystemCheckItem::getId).forEach(finalIds::add);
        requestedIds.stream().filter(id -> !finalIds.contains(id)).forEach(finalIds::add);
        return finalIds;
    }
}
