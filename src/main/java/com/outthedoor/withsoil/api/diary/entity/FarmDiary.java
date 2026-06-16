package com.outthedoor.withsoil.api.diary.entity;

import com.outthedoor.withsoil.api.member.entity.Member;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "farm_diary",
        indexes = {
                @Index(name = "idx_farm_diary_member_datetime", columnList = "member_id, diary_datetime"),
                @Index(name = "idx_farm_diary_datetime", columnList = "diary_datetime")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarmDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "diary_datetime", nullable = false)
    private LocalDateTime diaryDateTime;

    @ElementCollection
    @CollectionTable(name = "farm_diary_work", joinColumns = @JoinColumn(name = "farm_diary_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "work", nullable = false, length = 50)
    private List<String> works = new ArrayList<>();

    @Column(length = 1000)
    private String memo;

    @OneToMany(mappedBy = "diary", orphanRemoval = true)
    private List<FarmDiaryPhoto> photos = new ArrayList<>();

    @Column(nullable = false)
    private boolean isDeleted;

    private LocalDateTime deletedAt;

    public static FarmDiary create(Member member, LocalDateTime diaryDateTime, List<String> works, String memo) {
        FarmDiary diary = new FarmDiary();
        diary.member = member;
        diary.diaryDateTime = diaryDateTime;
        diary.works = new ArrayList<>(works);
        diary.memo = memo;
        diary.isDeleted = false;
        return diary;
    }

    public void update(LocalDateTime diaryDateTime, List<String> works, String memo) {
        this.diaryDateTime = diaryDateTime;
        this.works.clear();
        this.works.addAll(works);
        this.memo = memo;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isOwner(Member member) {
        return this.member.getId().equals(member.getId());
    }
}
