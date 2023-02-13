package com.netease.hz.bdms.easyinsight.web.demo.adapters;

import com.netease.eis.adapters.NotifyUserAdapter;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class DemoUserNotifyAdapter implements NotifyUserAdapter {

    @Override
    public void sendNotifyTextContent(String userEmail, String title, String content) {
        log.info("sendNotifyTextContent {} {} {}", userEmail, title, content);
    }

    @Override
    public void sendNotifyMail(String userEmail, String title, String content) {
        log.info("sendNotifyMail {} {} {}", userEmail, title, content);
    }

    @Override
    public void sendNotifyTextTemplateContent(String userEmail, String topic, String title, Map<String, Object> data) {
        log.info("sendNotifyTextTemplateContent {} {} {} {}", userEmail, topic, title, JsonUtils.toJson(data));
    }
}
