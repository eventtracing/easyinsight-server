package com.netease.hz.bdms.eistest.web.controller;

import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserBaseInfoParam;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.eistest.entity.ClientBasicInfo;
import com.netease.hz.bdms.eistest.entity.TestDetailInfo;
import com.netease.hz.bdms.eistest.cache.BuryPointProcessorKey;
import com.netease.hz.bdms.eistest.service.impl.ConversationBasicInfoService;
import com.netease.hz.bdms.eistest.ws.SessionManager;
import com.netease.hz.bdms.eistest.ws.dto.AppStorage;
import com.netease.hz.bdms.eistest.ws.handler.AppWsHandler;
import com.netease.hz.bdms.eistest.ws.session.AppSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;

@Slf4j
@RestController
@RequestMapping("/processor/realtime/exam")
public class RealtimeExamController {

    @Resource
    private ApplicationContext context;

    @Resource
    private ConversationBasicInfoService conversationBasicInfoService;

    @Resource
    private CacheAdapter cacheAdapter;

    @GetMapping("/basicinfo/client")
    public HttpResult getBasicClientInfo(String conversation) {
        if (StringUtils.isEmpty(conversation)) {
            throw new CommonException("conversation 参数错误，不能为空");
        }
        // 客户端上报的基础会话信息
        ClientBasicInfo clientBasicInfo = conversationBasicInfoService.getClientBasicInfo(conversation);
        if (clientBasicInfo == null) {
            return HttpResult.success(new TestDetailInfo());
        }
        TestDetailInfo testDetailInfo = new TestDetailInfo();
        BeanUtils.copyProperties(clientBasicInfo, testDetailInfo);

        // 网站访问上下文
        String userkey = BuryPointProcessorKey.getBuryPointUserKey(conversation);
        String userInfoStr = cacheAdapter.get(userkey);
        if (StringUtils.isNotBlank(userInfoStr)) {
            UserBaseInfoParam param = JsonUtils.parseObject(userInfoStr, UserBaseInfoParam.class);
            if (param != null) {
                testDetailInfo.setTester(param.getUserName());
                testDetailInfo.setTesterEmail(param.getEmail());
            }
        }

        testDetailInfo.setBaseLineName(conversationBasicInfoService.getBaseLineName(conversation));
        return HttpResult.success(testDetailInfo);
    }

    @GetMapping("/check-scanned")
    public HttpResult<Boolean> checkScanned(String conversation) {
        if (StringUtils.isEmpty(conversation)) {
            throw new CommonException("conversation 参数错误，不能为空");
        }

        try {
            String s = cacheAdapter.get(AppWsHandler.getAppScanSuccessCacheKey(conversation));
            return HttpResult.success(StringUtils.isNotBlank(s));
        } catch (Exception e) {
            log.error("获取APP是否扫码成功失败", e);
            return HttpResult.error(500, "获取APP是否扫码成功失败");
        }
    }

    @DeleteMapping("/clear/log")
    public HttpResult clearLog(@PathParam("conversation") String conversation) {
        SessionManager sessionManager = getBean("appSessionManager");
        AppSession session = (AppSession) sessionManager.getSessionByCode(conversation);
        if (session != null) {
            AppStorage storage = session.getStorage();
            storage.getStats().clearAllStatisticsResultInTargetConversation();
            storage.getQueue().clear();
        }
        return HttpResult.success();
    }

    public <T> T getBean(String name) {
        return (T) context.getBean(name);
    }
}
