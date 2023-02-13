package com.netease.hz.bdms.easyinsight.service.service.converter;

import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.DetectionIndicatorsSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.model.CheckHistory;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Map;

public class CheckHistoryConverter {

    public static CheckHistory dto2Do(CheckHistorySimpleDTO checkHistorySimpleDTO) {
        CheckHistory checkHistory = BeanConvertUtils.convert(checkHistorySimpleDTO, CheckHistory.class);
        if (null != checkHistory) {
            String log = JsonUtils.toJson(checkHistorySimpleDTO.getLog());
            String rule = JsonUtils.toJson(checkHistorySimpleDTO.getRule());
            String indicators = JsonUtils.toJson(checkHistorySimpleDTO.getIndicators());
            Timestamp logServerTime = checkHistorySimpleDTO.getLogServerTime() != null ? new Timestamp(checkHistorySimpleDTO.getLogServerTime()) : null;
            UserSimpleDTO saver = checkHistorySimpleDTO.getSaver();
            String spmWithOutPos = removePos(checkHistorySimpleDTO.getSpm());
            String saverEmail = "";
            String saverName = "";
            if (null != saver) {
                if(saver.getEmail() != null) {
                    saverEmail = saver.getEmail();
                }
                if(saver.getUserName() != null) {
                    saverName = saver.getUserName();
                }
            }
            Timestamp saveTime = checkHistorySimpleDTO.getSaveTime() != null ? new Timestamp(checkHistorySimpleDTO.getSaveTime()) : null;
            Timestamp updateTime = checkHistorySimpleDTO.getUpdateTime() != null ? new Timestamp(checkHistorySimpleDTO.getUpdateTime()) : null;
            checkHistory.setLog(log)
                    .setSpm(spmWithOutPos)
                    .setRule(rule)
                    .setIndicators(indicators)
                    .setLogServerTime(logServerTime)
                    .setSaverName(saverName)
                    .setSaverEmail(saverEmail)
                    .setSaveTime(saveTime)
                    .setUpdateTime(updateTime);
        }
        return checkHistory;
    }

    public static CheckHistorySimpleDTO do2Dto(CheckHistory checkHistory) {
        CheckHistorySimpleDTO checkHistorySimpleDTO = BeanConvertUtils
                .convert(checkHistory, CheckHistorySimpleDTO.class);
        if (null != checkHistorySimpleDTO) {
            Map<String, Object> log = JsonUtils.parseMap(checkHistory.getLog());
            Map<String, Object> rule = JsonUtils.parseMap(checkHistory.getRule());
            DetectionIndicatorsSimpleDTO indicators = JsonUtils.parseObject(checkHistory.getIndicators(), DetectionIndicatorsSimpleDTO.class);
            Long logServerTime = checkHistory.getLogServerTime() != null ? checkHistory.getLogServerTime().getTime() : null;
            UserSimpleDTO userSimpleDTO = new UserSimpleDTO();
            userSimpleDTO.setUserName(checkHistory.getSaverName());
            userSimpleDTO.setEmail(checkHistory.getSaverEmail());
            Long saveTime = checkHistory.getSaveTime() != null ? checkHistory.getSaveTime().getTime() : null;
            Long updateTime = checkHistory.getUpdateTime() != null ? checkHistory.getUpdateTime().getTime() : null;
            checkHistorySimpleDTO.setLog(log)
                    .setRule(rule)
                    .setIndicators(indicators)
                    .setLogServerTime(logServerTime)
                    .setSaver(userSimpleDTO)
                    .setSaveTime(saveTime)
                    .setUpdateTime(updateTime);
        }
        return checkHistorySimpleDTO;
    }

    private static String removePos(String oidWithPos) {
        if (StringUtils.isNotBlank(oidWithPos)) {
            String pattern = "(:[0-9]*)?";
            return oidWithPos.replaceAll(pattern, "");
        }
        return null;
    }
}
