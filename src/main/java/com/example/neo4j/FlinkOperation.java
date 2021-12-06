package com.example.neo4j;

import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import java.io.Serializable;
import java.util.List;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/28
 */
@Data
@NodeEntity(label = "FlinkOperation")
public class FlinkOperation implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * 属性
     */
    @Property
    private String name;

    private String operationId;

    private List<String> inputIdList;

}
