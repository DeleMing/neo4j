package com.example.neo4j.demo.relationship;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/27
 */
@Repository
public interface DemoRelationShipRepository extends Neo4jRepository<DemoRelationShip, Long> {
}
