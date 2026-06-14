package com.outthedoor.withsoil.api.diary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "farm_diary_photo",
        indexes = {
                @Index(name = "idx_farm_diary_photo_diary", columnList = "farm_diary_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarmDiaryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_diary_id", nullable = false)
    private FarmDiary diary;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 255)
    private String storedFilename;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    public static FarmDiaryPhoto create(FarmDiary diary, String originalFilename, String storedFilename,
                                        String filePath, String contentType, long fileSize) {
        FarmDiaryPhoto photo = new FarmDiaryPhoto();
        photo.diary = diary;
        photo.originalFilename = originalFilename;
        photo.storedFilename = storedFilename;
        photo.filePath = filePath;
        photo.contentType = contentType;
        photo.fileSize = fileSize;
        return photo;
    }
}
