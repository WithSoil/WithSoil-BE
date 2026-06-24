package com.outthedoor.withsoil.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추천 작물 개별 상세 정보 DTO")
public class CropRecommendDetailDto {

    @Schema(description = "작물 명", example = "콩")
    private String cropName;

    @Schema(description = "추천 점수", example = "95.0")
    private double recommendScore;

    @Schema(description = "AI 추천 한줄 제목", example = "왜 나한테 맞을까요?")
    private String aiReasonTitle;

    @Schema(description = "AI 추천 상세 사유", example = "가족과 함께 시골 생활을 시작하며 '가볍게 농사'를 지어보려는 목적에...")
    private String aiReasonDetail;

    @Schema(description = "재배 난이도", example = "쉬움")
    private String difficultyLevel;

    @Schema(description = "적정 온도", example = "20~25℃")
    private String optimalTemp;

    @Schema(description = "토양 산도", example = "6.5 내외")
    private String soilPh;

    @Schema(description = "재배 기간", example = "약 4~5개월")
    private String cultivationPeriod;

    @Schema(description = "주요 농작업 목록", example = "[\"파종\", \"북주기\", \"적심\", \"물관리\", \"수확\"]")
    private List<String> mainTasks;

    @Schema(description = "주요 위험 요소 목록", example = "[\"가뭄\", \"침수\", \"습해\", \"불마름병\", \"노린재류\"]")
    private List<String> mainRisks;
}