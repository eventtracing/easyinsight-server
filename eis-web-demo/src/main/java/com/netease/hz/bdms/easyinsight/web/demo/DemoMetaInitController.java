package com.netease.hz.bdms.easyinsight.web.demo;

import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.dao.*;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DemoMetaInitController {

    @Autowired
    private ParamMapper paramMapper;

    @Autowired
    private ParamValueMapper paramValueMapper;

    @Autowired
    private ParamBindMapper paramBindMapper;

    @Autowired
    private VersionMapper versionMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private ParamBindValueMapper paramBindValueMapper;

    ParamValue pDuration = new ParamValue().setCode("^[0-9]*$").setName("数字格式").setDescription("ms").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pEsParams = new ParamValue().setCode(".*").setName("json格式字符串").setDescription("").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pResolution = new ParamValue().setCode(".*").setName(".*").setDescription("").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pActSeq = new ParamValue().setCode("^[0-9]*$").setName("数字格式").setDescription("- 用户交互深度，每次交互（点击/滑动等）都会+1 - 在当前根page的一次曝光生命周期内自增，如果页面重新曝光了，则先置0 - 页面曝光，不自增").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pCarrier = new ParamValue().setCode(".*").setName(".*").setDescription("").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pImei = new ParamValue().setCode(".*").setName(".*").setDescription("").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pDevice = new ParamValue().setCode(".*").setName(".*").setDescription("设备标识：https://www.theiphonewiki.com/wiki/Models").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ParamValue pOaid = new ParamValue().setCode(".*").setName(".*").setDescription(".*").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param carrier = new Param().setCode("carrier").setName("运营商").setParamType(2).setValueType(2).setDescription("").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param imei = new Param().setCode("imei").setName("终端唯一标识").setParamType(2).setValueType(2).setDescription("").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param oaid = new Param().setCode("oaid").setName("oaid").setParamType(2).setValueType(2).setDescription("移动安全联盟针对该问题联合国内手机厂商推出补充设备标准体系方案，选择 OAID 字段作为 IMEI 等的替代字段").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param device = new Param().setCode("device").setName("设备标识").setParamType(2).setValueType(2).setDescription("Identifier https://www.theiphonewiki.com/wiki/Models").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param resolution = new Param().setCode("resolution").setName("屏幕分辨率").setParamType(2).setValueType(2).setDescription("格式1125x2436").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param es_params =  new Param().setCode("es_params").setName("滑动距离").setParamType(2).setValueType(2).setDescription("滑动结束较滑动开始的偏移量").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param _actseq = new Param().setCode("_actseq").setName("交互深度").setParamType(2).setValueType(2).setDescription("- 用户交互深度，每次交互（点击/滑动等）都会+1 - 在当前根page的一次曝光生命周期内自增，如果页面重新曝光了，则先置0 - 页面曝光，不自增").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Param _duration = new Param().setCode("_duration").setName("时长").setParamType(2).setValueType(2).setDescription("单位ms； ！注意！对于pd、ed等曝光结束事件，SDK内部已经直接计算好了，不需要业务方再开发").setCreateEmail("").setCreateName("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis()));
    Map<Param, ParamValue> paramToValueMap = new HashMap<>();

    /**
     * 初始化DEMO的数据配置
     */
    @GetMapping("/clear-meta")
    public String clearMeta(long appId) {
        paramMapper.deleteByAppId(appId);
        paramValueMapper.deleteByAppId(appId);
        paramBindMapper.deleteByAppId(appId);
        versionMapper.deleteByAppId(appId);
        eventMapper.deleteByAppId(appId);
        paramBindValueMapper.deleteByAppId(appId);
        return "OK";
    }

    /**
     * 初始化DEMO的数据配置
     */
    @GetMapping("/init-meta")
    public String initMeta(long appId) {
        // 0. 初始化全局公参
        Map<Param, List<ParamValue>> initGlobalParams = getInitGlobalParams(appId);
        initGlobalParams.forEach((param, paramValues) -> {
            paramMapper.insert(param);
            Long newParamId = param.getId();
            if (CollectionUtils.isNotEmpty(paramValues)) {
                paramValues.forEach(p -> {
                    p.setParamId(newParamId);
                    paramValueMapper.insert(p);
                });
            }
        });

        // 1. 初始化对象参数
        Map<Param, List<ParamValue>> initObjParams = getInitObjParams(appId);
        initObjParams.forEach((param, paramValues) -> {
            paramMapper.insert(param);
            Long newParamId = param.getId();
            if (CollectionUtils.isNotEmpty(paramValues)) {
                paramValues.forEach(p -> {
                    p.setParamId(newParamId);
                    paramValueMapper.insert(p);
                });
            }
        });

        // 2. 初始化事件参数
        paramToValueMap.put(_duration, pDuration);
        paramToValueMap.put(es_params, pEsParams);
        paramToValueMap.put(_actseq, pActSeq);
        paramToValueMap.put(carrier, pCarrier);
        paramToValueMap.put(imei, pImei);
        paramToValueMap.put(oaid, pOaid);
        paramToValueMap.put(device, pDevice);
        paramToValueMap.put(resolution, pResolution);

        Map<Event, EventParamData> initEvents = getInitEvents(appId);
        // 2.1 插入事件
        Map<String, Param> eventParamMap = new HashMap<>();
        initEvents.forEach((event, data) -> {
            event.setAppId(appId);
            List<Event> events = eventMapper.selectByCode(event.getCode(), event.getAppId());
            if (CollectionUtils.isEmpty(events)) {
                eventMapper.insert(event);
            } else {
                event.setId(events.get(0).getId());
            }
            data.getEventParams().forEach(eventParam -> eventParamMap.put(eventParam.getCode(), eventParam));
        });
        // 2.2 插入事件参数
        eventParamMap.values().forEach(eventParam -> {
            eventParam.setAppId(appId);
            paramMapper.insert(eventParam);
        });
        // 2.3 插入version
        initEvents.forEach((event, data) -> {
            Version version = new Version();
            version.setAppId(appId)
                    .setName("V1")
                    .setEntityId(event.getId())
                    .setEntityType(EntityTypeEnum.EVENT.getType())
                    .setVersionSource(VersionSourceEnum.MANUAL.getType())
                    .setCurrentUsing(true)
                    .setPreset(true)
                    .setCreateEmail("")
                    .setCreateName("")
                    .setUpdateEmail("")
                    .setUpdateName("");
            List<Version> versions = versionMapper.selectByEntityId(version.getEntityId(), version.getEntityType(), version.getName(), appId);
            if (CollectionUtils.isNotEmpty(versions)) {
                version.setAppId(versions.get(0).getId());
            } else {
                versionMapper.insert(version);
            }
            data.setVersion(version);
        });
        // 2.4 绑定event-version-param -> 产生bindId
        initEvents.forEach((event, data) -> {
            List<ParamBind> paramBinds = data.getEventParams().stream().map(p -> new ParamBind()
                    .setParamId(eventParamMap.get(p.getCode()).getId())
                    .setEntityId(event.getId())
                    .setEntityType(EntityTypeEnum.EVENT.getType())
                    .setVersionId(data.getVersion().getId())
                    .setAppId(appId)
                    .setCreateEmail("")
                    .setCreateName("")
                    .setUpdateEmail("")
                    .setUpdateName("")
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()))
                    .setDescription("")
                    .setNotEmpty(true)
                    .setMust(true)
                    .setNeedTest(true)
                    .setIsEncode(false)).collect(Collectors.toList());
            paramBinds.forEach(paramBind ->  {
                paramBind.setAppId(appId);
                paramBindMapper.insert(paramBind);
                data.getParamBinds().add(paramBind);
            });
        });

        // 2.5 插入参数取值
        Map<String, ParamValue> paramValueByKey = new HashMap<>();
        paramToValueMap.forEach((param, paramValue) -> {
            Long paramId = eventParamMap.get(param.getCode()).getId();
            paramValue.setParamId(paramId);
            paramValueByKey.put(paramId + "##" + paramValue.getCode(), paramValue);
        });
        paramValueByKey.values().forEach(paramValue -> {
            paramValue.setAppId(appId);
            paramValueMapper.insert(paramValue);
        });

        // 2.6 绑定bindId和bindValue
        initEvents.forEach((event, data) -> {
            List<ParamBindValue> paramBindValues = new ArrayList<>();
            data.getEventParams().forEach((param) -> {
                Long paramId = eventParamMap.get(param.getCode()).getId();
                ParamValue paramValue = paramToValueMap.get(param);
                ParamValue insertedParamValue = paramValueByKey.get(paramId + "##" + paramValue.getCode());
                Long paramValueId = insertedParamValue.getId();
                ParamBindValue paramBindValue = new ParamBindValue();
                ParamBind paramBind = data.getParamBinds().stream()
                        .filter(o -> o.getParamId().equals(paramId) && o.getEntityId().equals(event.getId()))
                        .findAny().orElse(null);
                if (paramBind == null) {
                    return;
                }
                paramBindValue.setBindId(paramBind.getId());
                paramBindValue.setParamValueId(paramValueId);
                paramBindValue.setAppId(appId);
                paramBindValue.setDescription("");
                paramBindValue.setCreateEmail("");
                paramBindValue.setCreateName("");
                paramBindValue.setUpdateEmail("");
                paramBindValue.setUpdateName("");
                paramBindValue.setCreateTime(new Timestamp(System.currentTimeMillis()));
                paramBindValue.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                paramBindValues.add(paramBindValue);
            });
            if (CollectionUtils.isNotEmpty(paramBindValues)) {
                paramBindValueMapper.batchInsert(paramBindValues);
            }
        });
        return "OK";
    }

    private Map<Event, EventParamData> getInitEvents(long appId) {

        Map<Event, EventParamData> result = new HashMap<>();
        EventParamData ac = result.computeIfAbsent(new Event().setCode("_ac").setName("app冷启动")
                .setAppId(appId).setDescription("app active ：app冷启动")
                .setSelectedByDefault(false).setApplicableObjTypes("[1]"), k -> new EventParamData());
        ac.getEventParams().add(carrier);
        ac.getEventParams().add(imei);
        ac.getEventParams().add(oaid);
        ac.getEventParams().add(device);
        ac.getEventParams().add(resolution);


        EventParamData ai = result.computeIfAbsent(new Event().setCode("_ai").setName("app进入前台")
                .setAppId(appId).setDescription("app in：app进入前台")
                .setSelectedByDefault(false).setApplicableObjTypes("[1]"), k -> new EventParamData());
        ai.getEventParams().add(device);
        ai.getEventParams().add(oaid);
        ai.getEventParams().add(imei);
        ai.getEventParams().add(carrier);

        result.computeIfAbsent(new Event().setCode("_ao").setName("app退出到后台")
                .setAppId(appId).setDescription("app out：app退到后台")
                .setSelectedByDefault(false).setApplicableObjTypes("[1]"), k -> new EventParamData())
                .getEventParams().add(_duration);

        result.computeIfAbsent(new Event().setCode("_ec").setName("对象点击")
                .setAppId(appId).setDescription("element click ：对象点击")
                .setSelectedByDefault(false).setApplicableObjTypes("[1,2,3]"), k -> new EventParamData())
                .getEventParams().add(_actseq);

        result.computeIfAbsent(new Event().setCode("_ed").setName("对象曝光结束")
                .setAppId(appId).setDescription("element viewend:对象视觉消失")
                .setSelectedByDefault(false).setApplicableObjTypes("[2]"), k -> new EventParamData())
                .getEventParams().add(_duration);

        result.computeIfAbsent(new Event().setCode("_elc").setName("对象长按")
                .setAppId(appId).setDescription("element long click：对象长按")
                .setSelectedByDefault(false).setApplicableObjTypes("[2]"), k -> new EventParamData())
                .getEventParams().add(_actseq);

        result.computeIfAbsent(new Event().setCode("_es").setName("流滑动")
                .setAppId(appId).setDescription("element slide :对象滑动，打点时机为滑动结束")
                .setSelectedByDefault(false).setApplicableObjTypes("[1,2]"), k -> new EventParamData())
                .getEventParams().add(es_params);

        result.computeIfAbsent(new Event().setCode("_ev").setName("对象曝光开始")
                .setAppId(appId).setDescription("element view:打点时机为视觉漏出")
                .setSelectedByDefault(false).setApplicableObjTypes("[2]"), k -> new EventParamData());

        result.computeIfAbsent(new Event().setCode("_pd").setName("页面曝光结束")
                .setAppId(appId).setDescription("page viewend:页面视觉消失")
                .setSelectedByDefault(true).setApplicableObjTypes("[1,3]"), k -> new EventParamData())
                .getEventParams().add(_duration);

        result.computeIfAbsent(new Event().setCode("_pv").setName("页面曝光开始")
                .setAppId(appId).setDescription("page view:打点时机为：视觉露出")
                .setSelectedByDefault(true).setApplicableObjTypes("[1,3,4]"), k -> new EventParamData());

        return result;
    }

    private Map<Param, List<ParamValue>> getInitGlobalParams(long appId) {

        Map<Param, List<ParamValue>> result = new HashMap<>();
        result.computeIfAbsent(new Param().setCode("appver").setName("app版本")
                        .setParamType(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                        .setAppId(appId).setDescription("app版本")
                        .setCreateEmail("").setCreateName("")
                        .setUpdateEmail("").setUpdateName("")
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())),k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("app版本").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        result.computeIfAbsent(new Param().setCode("device_id").setName("deviceId")
                        .setParamType(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                        .setAppId(appId).setDescription("deviceId")
                        .setCreateEmail("").setCreateName("")
                        .setUpdateEmail("").setUpdateName("")
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("deviceId").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        result.computeIfAbsent(new Param().setCode("ip").setName("ip")
                        .setParamType(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                        .setAppId(appId).setDescription("ip")
                        .setCreateEmail("").setCreateName("")
                        .setUpdateEmail("").setUpdateName("")
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("ip").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        result.computeIfAbsent(new Param().setCode("log_time").setName("客户端日志时间")
                        .setParamType(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                        .setAppId(appId).setDescription("unixtime")
                        .setCreateEmail("").setCreateName("")
                        .setUpdateEmail("").setUpdateName("")
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("unixtime").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        result.computeIfAbsent(new Param().setCode("os_ver").setName("系统版本")
                        .setParamType(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                        .setAppId(appId).setDescription("系统版本")
                        .setCreateEmail("").setCreateName("")
                        .setUpdateEmail("").setUpdateName("")
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("系统版本").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        result.computeIfAbsent(new Param().setCode("os").setName("操作系统")
                        .setParamType(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                        .setAppId(appId).setDescription("操作系统")
                        .setCreateEmail("").setCreateName("")
                        .setUpdateEmail("").setUpdateName("")
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("操作系统").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        return result;
    }

    private Map<Param, List<ParamValue>> getInitObjParams(long appId) {
        Map<Param, List<ParamValue>> result = new HashMap<>();
        result.computeIfAbsent(new Param().setCode("s_cid").setName("对象ID")
                .setParamType(ParamTypeEnum.OBJ_NORMAL_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                .setAppId(appId).setDescription("对象ID")
                .setCreateEmail("").setCreateName("")
                .setUpdateEmail("").setUpdateName("")
                .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("与s_ctype类型对应的id").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));

        List<ParamValue> sctypeParamValues = result.computeIfAbsent(new Param().setCode("s_ctype").setName("对象类型")
                .setParamType(ParamTypeEnum.OBJ_NORMAL_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                .setAppId(appId).setDescription("对象类型")
                .setCreateEmail("").setCreateName("")
                .setUpdateEmail("").setUpdateName("")
                .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>());
        sctypeParamValues.add(new ParamValue().setCode("image").setName("image").setAppId(appId).setDescription("图片").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));
        sctypeParamValues.add(new ParamValue().setCode("song").setName("song").setAppId(appId).setDescription("歌曲").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));


        result.computeIfAbsent(new Param().setCode("s_ctraceid").setName("请求traceID")
                .setParamType(ParamTypeEnum.OBJ_NORMAL_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                .setAppId(appId).setDescription("请求traceID")
                .setCreateEmail("").setCreateName("")
                .setUpdateEmail("").setUpdateName("")
                .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())),k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("请求traceID").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));


        result.computeIfAbsent(new Param().setCode("s_ctraceid").setName("请求ID")
                .setParamType(ParamTypeEnum.EVENT_PUBLIC_PARAM.getType()).setValueType(ParamValueTypeEnum.VARIABLE.getType())
                .setAppId(appId).setDescription("请求ID")
                .setCreateEmail("").setCreateName("")
                .setUpdateEmail("").setUpdateName("")
                .setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())), k -> new ArrayList<>())
                .add(new ParamValue().setCode(".*").setName("*").setAppId(appId).setDescription("请求traceID").setCreateName("").setCreateEmail("").setUpdateEmail("").setUpdateName("").setCreateTime(new Timestamp(System.currentTimeMillis())).setUpdateTime(new Timestamp(System.currentTimeMillis())));

        return result;
    }

    @Data
    @Accessors(chain = true)
    public static class EventParamData {
        private List<Param> eventParams = new ArrayList<>();   // 事件版本下所有参数

        private Version version;    // 运行时填充
        private List<ParamBind> paramBinds = new ArrayList<>(); // 运行时填充
    }
}
