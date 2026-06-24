package com.outthedoor.withsoil.api.diary.service;

import com.outthedoor.withsoil.api.diary.dto.request.FarmDiaryRequest;
import com.outthedoor.withsoil.api.diary.dto.response.FarmDiaryCalendarResponse;
import com.outthedoor.withsoil.api.diary.dto.response.FarmDiaryResponse;
import com.outthedoor.withsoil.api.diary.dto.response.FarmDiarySummaryResponse;
import com.outthedoor.withsoil.api.diary.entity.FarmDiary;
import com.outthedoor.withsoil.api.diary.entity.FarmDiaryPhoto;
import com.outthedoor.withsoil.api.diary.repository.FarmDiaryPhotoRepository;
import com.outthedoor.withsoil.api.diary.repository.FarmDiaryRepository;
import com.outthedoor.withsoil.api.member.entity.Member;
import com.outthedoor.withsoil.global.exeption.BaseException;
import com.outthedoor.withsoil.global.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FarmDiaryService {

    private final FarmDiaryRepository farmDiaryRepository;
    private final FarmDiaryPhotoRepository farmDiaryPhotoRepository;

    @Value("${file.upload.diary-photo-dir:uploads/diary-photos}")
    private String diaryPhotoDir;

    @Transactional
    public FarmDiaryResponse createDiary(Member member, FarmDiaryRequest request, List<MultipartFile> photos) {
        FarmDiary diary = FarmDiary.create(member, request.diaryDateTime(), request.normalizedWorks(), request.memo());
        farmDiaryRepository.save(diary);
        savePhotos(diary, photos);

        return FarmDiaryResponse.of(diary);
    }

    public List<FarmDiarySummaryResponse> getMonthlyDiaries(Member member, YearMonth month) {
        LocalDateTime startDateTime = month.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = month.plusMonths(1).atDay(1).atStartOfDay();

        return farmDiaryRepository
                .findAllByMemberAndDiaryDateTimeGreaterThanEqualAndDiaryDateTimeLessThanAndIsDeletedFalseOrderByDiaryDateTimeDesc(member, startDateTime, endDateTime)
                .stream()
                .map(FarmDiarySummaryResponse::of)
                .toList();
    }

    public List<FarmDiaryCalendarResponse> getMonthlyCalendar(Member member, YearMonth month) {
        LocalDateTime startDateTime = month.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = month.plusMonths(1).atDay(1).atStartOfDay();

        return farmDiaryRepository
                .findAllByMemberAndDiaryDateTimeGreaterThanEqualAndDiaryDateTimeLessThanAndIsDeletedFalseOrderByDiaryDateTimeDesc(member, startDateTime, endDateTime)
                .stream()
                .collect(Collectors.groupingBy(
                        diary -> diary.getDiaryDateTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new FarmDiaryCalendarResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public FarmDiaryResponse getDiary(Member member, Long diaryId) {
        FarmDiary diary = getActiveDiary(member, diaryId);
        return FarmDiaryResponse.of(diary);
    }

    public List<FarmDiaryResponse> getDiariesByDate(Member member, LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();

        return farmDiaryRepository
                .findAllByMemberAndDiaryDateTimeGreaterThanEqualAndDiaryDateTimeLessThanAndIsDeletedFalseOrderByDiaryDateTimeAsc(member, startDateTime, endDateTime)
                .stream()
                .map(FarmDiaryResponse::of)
                .toList();
    }

    @Transactional
    public FarmDiaryResponse updateDiary(Member member, Long diaryId, FarmDiaryRequest request, List<MultipartFile> photos) {
        FarmDiary diary = getActiveDiary(member, diaryId);
        diary.update(request.diaryDateTime(), request.normalizedWorks(), request.memo());
        if (photos != null && !photos.isEmpty()) {
            replacePhotos(diary, photos);
        }

        return FarmDiaryResponse.of(diary);
    }

    @Transactional
    public void deleteDiary(Member member, Long diaryId) {
        FarmDiary diary = getActiveDiary(member, diaryId);
        deletePhotoFiles(farmDiaryPhotoRepository.findAllByDiary(diary));
        farmDiaryPhotoRepository.deleteAllByDiary(diary);
        diary.delete();
    }

    public PhotoFile getPhoto(Member member, Long photoId) {
        FarmDiaryPhoto photo = farmDiaryPhotoRepository.findByIdAndDiary_Member_IdAndDiary_IsDeletedFalse(photoId, member.getId())
                .orElseThrow(() -> new BaseException(ErrorStatus.NOT_FOUND_DIARY_PHOTO));

        try {
            Resource resource = new UrlResource(Paths.get(photo.getFilePath()).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BaseException(ErrorStatus.NOT_FOUND_DIARY_PHOTO);
            }
            return new PhotoFile(resource, MediaType.parseMediaType(photo.getContentType()));
        } catch (MalformedURLException e) {
            throw new BaseException(ErrorStatus.NOT_FOUND_DIARY_PHOTO, e);
        }
    }

    private FarmDiary getActiveDiary(Member member, Long diaryId) {
        return farmDiaryRepository.findByIdAndMemberAndIsDeletedFalse(diaryId, member)
                .orElseThrow(() -> new BaseException(ErrorStatus.NOT_FOUND_DIARY));
    }

    private void savePhotos(FarmDiary diary, List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) {
            return;
        }

        photos.stream()
                .filter(photo -> !photo.isEmpty())
                .forEach(photo -> savePhoto(diary, photo));
    }

    private void savePhoto(FarmDiary diary, MultipartFile photo) {
        validateImage(photo);

        try {
            Path uploadDir = Paths.get(diaryPhotoDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String originalFilename = photo.getOriginalFilename() == null ? "crop-photo" : photo.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;
            Path storedPath = uploadDir.resolve(storedFilename);

            photo.transferTo(storedPath);

            FarmDiaryPhoto diaryPhoto = FarmDiaryPhoto.create(
                    diary,
                    originalFilename,
                    storedFilename,
                    storedPath.toString(),
                    photo.getContentType(),
                    photo.getSize()
            );
            farmDiaryPhotoRepository.save(diaryPhoto);
            diary.getPhotos().add(diaryPhoto);
        } catch (IOException e) {
            throw new BaseException(ErrorStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void replacePhotos(FarmDiary diary, List<MultipartFile> photos) {
        List<FarmDiaryPhoto> savedPhotos = farmDiaryPhotoRepository.findAllByDiary(diary);
        deletePhotoFiles(savedPhotos);
        diary.getPhotos().clear();
        farmDiaryPhotoRepository.deleteAllByDiary(diary);
        savePhotos(diary, photos);
    }

    private void deletePhotoFiles(List<FarmDiaryPhoto> photos) {
        photos.forEach(photo -> {
            try {
                Files.deleteIfExists(Paths.get(photo.getFilePath()));
            } catch (IOException ignored) {
                // DB 정합성을 우선 유지하고, 파일 삭제 실패는 운영 로그/모니터링에서 추적합니다.
            }
        });
    }

    private void validateImage(MultipartFile photo) {
        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BaseException(ErrorStatus.BAD_REQUEST_INVALID_DIARY_PHOTO);
        }
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    public record PhotoFile(Resource resource, MediaType mediaType) {
    }
}
