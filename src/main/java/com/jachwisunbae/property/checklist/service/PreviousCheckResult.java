package com.jachwisunbae.property.checklist.service;

import com.jachwisunbae.property.checklist.type.CheckStatus;

record PreviousCheckResult(CheckStatus status, String memo) {
}
