package com.outthedoor.withsoil.api.ai.dto.response;

import com.outthedoor.withsoil.api.ai.entity.DiseaseGuide;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "병해 진단 가이드 응답")
public record AiDiagnosisGuideResponse(
        @Schema(description = "가이드 대상 작물", example = "토마토")
        String cropName,

        @Schema(description = "진단 병해명", example = "흰가루병")
        String diseaseName,

        @Schema(description = "원본 병해 정보의 병해명", example = "흰가루병")
        String sourceDiseaseName,

        @Schema(description = "정상 진단 여부", example = "false")
        boolean normal,

        @Schema(description = "주요 증상")
        String symptoms,

        @Schema(description = "발생 조건")
        String developmentCondition,

        @Schema(description = "예방 및 방제 방법")
        String preventionMethod,

        @Schema(description = "병원체명", example = "Oidium neolycopersici")
        String pathogenName,

        @Schema(description = "병원체 분류", example = "곰팡이")
        String pathogenGroup,

        @Schema(description = "프론트엔드 표시용 가이드 항목 목록")
        List<AiDiagnosisGuideItemResponse> guideItems
) {

    public static AiDiagnosisGuideResponse from(DiseaseGuide guide) {
        return new AiDiagnosisGuideResponse(
                guide.getCropName(),
                guide.getDiseaseName(),
                guide.getSourceDiseaseName(),
                guide.isNormal(),
                guide.getSymptoms(),
                guide.getDevelopmentCondition(),
                guide.getPreventionMethod(),
                guide.getPathogenName(),
                guide.getPathogenGroup(),
                createGuideItems(guide)
        );
    }

    private static List<AiDiagnosisGuideItemResponse> createGuideItems(DiseaseGuide guide) {
        List<AiDiagnosisGuideItemResponse> items = new ArrayList<>();
        if (guide.isNormal()) {
            addItem(items, "현재 상태", guide.getSymptoms(), 2, 180);
            addItem(items, "관리 팁", guide.getPreventionMethod(), 3, 240);
            addItem(items, "주의할 환경", guide.getDevelopmentCondition(), 2, 200);
            return items;
        }

        addItem(items, "먼저 할 일", guide.getPreventionMethod(), 4, 360);
        addItem(items, "확인할 증상", guide.getSymptoms(), 3, 300);
        addItem(items, "발생하기 쉬운 환경", guide.getDevelopmentCondition(), 3, 300);
        return items;
    }

    private static void addItem(
            List<AiDiagnosisGuideItemResponse> items,
            String title,
            String content,
            int maxLines,
            int maxLength
    ) {
        if (StringUtils.hasText(content)) {
            items.add(new AiDiagnosisGuideItemResponse(title, compact(content, maxLines, maxLength)));
        }
    }

    private static String compact(String content, int maxLines, int maxLength) {
        List<String> lines = content.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .limit(maxLines)
                .toList();

        String compacted = String.join("\n", lines);
        if (compacted.length() <= maxLength) {
            return compacted;
        }
        return compacted.substring(0, maxLength).trim() + "...";
    }
}
