package com.outthedoor.withsoil.api.ai.repository;

import com.outthedoor.withsoil.api.ai.entity.DiseaseGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseGuideRepository extends JpaRepository<DiseaseGuide, Long> {

    Optional<DiseaseGuide> findByCropNameNormalizedAndDiseaseNameNormalized(
            String cropNameNormalized,
            String diseaseNameNormalized
    );
}
