package com.outthedoor.withsoil.api.ai.entity;

import com.outthedoor.withsoil.api.ai.dto.seed.DiseaseGuideSeed;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "disease_guide",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disease_guide_crop_disease",
                        columnNames = {"crop_name_normalized", "disease_name_normalized"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_disease_guide_crop_disease",
                        columnList = "crop_name_normalized, disease_name_normalized"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiseaseGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String cropName;

    @Column(name = "crop_name_normalized", nullable = false, length = 50)
    private String cropNameNormalized;

    @Column(nullable = false, length = 100)
    private String diseaseName;

    @Column(name = "disease_name_normalized", nullable = false, length = 100)
    private String diseaseNameNormalized;

    @Column(length = 100)
    private String sourceDiseaseName;

    @Column(nullable = false)
    private boolean normal;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String developmentCondition;

    @Column(columnDefinition = "TEXT")
    private String preventionMethod;

    @Column(columnDefinition = "TEXT")
    private String pathogenName;

    @Column(length = 50)
    private String pathogenGroup;

    public static DiseaseGuide create(DiseaseGuideSeed seed) {
        DiseaseGuide guide = new DiseaseGuide();
        guide.cropName = seed.cropName();
        guide.cropNameNormalized = resolveNormalized(seed.cropNameNormalized(), seed.cropName());
        guide.diseaseName = seed.diseaseName();
        guide.diseaseNameNormalized = resolveNormalized(seed.diseaseNameNormalized(), seed.diseaseName());
        guide.updateFrom(seed);
        return guide;
    }

    public void updateFrom(DiseaseGuideSeed seed) {
        this.sourceDiseaseName = seed.sourceDiseaseName();
        this.normal = seed.normal();
        this.symptoms = polishDescription(seed.symptoms());
        this.developmentCondition = polishDescription(seed.developmentCondition());
        this.preventionMethod = polishActionText(seed.preventionMethod());
        this.pathogenName = seed.pathogenName();
        this.pathogenGroup = seed.pathogenGroup();
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^가-힣A-Za-z0-9]", "")
                .toLowerCase();
    }

    public static String resolveNormalized(String normalizedValue, String originalValue) {
        if (normalizedValue == null || normalizedValue.isBlank()) {
            return normalize(originalValue);
        }
        return normalizedValue;
    }

    private static String polishDescription(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(DiseaseGuide::softenDescriptionLine)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private static String polishActionText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (String rawLine : value.lines().toList()) {
            String line = cleanActionPrefix(rawLine);
            if (line.isBlank()) {
                continue;
            }

            if (!lines.isEmpty() && shouldMergeWithPrevious(lines.getLast())) {
                int lastIndex = lines.size() - 1;
                lines.set(lastIndex, lines.getLast() + " " + line);
            } else {
                lines.add(line);
            }
        }

        return lines.stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(DiseaseGuide::polishActionLine)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private static String polishActionLine(String line) {
        String content = cleanActionPrefix(line);
        if (content.isBlank()) {
            return "";
        }
        if (isActionHeading(content)) {
            return content;
        }
        return "• " + softenActionLine(content);
    }

    private static String softenDescriptionLine(String line) {
        line = replaceAwkwardPhrase(line);
        if (line.endsWith("습니다.") || line.endsWith("니다.") || line.endsWith("해요.") || line.endsWith("요.") || line.endsWith("주세요.")) {
            return line;
        }
        if (line.endsWith("뒤 덮힌다.") || line.endsWith("뒤 덮힌다")) {
            return line.replaceFirst("뒤 덮힌다\\.?$", "뒤덮여요.");
        }
        if (line.endsWith("뒤덮힌다.") || line.endsWith("뒤덮힌다")) {
            return line.replaceFirst("뒤덮힌다\\.?$", "뒤덮여요.");
        }
        if (line.endsWith("발병시킨다.") || line.endsWith("발병시킨다")) {
            return line.replaceFirst("발병시킨다\\.?$", "발병시켜요.");
        }
        if (line.endsWith("밝혀지지 않았다.") || line.endsWith("밝혀지지 않았다")) {
            return line.replaceFirst("밝혀지지 않았다\\.?$", "밝혀지지 않았어요.");
        }
        if (line.endsWith("문제되지 않았다.") || line.endsWith("문제되지 않았다")) {
            return line.replaceFirst("문제되지 않았다\\.?$", "문제되지 않았어요.");
        }
        if (line.endsWith("되었다.") || line.endsWith("되었다")) {
            return line.replaceFirst("되었다\\.?$", "됐어요.");
        }
        if (line.endsWith("하였다.") || line.endsWith("하였다")) {
            return line.replaceFirst("하였다\\.?$", "했어요.");
        }
        if (line.endsWith("입었다.") || line.endsWith("입었다")) {
            return line.replaceFirst("입었다\\.?$", "입었어요.");
        }
        if (line.endsWith("된다.") || line.endsWith("된다")) {
            return line.replaceFirst("된다\\.?$", "될 수 있어요.");
        }
        if (line.endsWith("발생한다.") || line.endsWith("발생한다")) {
            return line.replaceFirst("발생한다\\.?$", "발생할 수 있어요.");
        }
        if (line.endsWith("나타난다.") || line.endsWith("나타난다")) {
            return line.replaceFirst("나타난다\\.?$", "나타날 수 있어요.");
        }
        if (line.endsWith("마른다.") || line.endsWith("마른다")) {
            return line.replaceFirst("마른다\\.?$", "마를 수 있어요.");
        }
        if (line.endsWith("한다.") || line.endsWith("한다")) {
            return line.replaceFirst("한다\\.?$", "합니다.");
        }
        if (line.endsWith("하다.") || line.endsWith("하다")) {
            return line.replaceFirst("하다\\.?$", "해요.");
        }
        if (line.endsWith("다르다.") || line.endsWith("다르다")) {
            return line.replaceFirst("다르다\\.?$", "달라요.");
        }
        if (line.endsWith("유사하다.") || line.endsWith("유사하다")) {
            return line.replaceFirst("유사하다\\.?$", "유사해요.");
        }
        if (line.endsWith("불분명하다.") || line.endsWith("불분명하다")) {
            return line.replaceFirst("불분명하다\\.?$", "불분명해요.");
        }
        if (line.endsWith("용이하다.") || line.endsWith("용이하다")) {
            return line.replaceFirst("용이하다\\.?$", "용이해요.");
        }
        if (line.endsWith("약하다.") || line.endsWith("약하다")) {
            return line.replaceFirst("약하다\\.?$", "약해요.");
        }
        if (line.endsWith("경미하다.") || line.endsWith("경미하다")) {
            return line.replaceFirst("경미하다\\.?$", "경미해요.");
        }
        return line;
    }

    private static String softenActionLine(String line) {
        line = replaceAwkwardPhrase(line);
        if (line.endsWith("습니다.") || line.endsWith("니다.") || line.endsWith("해요.") || line.endsWith("요.") || line.endsWith("주세요.")) {
            return line;
        }
        if (line.endsWith("하다.") || line.endsWith("하다")) {
            return line.replaceFirst("하다\\.?$", "해주세요.");
        }
        if (line.endsWith("못하다.") || line.endsWith("못하다")) {
            return line.replaceFirst("못하다\\.?$", "못해요.");
        }
        if (line.endsWith("바람직하다.") || line.endsWith("바람직하다")) {
            return line.replaceFirst("바람직하다\\.?$", "바람직해요.");
        }
        if (line.endsWith("높다.") || line.endsWith("높다")) {
            return line.replaceFirst("높다\\.?$", "높아요.");
        }
        if (line.endsWith("발생한다.") || line.endsWith("발생한다")) {
            return line.replaceFirst("발생한다\\.?$", "발생할 수 있어요.");
        }
        if (line.endsWith("한다.")) {
            return line.substring(0, line.length() - 3) + "해주세요.";
        }
        if (line.endsWith("한다")) {
            return line.substring(0, line.length() - 2) + "해주세요.";
        }
        return line;
    }

    private static String cleanActionPrefix(String line) {
        return line.replaceFirst("^[•\\-o]\\s*", "").trim();
    }

    private static boolean shouldMergeWithPrevious(String previousLine) {
        if (isActionHeading(previousLine)) {
            return false;
        }
        return !previousLine.matches(".*(요\\.|다\\.|니다\\.|습니다\\.|세요\\.)$");
    }

    private static boolean isActionHeading(String line) {
        return line.endsWith("방제") || line.endsWith("기타사항") || line.endsWith("기타 관리 사항");
    }

    private static String replaceAwkwardPhrase(String line) {
        return line.replace("공기전염 해요", "공기 전염돼요")
                .replace("공기전염되어", "공기 전염되어")
                .replace("오염물 제거해요", "오염물을 제거해요")
                .replace("잘 해야 해요", "잘해야 해요")
                .replace("하여야 해요", "해야 해요")
                .replace("사용방법에 따라", "사용 방법에 따라")
                .replace("등록약제", "등록 약제")
                .replace("뒤 덮힌다", "뒤덮힌다");
    }
}
