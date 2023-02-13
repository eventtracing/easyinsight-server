package com.netease.hz.bdms.easyinsight.common.dto.realtimetest;

import com.netease.hz.bdms.easyinsight.common.dto.obj.param.ParamWithValueItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class RealTimeTestResourceDTO {

    private ResourceGenerateDTO generateMeta;

    /**
     * 任务所属基线名
     */
    String baseLineName;

    //血缘路径上所有的埋点对象信息
    List<ObjMeta> objMetas;
    //待测试的埋点对象对应的血缘信息,key:spm
    Map<String,Linage> linageMap;
    //事件的code -> pointId map
    Map<String,Long> eventBuryPointMap;
    //全局公参（目前只有终端）
    List<ParamWithValueItemDTO> globalPublicParams;
    //事件公参（和当前待检查埋点对象相关的事件）
    List<ParamWithValueItemDTO> eventPublicParams;

    Long appId;

    String terminal;
    //所有事件的code -> name map
    Map<String,String> allEventNameMap;
    //所有事件的id -> code map
    Map<Long,String> allEventCodeMap;
    //所有事件的id -> version map
    Map<Long,Long> allEventVersionMap;
    //所有对象的code -> name map
    Map<String,String> allObjNameMap;
    //所有的路由路径 -> oid map
//    Map<String, String> routePath2OidMap;
    //增量spm信息
    Set<String> updateSpmInfo;
    //基线spm信息
    Set<String> baseSpmInfo;

    /**
     * 对象埋点信息，包括基本信息、事件、私参
     */
    @Data
    public static class ObjMeta {

        String oid;

        private String objName;
        /**
         * 类别
         * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
         */
        private Integer objType;
        /**
         * 对象私参
         */
        List<ParamBindItemDTO> privateParams;
        /**
         * 绑定事件
         */
        List<Event> events;

    }

    /**
     * 待测试对象的血缘，由对象本身到根对象
     */
    @Data
    public static class Linage{
        //目标对象spm
        String spm;
        //目标对象oid
        String oid;
        //目标埋点对象id
        Long trackerId;

        //埋点对象oid链路（从当前对象到根对象倒排）
        List<String> linageNodes;
    }

    @Data
    public static class Event{
        /**
         * ID
         */
        private Long id;
        /**
         * 事件代码
         */
        private String code;
        /**
         * 事件名称
         */
        private String name;
        /**
         * 事件关联的参数包版本
         */
        private Long paramVersion;
    }
}
