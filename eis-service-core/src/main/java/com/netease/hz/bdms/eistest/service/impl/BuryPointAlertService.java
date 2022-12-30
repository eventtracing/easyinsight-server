package com.netease.hz.bdms.eistest.service.impl;

import com.netease.eis.adapters.CacheAdapter;
import com.netease.eis.adapters.NotifyUserAdapter;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.realtimetest.TestHistoryRecordDTO;
import com.netease.hz.bdms.easyinsight.common.enums.BuryPointLogTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.TestStatusEnum;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserBaseInfoParam;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.eistest.client.ProcessorRpcAdapter;
import com.netease.hz.bdms.eistest.entity.ClientBasicInfo;
import com.netease.hz.bdms.eistest.cache.BuryPointProcessorKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BuryPointAlertService implements InitializingBean {

    @Resource
    private CacheAdapter cacheAdapter;
    @Resource
    private ConversationBasicInfoService conversationBasicInfoService;
    @Resource
    private ProcessorRpcAdapter processorRpcAdapter;
    @Resource
    private NotifyUserAdapter notifyUserAdapter;
    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    private String host;
    private String userEmail;

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenString("eis.http.host", s -> host = s);
        realtimeConfigAdapter.listenString("default.alert.receivers", s -> userEmail = s);
    }

    public void pointTestAlert(String code) {

        //基线版本
        String baseLine = conversationBasicInfoService.getBaseLineName(code);
        //client信息
        ClientBasicInfo clientBasicInfo =  conversationBasicInfoService.getClientBasicInfo(code);
        //用户信息
        String userkey = BuryPointProcessorKey.getBuryPointUserKey(code);
        String userInfoStr = cacheAdapter.get(userkey);
        UserBaseInfoParam param = JsonUtils.parseObject(userInfoStr, UserBaseInfoParam.class);

        if(param == null){
            log.error("用户信息不存在! code={}", code);
            return;
        }


        //获取会话测试日志未通过数量
        String key = BuryPointProcessorKey.getBuryPointLogCheckErrorCountKey(code);
        String count = cacheAdapter.get(key);
        String defalutUrl = host + "/test/realtime/detail?terminal=%s&conversation=%s&requirements=%s&tab=%s&taskId=%s&terminalId=%s&appId=%s&reqPoolId=%s";
        String detailUrl = String.format(defalutUrl, param.getTerminal(), param.getConversation(), param.getRequirements(), param.getTab(), param.getTaskId(), param.getTerminalId(), param.getAppId(), param.getReqPoolId());

        String exceptionLogKey = BuryPointProcessorKey.getBuryPointLogKey(code, BuryPointLogTypeEnum.EXCEPTION.getCode());
        Long exceptionLogCount = cacheAdapter.llen(exceptionLogKey);

        if(StringUtils.isNotBlank(count) && Long.parseLong(count) > NumberUtils.LONG_ZERO){
            Map<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("title", "【曙光平台-实时测试告警】");
            requestMap.put("user", param.getUserName());
            requestMap.put("version", baseLine);
            requestMap.put("req", param.getReqName());
            requestMap.put("conversation", code);
            requestMap.put("failedCount", count);
            requestMap.put("errorCount", exceptionLogCount);
            requestMap.put("url", detailUrl);
            notifyUserAdapter.sendNotifyTextTemplateContent(userEmail + "," + param.getEmail(), "easy-insight", "【曙光平台-实时测试告警】", requestMap);
        }

        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("targetUrl", detailUrl);

        TestHistoryRecordDTO testHistoryRecordDTO = TestHistoryRecordDTO.builder()
                .code(Long.parseLong(code))
                .userId(param.getId() != null ? param.getId() : 0)
                .appId(param.getAppId() != null ? Long.parseLong(param.getAppId()) : 0)
                .userName(param.getUserName() != null ? param.getUserName() : "")
                .appVersion(clientBasicInfo != null ? clientBasicInfo.getAppVer() : "")
                .baseVersion(baseLine)
                .failedNum(StringUtils.isNotBlank(count)? Long.parseLong(count) : 0)
                .reqName(param.getReqName() != null ? param.getReqName() : "")
                .status(TestStatusEnum.RESULT.getStatus())
                .terminal(param.getTerminal() != null ? param.getTerminal() : "")
                .saveTime(System.currentTimeMillis())
                .extInfo(JsonUtils.toJson(extMap))
                .taskId(Long.parseLong(param.getTaskId()))
                .updateTime(new Timestamp(System.currentTimeMillis()))
                .build();
        processorRpcAdapter.saveTestHistoryRecord(testHistoryRecordDTO);
    }
}
