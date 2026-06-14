package com.outthedoor.withsoil.api.diary.repository;

import com.outthedoor.withsoil.api.diary.entity.FarmDiary;
import com.outthedoor.withsoil.api.diary.entity.FarmDiaryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmDiaryPhotoRepository extends JpaRepository<FarmDiaryPhoto, Long> {

    List<FarmDiaryPhoto> findAllByDiary(FarmDiary diary);

    Optional<FarmDiaryPhoto> findByIdAndDiary_Member_IdAndDiary_IsDeletedFalse(Long id, Long memberId);

    void deleteAllByDiary(FarmDiary diary);
}
