package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.eis.adapters.NotifyUserAdapter;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.enums.PackageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuditReportService implements InitializingBean {

    @Resource
    private NotifyUserAdapter notifyUserAdapter;

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    private String userEmail;

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenString("default.alert.receivers", s -> userEmail = s);
    }

    public void auditReportAlert(String detailStr, String email, PackageTypeEnum packageTypeEnum, String version) {
        String finalEmail = "";
        if(packageTypeEnum.getType() == PackageTypeEnum.RELEASE.getType() || packageTypeEnum.getType() == PackageTypeEnum.GRAY.getType()){
            finalEmail = userEmail;
        }
        if(email != null){
            finalEmail = StringUtils.isBlank(finalEmail) ? email : finalEmail + "," + email;
        }
        //不需要发送
        if(StringUtils.isBlank(finalEmail)){
            return;
        }
        String mainTitle = "曙光平台-稽查报告-" + "云音乐" + packageTypeEnum.getDesc();
        if(StringUtils.isNotBlank(version)) {
            mainTitle += "-" + version;
        }
        notifyUserAdapter.sendNotifyTextContent(finalEmail, mainTitle, detailStr);

        //转化格式发邮件
        String[] strArray = detailStr.split("\n");
        StringBuilder newStr = new StringBuilder();
        for(String line : strArray){
            if(line.startsWith("详情连接：")){
                line = line.replace("详情连接：", "");
                newStr.append("<a href=\"").append(line).append("\">详情连接</a>");
            }else {
                newStr.append("<p>").append(line).append("</p>");
            }
        }
        notifyUserAdapter.sendNotifyMail(finalEmail, mainTitle, newStr.toString());
    }

    public void buryPointTestAlert(String detailStr, String comment) {
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        //不需要发送
        if (currentUserDTO == null || StringUtils.isBlank(currentUserDTO.getEmail())) {
            return;
        }
        String userEmails = currentUserDTO.getEmail() + "," + userEmail;
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("data", currentUserDTO.getUserName() + " " + detailStr);
        requestMap.put("title", "【埋点任务流转异常通知】");
        requestMap.put("comment", comment);
        notifyUserAdapter.sendNotifyTextTemplateContent(userEmails, "insight-process-forward", "【埋点任务流转异常通知】", requestMap);
    }
}
