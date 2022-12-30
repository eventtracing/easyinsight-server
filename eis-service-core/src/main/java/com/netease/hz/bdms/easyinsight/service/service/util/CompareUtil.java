package com.netease.hz.bdms.easyinsight.service.service.util;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.CheckItemResultFormatSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.CheckItemResultSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.CompareItemSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.CheckErrorCauseEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckItemEnum;
import com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;

import com.netease.hz.bdms.easyinsight.common.dto.audit.BloodLink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class CompareUtil {

    /**
     * 日志和规则的单条匹配结果
     *
     * @param param  参数对象（含参数值）, 不能为空
     * @param logMap 日志Map
     * @return 的单条匹配结果
     */
    public static CheckItemResultFormatSimpleDTO check(BloodLink.Param param, Map<String, Object> logMap) {
        if (param == null) {
            throw new CommonException("参数不能为空");
        }

        String paramCode = param.getCode();
        String logValue = LogUtil.getString(logMap, paramCode);
        boolean containsKey = MapUtils.isNotEmpty(logMap) && logMap.containsKey(paramCode);
        // 临时逻辑：验证完后删除
        if (StringUtils.startsWith(logValue, "_v6_")) {
            log.info("v6prefix found {} {}", JsonUtils.toJson(param), JsonUtils.toJson(logMap));
        }
        CheckItemResultSimpleDTO checkResult = ruleCompareLog(containsKey, logValue, param);
        if (checkResult == null) {
            return null;
        }

        CompareItemSimpleDTO log = null;
        CompareItemSimpleDTO rule = null;
        String comment = param.getDescription() != null ? param.getDescription() : "";
        String paramValueInRule = param.getSelectedValues() == null ? JsonUtils.toJson(
                Lists.newArrayList()) : JsonUtils.toJson(param.getSelectedValues());
        if (checkResult.getCheckResult() == CheckResultEnum.PASS) {
            rule = new CompareItemSimpleDTO();
            rule.setKey(paramCode)
                    .setValue(paramValueInRule)
                    .setCause("")
                    .setComment(comment);

            log = new CompareItemSimpleDTO();
            log.setKey(paramCode)
                    .setValue(logValue)
                    .setCause("")
                    .setComment(comment);
        } else {
            rule = new CompareItemSimpleDTO();
            rule.setKey(paramCode)
                    .setValue(paramValueInRule)
                    .setCause(
                            checkResult.getCheckItem() == CheckItemEnum.RULE ? checkResult.getCheckErrorCause()
                                    .getCause() : "")
                    .setComment(comment);

            if (MapUtils.isNotEmpty(logMap) && logMap.containsKey(paramCode)) {
                log = new CompareItemSimpleDTO();
                log.setKey(paramCode)
                        .setValue(logValue)
                        .setCause(
                                checkResult.getCheckItem() == CheckItemEnum.LOG ? checkResult.getCheckErrorCause()
                                        .getCause() : "")
                        .setComment(comment);
            }
        }

        CheckItemResultFormatSimpleDTO result = BeanConvertUtils.convert(checkResult, CheckItemResultFormatSimpleDTO.class);
        result.setLog(log)
                .setRule(rule);
        return result;
    }


    /**
     * 判断一条规则和一条日志的对比结果
     *
     * @param logValue 日志中的值， logValue为null, 表明不存在该logKey； 若logValue为空字符串，表明该logKey存在，但是值为空；否则表示存在logKye, 且值有效
     * @param param    规则：参数及参数值对象信息
     * @return 一条规则和一条日志的对比结果
     */
    private static CheckItemResultSimpleDTO ruleCompareLog(boolean containsKey, String logValue, BloodLink.Param param) {
        // 判断依据1：若日志中存在参数code, 值为空，且规则中非空参数（不管有无取值）, 此时判断不匹配，日志上hover提示“该参数值不能为空”
        // 判断依据2：若日志中存在参数code, 值不为空， 且规则中有取值(不管是否非空), 若取值不匹配，此时判断最终不匹配，日志上hover提示“该参数值不匹配”
        // 判断依据3：若日志中不存在参数code（不管规则中是否是必填参数，有无取值），此时判断不匹配，规则上hover提示“该参数值为必填参数”
        if (param == null) {
            CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
            result.setCheckResult(CheckResultEnum.PASS)
                    .setKeyMatched(true)
                    .setValueMatched(true)
                    .setValueEmpty("".equals(logValue));
            return result;
        }
        // 不含该key时逻辑
        if (!containsKey) {
            // 该参数必须打
            boolean must = param.getMust() == null || Boolean.TRUE.equals(param.getMust());
            if (must) {
                CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
                result.setCheckResult(CheckResultEnum.NOT_PASS)
                        .setCheckErrorCause(CheckErrorCauseEnum.LOSS_REQUIRED_PARAM)
                        .setCheckItem(CheckItemEnum.RULE)
                        .setKeyMatched(false)
                        .setValueMatched(false)
                        .setValueEmpty(false); // 这里表示不存在该参数值，不是参数值为空
                return result;
            }
            CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
            result.setCheckResult(CheckResultEnum.PASS)
                    .setKeyMatched(true)
                    .setValueMatched(true)
                    .setValueEmpty(true);
            return result;
        }
        // 参数值为null时逻辑
        if (logValue == null) {
            CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
            result.setCheckResult(CheckResultEnum.PASS)
                    .setKeyMatched(true)
                    .setValueMatched(true)
                    .setValueEmpty(true);
            return result;
        }
        // 参数值为空时逻辑
        if (StringUtils.isBlank(logValue)) {
            if (param.getNotEmpty()) {
                CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
                result.setCheckResult(CheckResultEnum.NOT_PASS)
                        .setCheckErrorCause(CheckErrorCauseEnum.NOVALUE_FOR_NOTEMPTY_PARAM)
                        .setCheckItem(CheckItemEnum.LOG)
                        .setKeyMatched(true)
                        .setValueMatched(false)
                        .setValueEmpty(true); // 参数值为空字符串
                return result;
            }
            CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
            result.setCheckResult(CheckResultEnum.PASS)
                    .setKeyMatched(true)
                    .setValueMatched(true)
                    .setValueEmpty("".equals(logValue));
            return result;
        }
        // 参数有值时逻辑
        if (CollectionUtils.isNotEmpty(param.getSelectedValues())) {
            CheckResultEnum checkResultEnum = CheckResultEnum.NOT_PASS;
            ParamValueTypeEnum paramValueTypeEnum = ParamValueTypeEnum
                    .fromType(param.getValueType());

            switch (paramValueTypeEnum) {
                case CONSTANT:
                    for (String rule : param.getSelectedValues()) {
                        if (rule.equals(logValue)) {
                            checkResultEnum = CheckResultEnum.PASS;
                            break;
                        }
                    }
                    break;
                case VARIABLE:
                    for (String rule : param.getSelectedValues()) {
//                  if (Pattern.matches(rule, logValue)) {
//                    checkResultEnum = CheckResultEnum.PASS;
//                    break;
//                  }
                        try {
                            if (Pattern.matches(rule, logValue.replaceAll("\n", ""))) {
                                checkResultEnum = CheckResultEnum.PASS;
                                break;
                            }
                        } catch (Exception e) {
                            log.error("match error,rule={},logvalue={}", rule, logValue, e);
                            checkResultEnum = CheckResultEnum.NOT_PASS;
                            break;
                        }
                    }
                    break;
            }

            CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
            result.setCheckResult(checkResultEnum)
                    .setCheckErrorCause(CheckErrorCauseEnum.UNMATCHED_PARAM_VALUE)
                    .setCheckItem(CheckItemEnum.LOG)
                    .setKeyMatched(true)
                    .setValueMatched(!CheckResultEnum.NOT_PASS.equals(checkResultEnum))
                    .setValueEmpty(false);
            return result;
        }

        // 参数有值，不限定取值
        CheckItemResultSimpleDTO result = new CheckItemResultSimpleDTO();
        result.setCheckResult(CheckResultEnum.PASS)
                .setKeyMatched(true)
                .setValueMatched(true)
                .setValueEmpty("".equals(logValue));
        return result;
    }
}
