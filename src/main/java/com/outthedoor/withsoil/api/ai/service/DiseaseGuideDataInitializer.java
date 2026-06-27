package com.outthedoor.withsoil.api.ai.service;

import com.outthedoor.withsoil.api.ai.dto.seed.DiseaseGuideSeed;
import com.outthedoor.withsoil.api.ai.entity.DiseaseGuide;
import com.outthedoor.withsoil.api.ai.repository.DiseaseGuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiseaseGuideDataInitializer implements ApplicationRunner {

    private static final String SEED_RESOURCE_PATH = "data/disease-guide.json";
    private static final List<String> LEGACY_UNUSED_COLUMNS = List.of(
            "source_crop_name",
            "source_disease_key",
            "biology_prevention_method",
            "chemical_prevention_method",
            "image_url"
    );

    private final DiseaseGuideRepository diseaseGuideRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE_PATH);
        DiseaseGuideSeed[] seeds = objectMapper.readValue(resource.getInputStream(), DiseaseGuideSeed[].class);

        List<DiseaseGuide> guides = new ArrayList<>();
        int updatedCount = 0;
        for (DiseaseGuideSeed seed : seeds) {
            String cropNameNormalized = DiseaseGuide.resolveNormalized(seed.cropNameNormalized(), seed.cropName());
            String diseaseNameNormalized = DiseaseGuide.resolveNormalized(seed.diseaseNameNormalized(), seed.diseaseName());
            DiseaseGuide guide = diseaseGuideRepository.findByCropNameNormalizedAndDiseaseNameNormalized(
                    cropNameNormalized,
                    diseaseNameNormalized
            ).orElse(null);

            if (guide == null) {
                guides.add(DiseaseGuide.create(seed));
            } else {
                guide.updateFrom(seed);
                updatedCount++;
            }
        }

        if (!guides.isEmpty()) {
            diseaseGuideRepository.saveAll(guides);
        }
        dropLegacyUnusedColumns();
        log.info("Disease guide seed data initialized. inserted={}, updated={}", guides.size(), updatedCount);
    }

    private void dropLegacyUnusedColumns() {
        for (String column : LEGACY_UNUSED_COLUMNS) {
            if (!existsColumn(column)) {
                continue;
            }

            try {
                jdbcTemplate.execute("ALTER TABLE disease_guide DROP COLUMN " + column);
                log.info("Dropped unused disease_guide column. column={}", column);
            } catch (RuntimeException e) {
                log.warn("Failed to drop unused disease_guide column. column={}", column, e);
            }
        }
    }

    private boolean existsColumn(String column) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'disease_guide'
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                column
        );
        return count != null && count > 0;
    }
}
