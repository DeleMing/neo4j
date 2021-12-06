package com.example.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/28
 */
@Repository
public interface FlinkJobRepository extends Neo4jRepository<FlinkJob, Long> {

    FlinkJob findByName(String name);
}
