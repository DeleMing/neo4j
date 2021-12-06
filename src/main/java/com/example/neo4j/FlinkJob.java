package com.example.neo4j;

import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import java.io.Serializable;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/28
 */
@Data
@NodeEntity(label = "FlinkJob")
public class FlinkJob implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * 属性
     */
    @Property
    private String name;

    private String jid;
}
