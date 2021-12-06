package com.example.neo4j.demo.relationship;

import com.example.neo4j.demo.entity.Demo;
import lombok.Data;
import org.neo4j.ogm.annotation.*;

import java.io.Serializable;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/27
 */
@Data
@RelationshipEntity(type = "实体关系")
public class DemoRelationShip implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Demo parent;

    @EndNode
    private Demo child;

    @Property
    private String relation;

}
