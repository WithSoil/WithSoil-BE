package com.outthedoor.withsoil.api.diary.repository;

import com.outthedoor.withsoil.api.diary.entity.FarmDiary;
import com.outthedoor.withsoil.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FarmDiaryRepository extends JpaRepository<FarmDiary, Long> {

    Optional<FarmDiary> findByIdAndMemberAndIsDeletedFalse(Long id, Member member);

    List<FarmDiary> findAllByMemberAndDiaryDateTimeGreaterThanEqualAndDiaryDateTimeLessThanAndIsDeletedFalseOrderByDiaryDateTimeDesc(
            Member member,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
