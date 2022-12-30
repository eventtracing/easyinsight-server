package com.netease.hz.bdms.easyinsight.service.service.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.dto.message.EasyInsightLogMessage;
import com.netease.hz.bdms.easyinsight.common.dto.processor.realtime.rulecheck.CompareItemSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.util.TimeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 从日志中取出指定key的数据
 */
public class LogUtil {
    // 新版日志相关key关键字
    public static final String SPM_KEYWORD = "_spm";
    public static final String ELIST_KEYWORD = "_elist";
    public static final String PLIST_KEYWORD = "_plist";
    public static final String EVENTCODE_KEYWORD = "_eventcode";
    public static final String LOG_SERVER_TIME_KEYWORD = "logServerTime";
    public static final String OID_KEYWORD = "_oid";
    public static final String SPM_SEPERATOR_KEYWORD = "\\|";
    public static final String INDEX_KEYWORD = "index";
    public static final String UNKNOWN_SPM_KEYWORD = "未知spm";
    public static final String UNKNOWN_EVENT = "未知事件类型";

    // 旧版日志相关key关键字
    public static final String OLD_MSPM_KEYWORD = "mspm";
    public static final String OLD_MSPM2_KEYWORD = "_mspm2";
    public static final String UNKNOWN_MSPM_KEYWORD = "未知mspm";
    public static final String OLD_TARGETID_KEYWORD = "target_id";
    public static final String OLD_RESOURCEID_KEYWORD = "resource_id";
    public static final String OLD_RESOURCETYPE_KEYWORD = "resource_type";

    public static String getOs(EasyInsightLogMessage buryPointLog) {
        if (StringUtils.isNotBlank(buryPointLog.getOs())) {
            return buryPointLog.getOs();
        }
        if (buryPointLog.getProps() != null) {
            Object o = buryPointLog.getProps().get("##os");
            if (o != null && o instanceof String) {
                return (String) o;
            }
        }
        return "";
    }

    public static String getAppver(EasyInsightLogMessage buryPointLog) {
        if (StringUtils.isNotBlank(buryPointLog.getApp_ver())) {
            return buryPointLog.getApp_ver();
        }
        if (buryPointLog.getProps() != null) {
            Object o = buryPointLog.getProps().get("##app_ver");
            if (o != null && o instanceof String) {
                return (String) o;
            }
        }
        return "";
    }

    /**
     * 是否是稽查日志
     */
    public static boolean isLogCheckLog(EasyInsightLogMessage buryPointLog) {
        return buryPointLog != null && buryPointLog.getUser_id() != null;
    }

    public static String getSpm(EasyInsightLogMessage buryPointLog) {
        if (!isLogCheckLog(buryPointLog)) {
            return getSpm(buryPointLog.getProps());
        }
        String spm = buryPointLog.getSpm();
        if (StringUtils.isBlank(spm)) {
            return null;
        }
        return spm;
    }

    public static String getSpm(Map<String, Object> map) {
        return getString(map, SPM_KEYWORD);
    }


    public static String getEventCode(Map<String, Object> map) {
        return getString(map, EVENTCODE_KEYWORD);
    }

    public static LinkedHashMap<String, Map<String, Object>> getElist(Map<String, Object> map) {
        return getList(map, ELIST_KEYWORD);
    }

    public static LinkedHashMap<String, Map<String, Object>> getPlist(Map<String, Object> map) {
        return getList(map, PLIST_KEYWORD);
    }

    public static LinkedHashMap<String, Map<String, Object>> getRuleElist(Map<String, Object> map) {
        return getRuleList(map, ELIST_KEYWORD);
    }

    public static LinkedHashMap<String, Map<String, Object>> getRulePlist(Map<String, Object> map) {
        return getRuleList(map, PLIST_KEYWORD);
    }

    public static ArrayList<Map<String, Object>> getRuleRealElist(Map<String, Object> map) {
        return getRuleRealList(map, ELIST_KEYWORD);
    }

    public static ArrayList<Map<String, Object>> getRuleRealPlist(Map<String, Object> map) {
        return getRuleRealList(map, PLIST_KEYWORD);
    }

    public static ArrayList<Map<String, Object>>  getRealPlist(Map<String, Object> map) {
        return getRealList(map, PLIST_KEYWORD);
    }

    public static String getMspmInOldVersion(Map<String, Object> map) {
        Object mspmValue = map.get(OLD_MSPM_KEYWORD);
        if (mspmValue != null) {
            return String.valueOf(mspmValue);
        }
        Object mspm2Value = map.get(OLD_MSPM2_KEYWORD);
        if (mspm2Value != null) {
            return String.valueOf(mspm2Value);
        }
        return "";
    }

    public static String getString(Map<String, Object> map, String key) {
        if (MapUtils.isNotEmpty(map) && map.containsKey(key)) {
            Object result = map.get(key);
            if (result != null) {
                return String.valueOf(result);
            }
        }
        // 注意：这里不能返回空字符串，若返回空字符串，会影响空置率和完整度的判断
        return null;
    }

