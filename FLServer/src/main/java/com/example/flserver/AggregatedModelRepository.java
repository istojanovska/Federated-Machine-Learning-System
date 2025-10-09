package com.example.flserver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregatedModelRepository extends JpaRepository<AggregatedModel, Long> {
    AggregatedModel findTopByOrderByTimestampDesc();
    AggregatedModel findTopByModelTypeOrderByRoundDesc(ModelType modelType);

    AggregatedModel findTopByModelTypeOrderByTimestampDesc(ModelType modelType);

}
