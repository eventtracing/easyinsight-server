package com.netease.hz.bdms.easyinsight.common.util;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.enums.RequirementTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonUtil {

    public static String getSpmStringByObjIds(List<Long> objIds){
        List<String> toStringList = objIds.stream().map(e -> e.toString()).collect(Collectors.toList());
        String spmByObjIds = String.join("|",toStringList);
        return spmByObjIds;
    }

    public static List<Long> transSpmToObjIdList(String spmByObjIds){
        List<String> objIdStringList = Lists.newArrayList(spmByObjIds.split("\\|"));
        return objIdStringList.stream().map(e -> Long.valueOf(e)).collect(Collectors.toList());
    }

    public static List<String> transSpmToOidList(String spmByObjIds){
        return Lists.newArrayList(spmByObjIds.split("\\|"));
    }

    public static String transReqTypeToDescription(String reqTypeEnumStr){
        List<String> reqTypeNameList = new ArrayList<>();
        if(!StringUtils.isEmpty(reqTypeEnumStr)){
            List<String> reqTypeEnumList = Lists.newArrayList(reqTypeEnumStr.split(","));
            for (String enumCodeStr : reqTypeEnumList) {
                Integer enumCode = Integer.valueOf(enumCodeStr);
                RequirementTypeEnum requirementTypeEnum = RequirementTypeEnum.fromReqType(enumCode);
                reqTypeNameList.add(requirementTypeEnum.getDesc());
            }
        }
        return String.join("，",reqTypeNameList);
    }

    public static String transSpmByObjIdToSpmByOid(Map<Long,String> objIdToOidMap,String spmByObjId){
        if(org.apache.commons.lang3.StringUtils.isBlank(spmByObjId)){
            return "";
        }
        List<String> spmByObjIdAsList = Lists.newArrayList(spmByObjId.split("\\|"));
        List<String> oidList = spmByObjIdAsList.stream()
                .map(e -> Long.valueOf(e))
                .map(e -> objIdToOidMap.get(e))
                .collect(Collectors.toList());
        return String.join("|",oidList);
    }

    public static String transSpmByOidToSpmByObjId(Map<String,Long> oidToObjIdMap, String spmByOid){
        if(org.apache.commons.lang3.StringUtils.isBlank(spmByOid)){
            return "";
        }
        List<String> spmByOidAsList = Lists.newArrayList(spmByOid.split("\\|"));
        List<Long> objIdList = spmByOidAsList.stream()
                .map(e -> oidToObjIdMap.get(e))
                .collect(Collectors.toList());
        for (Long objId : objIdList) {
            if (objId == null) {
                return "";
            }
        }
        return objIdList.stream().map(String::valueOf).collect(Collectors.joining("|"));
    }

    public static String getName(String oid, Map<String, String> oidNameMap) {
        if (oidNameMap == null) {
            return "未知";
        }
        String name = oidNameMap.get(oid);
        if (name == null) {
            return "未知";
        }
        return name;
    }

    /**
     * 获取SPM中对象名拼凑的名字
     */
    public static String getSpmName(String spmWithOutPos, Map<String, String> oidNameMap) {
        List<String> oids = CommonUtil.transSpmToOidList(spmWithOutPos);
        if (CollectionUtils.isEmpty(oids)) {
            return "";
        }
        List<String> list = new ArrayList<>();
        for (String oid : oids) {
            String name = CommonUtil.getName(oid, oidNameMap);
            list.add(name);
        }
        return StringUtils.join(list, "|");
    }
}
