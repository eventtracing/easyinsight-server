package com.netease.hz.bdms.easyinsight.common.dto.audit;

import lombok.Data;

import java.util.List;

@Data
public class BloodLink {
    private Long appId;
    private BloodLink parent;
    private String oid;
    private String objName;
    /**
     * 类别
     *
     * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
     */
    private Integer objType;
    private String objVersion;
    private List<Event> events;
    private List<Param> bindParams;
    private String terminal;
    private String terminalVersion;

    public String uniqueKey() {
        String key = oid;
        if (parent != null) {
            key += "/" + parent.uniqueKey();
        }
        return key;
    }

    @Data
    public static class Event {
        private String code;
        private String name;
    }

    @Data
    public static class Param {
        private String code;
        private String name;
        private Boolean notEmpty;
        /**
         * 是否必须传
         */
        private Boolean must = true;
        /**
         * 参数类型
         */
        private Integer paramType;
        /**
         * 参数值类型
         *
         * @see com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum
         */
        private Integer valueType;
        private List<String> selectedValues;
        /**
         * 取值描述
         */
        private String description;

        /**
         * 校验范围
         */
        private CheckScopeEnum checkScopeEnum = CheckScopeEnum.ALL;
    }
}
