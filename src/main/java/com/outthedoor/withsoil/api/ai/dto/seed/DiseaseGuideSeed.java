package com.outthedoor.withsoil.api.ai.dto.seed;

public record DiseaseGuideSeed(
        String cropName,
        String cropNameNormalized,
        String diseaseName,
        String diseaseNameNormalized,
        String sourceDiseaseName,
        boolean normal,
        String symptoms,
        String developmentCondition,
        String preventionMethod,
        String pathogenName,
        String pathogenGroup
) {
}
