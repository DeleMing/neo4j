package com.example.neo4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.neo4j.demo.entity.Demo;
import com.example.neo4j.demo.entity.DemoRepository;
import com.example.neo4j.demo.relationship.DemoRelationShip;
import com.example.neo4j.demo.relationship.DemoRelationShipRepository;
import com.example.neo4j.utils.HttpConPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class Neo4jApplicationTests {

    @Autowired
    DemoRepository demoRepository;

    @Autowired
    DemoRelationShipRepository demoRelationShipRepository;

    @Autowired
    FlinkJobRepository flinkJobRepository;

    @Autowired
    FlinkOperationRepository flinkOperationRepository;

    @Autowired
    JobOperationRelationShipRepository jobOperationRelationShipRepository;

    @Autowired
    OperationRelationShipRepository operationRelationShipRepository;

    @Test
    void contextLoads() {
        Demo demo = new Demo();
        demo.setName("demo3");
        Demo demo2 = new Demo();
        demo2.setName("demo4");
        demo = demoRepository.save(demo);
        demo2 = demoRepository.save(demo2);
        DemoRelationShip demoRelationShip = new DemoRelationShip();
        demoRelationShip.setRelation("测试关系");
        demoRelationShip.setParent(demo);
        demoRelationShip.setChild(demo2);
        DemoRelationShip save = demoRelationShipRepository.save(demoRelationShip);
        System.out.println(save.getId());
    }

    @Test
    void add() {
        try {
            String respStr = HttpConPoolUtils.get("http://192.168.70.2:8088/proxy/application_1632742564099_0064/jobs/35fb7b0ea3f9944ccd545363feaedffd#");
            if (StringUtils.isBlank(respStr)) {
                log.error("请求接口为空");
                return;
            }
            Map<String, Object> respMap = JSON.parseObject(respStr, new TypeReference<Map<String, Object>>() {
            });
            if (null == respMap) {
                log.error("序列化map失败");
                return;
            }
            try {
                Map<String, Object> planMap = (Map<String, Object>) respMap.get("plan");
                // FlinkJob
                String jid = (String) planMap.get("jid");
                String jobName = (String) planMap.get("name");

                // FlinkOperation
                List<Map<String, Object>> nodeMapList = (List<Map<String, Object>>) planMap.get("nodes");
                List<FlinkOperation> flinkOperationSourceList = new ArrayList<>();

                for (Map<String, Object> nodeMap : nodeMapList) {
                    String id = (String) nodeMap.get("id");
                    String description = (String) nodeMap.get("description");

                    FlinkOperation flinkOperation = new FlinkOperation();
                    flinkOperation.setName(description);
                    flinkOperation.setOperationId(id);
                    flinkOperationSourceList.add(flinkOperation);
                    if (nodeMap.containsKey("inputs")) {
                        List<Map<String, Object>> inputList = (List<Map<String, Object>>) nodeMap.get("inputs");
                        List<String> inputIdList = new ArrayList<>();
                        for (Map<String, Object> inputMap : inputList) {
                            String inputId = (String) inputMap.get("id");
                            inputIdList.add(inputId);
                        }
                        flinkOperation.setInputIdList(inputIdList);
                    }
                }

                FlinkJob flinkJob = new FlinkJob();
                flinkJob.setName(jobName);
                flinkJob.setJid(jid);
                flinkJob = flinkJobRepository.save(flinkJob);

                Iterable<FlinkOperation> flinkOperationList = flinkOperationRepository.saveAll(flinkOperationSourceList);

                // 建立job与算子之间的关系
                for (FlinkOperation flinkOperation : flinkOperationList) {
                    JobOperationRelationShip jobOperationRelationShip = new JobOperationRelationShip();
                    jobOperationRelationShip.setParent(flinkOperation);
                    jobOperationRelationShip.setChild(flinkJob);
                    jobOperationRelationShip.setRelation("flink任务与算子之间的关系");
                    jobOperationRelationShipRepository.save(jobOperationRelationShip);
                }

                // 建立算子之间的关系
                for (FlinkOperation flinkOperation : flinkOperationList) {
                    List<String> inputIdList = flinkOperation.getInputIdList();
                    if (null == inputIdList || inputIdList.isEmpty()) {
                        continue;
                    }
                    List<FlinkOperation> temp = new ArrayList<>();
                    for (String inputId : inputIdList) {
                        for (FlinkOperation operation : flinkOperationList) {
                            if (inputId.equals(operation.getOperationId())) {
                                temp.add(operation);
                            }
                        }
                    }
                    if (!temp.isEmpty()) {
                        for (FlinkOperation operation : temp) {
                            OperationRelationShip operationRelationShip = new OperationRelationShip();
                            operationRelationShip.setRelation("算子之间的关系");
                            operationRelationShip.setChild(flinkOperation);
                            operationRelationShip.setParent(operation);
                            operationRelationShipRepository.save(operationRelationShip);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取数据异常", e);
            }
        } catch (IOException e) {
            log.error("请求失败:", e);
        }
    }

    @Test
    void deleteAll() {
        jobOperationRelationShipRepository.deleteAll();
        operationRelationShipRepository.deleteAll();
        flinkOperationRepository.deleteAll();
        flinkJobRepository.deleteAll();
    }

    @Test
    void delete() {
        // System.out.println(JSON.toJSONString(flinkJob));
        FlinkJob flinkJob = flinkJobRepository.findByName("sqlserver_sidetest1");
        if (null == flinkJob) {
            return;
        }
        // 删除任务和算子的关系
        Iterable<JobOperationRelationShip> all1 = jobOperationRelationShipRepository.findAll();
        List<JobOperationRelationShip> deleteTemp = new ArrayList<>();
        List<FlinkOperation> operationList = new ArrayList<>();
        for (JobOperationRelationShip jobOperationRelationShip : all1) {
            if (jobOperationRelationShip.getChild().equals(flinkJob)) {
                deleteTemp.add(jobOperationRelationShip);
                FlinkOperation parent = jobOperationRelationShip.getParent();
                operationList.add(parent);
                System.out.println(JSON.toJSONString(jobOperationRelationShip));
            }
        }
        jobOperationRelationShipRepository.deleteAll(deleteTemp);
        // 删除算子与算子之间的关系
        Iterable<OperationRelationShip> all2 = operationRelationShipRepository.findAll();
        List<OperationRelationShip> deleteTemp2 = new ArrayList<>();
        for (FlinkOperation flinkOperation : operationList) {
            for (OperationRelationShip operationRelationShip : all2) {
                if (operationRelationShip.getParent().equals(flinkOperation)) {
                    deleteTemp2.add(operationRelationShip);
                }
            }
        }
        operationRelationShipRepository.deleteAll(deleteTemp2);

        // 删除 job
        flinkJobRepository.delete(flinkJob);
        // 删除 算子
        flinkOperationRepository.deleteAll(operationList);
    }

}



