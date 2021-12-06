package com.example.neo4j.controller;

import com.alibaba.fastjson.JSON;
import com.example.neo4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: LiaoMingtao
 * @date: 2021/10/28
 */
@Slf4j
@RestController
public class BaseController {

    @Autowired
    private FlinkJobRepository flinkJobRepository;

    @Autowired
    private FlinkOperationRepository flinkOperationRepository;

    @Autowired
    private JobOperationRelationShipRepository jobOperationRelationShipRepository;

    @Autowired
    private OperationRelationShipRepository operationRelationShipRepository;

    @PostMapping(value = "/addFlinkJob")
    public String addFlinkJob(@RequestBody Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return "入参不能为空";
        }
        try {
            Map<String, Object> planMap = (Map<String, Object>) map.get("plan");
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

            // 删除原有关系
            deleteFlinkJobByName(jobName);

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
            log.error("数据解析失败", e);
            return "数据解析失败" + e;
        }
        return "新增成功";
    }

    @GetMapping(value = "/deleteFlinkJobByName")
    public void deleteFlinkJobByName(String jobName) {
        FlinkJob flinkJob = flinkJobRepository.findByName(jobName);
        if (null == flinkJob) {
            log.info("不存在{}任务无需删除", jobName);
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

    @GetMapping(value = "/deleteFlinkAll")
    public String deleteFlinkAll() {
        jobOperationRelationShipRepository.deleteAll();
        operationRelationShipRepository.deleteAll();
        flinkOperationRepository.deleteAll();
        flinkJobRepository.deleteAll();
        return "已执行删除操作";
    }

}
