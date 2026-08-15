package com.jachwisunbae.member.repository;

import java.util.Optional;

import com.jachwisunbae.member.entity.Member;

public interface MemberRepository {

    Optional<Member> findById(Long id);
}