    private static LinkedHashMap<String, Map<String, Object>> getList(Map<String, Object> map, String key) {
        LinkedHashMap<String, Map<String, Object>> result = Maps.newLinkedHashMap();
        if (MapUtils.isEmpty(map)) {
            return result;
        }
        Object value = map.get(key);
        if (value == null) {
            return result;
        }
        if (value instanceof List) {
            List<Object> valueItems = (List) value;
            for (Object valueItem : valueItems) {
                if (valueItem instanceof Map) {
                    Map<String, Object> valueItemMap = (Map<String, Object>) valueItem;
                    Object oid = valueItemMap.get(OID_KEYWORD);
                    result.put((String) oid, valueItemMap);
                }
            }
            return result;
        }
        List<Object> valueItems = JsonUtils.parseObject(String.valueOf(value), new TypeReference<List<Object>>() {
        });
        if (CollectionUtils.isNotEmpty(valueItems)) {
            for (Object valueItem : valueItems) {
                if (valueItem instanceof Map) {
                    Map<String, Object> valueItemMap = (Map<String, Object>) valueItem;
                    Object oid = valueItemMap.get(OID_KEYWORD);
                    String oidStr = String.valueOf(oid).replaceAll("\\[", "").replaceAll("]", "");
                    if (StringUtils.isBlank(oidStr)) {
                        continue;
                    }
                    result.put((String) oid, valueItemMap);
                }
            }
        }
        return result;
    }

    private static LinkedHashMap<String, Map<String, Object>> getRuleList(Map<String, Object> map, String key) {
        LinkedHashMap<String, Map<String, Object>> result = Maps.newLinkedHashMap();
        if (MapUtils.isNotEmpty(map) && map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof List) {
                List<Object> valueItems = (List) value;
                for (Object valueItem : valueItems) {
                    if (valueItem instanceof Map) {
                        Map<String, Object> valueItemMap = (Map<String, Object>) valueItem;
                        CompareItemSimpleDTO dto = (CompareItemSimpleDTO)valueItemMap.get(OID_KEYWORD);
                        String oidValue = dto.getValue();
                        if (StringUtils.isBlank(oidValue)) {
                            continue;
                        }
                        boolean isJsonList = oidValue.startsWith("[");
                        if (isJsonList) {
                            try {
                                List<String> oids = JsonUtils.parseObject(oidValue, new TypeReference<List<String>>() {});
                                if (CollectionUtils.isEmpty(oids)) {
                                    continue;
                                }
                                result.put(oids.get(0), valueItemMap);
                            } catch (Exception e) {
                                // do nothing
                            }
                        } else {
                            result.put(oidValue, valueItemMap);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static ArrayList<Map<String, Object>> getRuleRealList(Map<String, Object> map, String key) {
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        if (MapUtils.isNotEmpty(map) && map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof List) {
                List<Object> valueItems = (List) value;
                for (Object valueItem : valueItems) {
                    if (valueItem instanceof Map) {
                        Map<String, Object> valueItemMap = (Map<String, Object>) valueItem;
                        Map<String, Object> valueMap = new HashMap<>();
                        for(String valueKey : valueItemMap.keySet()){
                            Map<String, Object> paramMap = (Map<String, Object>)valueItemMap.get(valueKey);
                            valueMap.put(valueKey, paramMap.get("value"));
                        }
                        result.add(valueMap);
                    }
                }
            }
        }
        return result;
    }

    private static ArrayList<Map<String, Object>> getRealList(Map<String, Object> map, String key) {
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        if (MapUtils.isNotEmpty(map) && map.containsKey(key)) {
            List<Object> valueItems = JsonUtils.parseObject(String.valueOf(map.get(key)), new TypeReference<List<Object>>() {});
            if (CollectionUtils.isNotEmpty(valueItems)) {
                for (Object valueItem : valueItems) {
                    if (valueItem instanceof Map) {
                        Map<String, Object> valueItemMap = (Map<String, Object>) valueItem;
                        result.add(valueItemMap);
                    }
                }
            }
        }
        return result;
    }

    public static String getRootPageOid(String spm) {
        if (StringUtils.isNotBlank(spm)) {
            String[] spms = spm.split("\\|");
            return spms.length > 0 ? removePos(spms[spms.length - 1]) : null;
        }
        return null;
    }

    public static String getFirstObjOid(String spm) {
        if (StringUtils.isNotBlank(spm)) {
            String[] spms = spm.split("\\|");
            return spms.length > 0 ? removePos(spms[0]) : null;
        }
        return null;
    }

    public static Long getLogServerTimer(Map<String, Object> logMap) {
        return TimeUtil.parseTimeStr(LogUtil.getString(logMap, LOG_SERVER_TIME_KEYWORD));
    }


    public static String removePos(String oidWithPos) {
        if (StringUtils.isNotBlank(oidWithPos)) {
            String pattern = "(:[0-9]*)?";
            return oidWithPos.replaceAll(pattern, "");
        }
        return null;
    }

    public static String transSpm(String spm, Map<String, String> oid2NameMap) {
        if (StringUtils.isBlank(spm)){
            return "";
        }
        String pattern = "(:[0-9]*)?";
        String spmWithoutPos = spm.replaceAll(pattern, "");

        String[] spms = spmWithoutPos.split("\\|");
        List<String> spmNames = new ArrayList<>();
        for(String oid : spms){
            spmNames.add(oid2NameMap.get(oid));
        }
        return String.join("|", spmNames);
    }
}
