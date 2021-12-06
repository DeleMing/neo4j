package com.example.neo4j;

import lombok.Data;
import org.neo4j.ogm.annotation.*;

import java.io.Serializable;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/28
 */
@Data
@RelationshipEntity(type = "下游")
public class OperationRelationShip implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private FlinkOperation parent;

    @EndNode
    private FlinkOperation child;

    @Property
    private String relation;
}
