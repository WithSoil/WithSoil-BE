package com.outthedoor.withsoil.api.member.repository;

import com.outthedoor.withsoil.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndIsDeleted(String email, boolean isDeleted);

    Optional<Member> findByIdAndIsDeleted(Long id, boolean isDeleted);

    boolean existsByEmailAndIsDeleted(String email, boolean isDeleted);

    List<Member> findAllByIsDeletedFalseAndPushTokenIsNotNull();
}
