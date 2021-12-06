package com.example.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/28
 */
@Repository
public interface OperationRelationShipRepository extends Neo4jRepository<OperationRelationShip, Long> {

}
