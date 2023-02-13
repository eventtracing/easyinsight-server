package com.netease.hz.bdms.easyinsight.service.helper;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.dao.model.EisTerminalVersionInfo;
import com.netease.hz.bdms.easyinsight.service.service.TerminalVersionInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: xumengqiang
 * @date: 2022/1/10 10:29
 */

@Component
@Slf4j
public class TerminalVersionInfoHelper {

    @Resource
    private TerminalVersionInfoService terminalVersionInfoService;

    /**
     * 给定端版本名称，若表中不存在则插入
     *
     * @param name
     */
    public void checkAndInsert(String name){
        EisTerminalVersionInfo terminalVersionInfo = terminalVersionInfoService.getByName(name);

        if(null == terminalVersionInfo){
            Long appId = EtContext.get(ContextConstant.APP_ID);
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            String userName = currUser == null ? "" : currUser.getUserName();
            String userEmail = currUser == null ? "" : currUser.getEmail();

            terminalVersionInfo = new EisTerminalVersionInfo();
            terminalVersionInfo.setAppId(appId == null ? 0 : appId);
            terminalVersionInfo.setCreateName(userName);
            terminalVersionInfo.setCreateEmail(userEmail);
            terminalVersionInfo.setUpdateName(userName);
            terminalVersionInfo.setUpdateEmail(userEmail);
            terminalVersionInfo.setName(name);

            terminalVersionInfoService.create(terminalVersionInfo);
        }

    }

}
