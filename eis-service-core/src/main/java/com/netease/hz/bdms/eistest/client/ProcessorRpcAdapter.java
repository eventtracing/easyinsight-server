package com.netease.hz.bdms.eistest.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.BranchCoverIgnoreRequestDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.RealTimeTestResourceDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.ResourceRequestDTO;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.exception.RealTimeTestException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.util.RestTemplateUtils;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.BranchCoverageDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 调用外部接口
 */
@Component
@Slf4j
public class ProcessorRpcAdapter implements InitializingBean {

    @Autowired
    RestTemplateUtils restTemplateUtils;

    private String host;

    private String ruleResourcePath = "/et/v1/realtime/in/resource";

    private String saveResourcePath = "/et/v1/realtime/validate/save";

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenString("eis.backend-http.host", s -> host = s);
    }


    /**
     * 获取实时测试源数据
     * @param appId
     * @param domainId
     * @return
     */
    public RealTimeTestResourceDTO getResource(Long reqPoolId, Long terminalId,Long domainId, Long appId) {
        ResponseEntity<HttpResult> responseEntity;
        ResourceRequestDTO resourceRequest = new ResourceRequestDTO();
        resourceRequest.setAppId(appId);
        resourceRequest.setDomainId(domainId);
        resourceRequest.setTaskId(reqPoolId);
        resourceRequest.setTerminalId(terminalId);
        String url = host + ruleResourcePath;
        try {
            responseEntity = restTemplateUtils.post(url, resourceRequest, HttpResult.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RealTimeTestException("查询管理服务获取实时测试规则资源失败", e);
        }
        if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody().getCode() != 0) {
            log.error("errmsg:{}", responseEntity.getBody());
            throw new RealTimeTestException("查询管理服务获取实时测试规则资源失败");
        }
        ObjectMapper mapper = new ObjectMapper();
        RealTimeTestResourceDTO realTimeTestResourceDTO = mapper.convertValue(responseEntity.getBody().getResult(),
                RealTimeTestResourceDTO.class);
        return realTimeTestResourceDTO;
    }

    /**
     * 获取无需覆盖的待测分支
     */
    public List<BranchCoverageDetailVO> updateNoNeedCoverBranches(String conversationId) {
        ResponseEntity<HttpResult> responseEntity;
        BranchCoverIgnoreRequestDTO requestDTO = new BranchCoverIgnoreRequestDTO();
        requestDTO.setConversationId(conversationId);
        String url = host + "/et/v1/realtime/branchCover/ignore/get";
        try {
            responseEntity = restTemplateUtils.post(url, requestDTO, HttpResult.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RealTimeTestException("获取无需覆盖的待测分支失败", e);
        }
        if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody().getCode() != 0) {
            log.error("errmsg:{}", responseEntity.getBody());
            throw new RealTimeTestException("获取无需覆盖的待测分支失败");
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(responseEntity.getBody().getResult(),
                new TypeReference<List<BranchCoverageDetailVO>>() {});
    }

    /**
     * 保存实时测试数据
     *
     * @param resourceRequest
     * @return saveResult
     */
    public List<Long> saveTestRecords(List<CheckHistorySimpleDTO> resourceRequest) {
        ResponseEntity<HttpResult> responseEntity;
        String url = host + saveResourcePath;
        try {
            responseEntity = restTemplateUtils.post(url, resourceRequest, HttpResult.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody().getCode() != 0) {
            log.error("测试日志保存失败:{}", JsonUtils.toJson(responseEntity));
            return new ArrayList<>();
        }
        log.info("测试记录：request:{}, ret:{}",JsonUtils.toJson(resourceRequest), JsonUtils.toJson(responseEntity.getBody()));
        ObjectMapper mapper = new ObjectMapper();
        List<Long> saveResult = (List<Long>) responseEntity.getBody().getResult();
        return saveResult;
    }

    /**
     * 保存实时测试会话记录
     *
     * @param testHistoryRecordDTO
     * @return saveResult
     */
    public Integer saveTestHistoryRecord(TestHistoryRecordDTO testHistoryRecordDTO) {
        ResponseEntity<HttpResult> responseEntity;
        String url = host + "/realtime/test/history/save";
        try {
            responseEntity = restTemplateUtils.post(url, testHistoryRecordDTO, HttpResult.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody().getCode() != 0) {
            log.error("会话记录保存失败:{}", JsonUtils.toJson(responseEntity));
            return null;
        }
        return (Integer) responseEntity.getBody().getResult();
    }
}
