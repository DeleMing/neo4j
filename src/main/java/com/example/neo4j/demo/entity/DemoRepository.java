package com.example.neo4j.demo.entity;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/27
 */
@Repository
public interface DemoRepository extends Neo4jRepository<Demo, Long> {
}
