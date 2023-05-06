package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.event.EventBuryPointCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.event.EventBuryPointEditParam;
import com.netease.hz.bdms.easyinsight.common.vo.event.*;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.UnDevelopedEventVO;
import com.netease.hz.bdms.easyinsight.service.facade.EventPoolFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求管理 事件埋点
 *
 * @author: xumengqiang
 * @date: 2022/1/5 10:57
 */
@Slf4j
@RequestMapping("/eis/v2/event/pool")
@RestController
public class EventPoolController {

    @Autowired
    EventPoolFacade eventPoolFacade;

    /**
     * 需求管理模块——获取某需求组下所有埋点事件列表
     *
     * @param reqPoolId 需求组ID
     * @return
     */
    @GetMapping("/list")
    public HttpResult getEventsPool(@RequestParam("reqPoolId") Long reqPoolId){
        List<UnDevelopedEventVO> reqPoolEventVOList = eventPoolFacade.getReqPoolEvents(reqPoolId);
        return HttpResult.success(reqPoolEventVOList);
    }

    /**
     * 需求管理模块——删除某需求组中的某个埋点事件
     *
     * @param reqPoolEventId 需求事件池表 主键ID
     * @return
     */
    @GetMapping("/delete")
    public HttpResult delete(@RequestParam("reqPoolEventId") Long reqPoolEventId){
        eventPoolFacade.delete(reqPoolEventId);
        return HttpResult.success();
    }


    /**
     * 需求管理模块——编辑某需求组中的某埋点事件
     *
     * @param eventBuryPointEditParam 更新信息
     * @return {@link Boolean}
     */
    @PostMapping("/edit")
    public HttpResult edit(@RequestBody @Validated EventBuryPointEditParam eventBuryPointEditParam){
        eventPoolFacade.edit(eventBuryPointEditParam);
        return HttpResult.success();
    }

    /**
     * 需求管理模块——创建事件埋点
     *
     * @param param
     * @return
     */
    @PostMapping("/create")
    public HttpResult create(@RequestBody @Validated EventBuryPointCreateParam param){
        eventPoolFacade.create(param);
        return HttpResult.success();
    }

    /**
     * 事件埋点池——新建事件埋点页面 获取终端信息
     *
     * @return
     */
    @GetMapping("/aggregate/get")
    public HttpResult getAggregateInfo(){
        EventAggregateInfoVO eventAggregateInfoVO = eventPoolFacade.getAggregateInfo();
        return HttpResult.success(eventAggregateInfoVO);
    }


    /**
     * 已上线事件埋点模块——获取已上线埋点事件列表
     *
     * @return
     */
    @GetMapping("/released/list")
    public HttpResult getReleasedEventBuryPoints(@RequestParam("releaseId") Long releaseId,
                                                 @RequestParam(value = "search", required = false) String search){
        List<EventBuryPointSimpleVO> eventBuryPointSimpleVOList = eventPoolFacade.list(releaseId, search);
        return HttpResult.success(eventBuryPointSimpleVOList);
    }

    /**
     * 已上线事件埋点模块——事件详情获取
     *
     * @param eventBuryPointId
     * @return
     */
    @GetMapping("/released/get")
    public HttpResult getEventBuryPointInfo(@RequestParam("eventBuryPointId") Long eventBuryPointId){
        EventBuryPointVO eventBuryPointVO = eventPoolFacade
                .getEventBuryPoint(eventBuryPointId);
        return HttpResult.success(eventBuryPointVO);
    }

    /**
     * 已上线事件埋点模块——获取埋点事件版本历史信息
     *
     * @param eventBuryPointId
     * @return
     */
    @GetMapping("/released/history/get")
    public HttpResult getReleasedHistory(@RequestParam("eventBuryPointId") Long eventBuryPointId){
        List<ReleasedEventBuryPointVO> releasedEventBuryPointVOS = eventPoolFacade
                .getReleaseHistory(eventBuryPointId);
        return HttpResult.success(releasedEventBuryPointVOS);
    }

    /**
     * 已上线事件埋点模块——聚合信息查询
     *
     * @return
     */
    @GetMapping("/released/aggregation/get")
    public HttpResult<ReleasedEventAggregationVO> getReleasedAggregationInfo(){
        ReleasedEventAggregationVO releasedAggregationInfo = eventPoolFacade
                .getReleasedAggregationInfo();
        return HttpResult.success(releasedAggregationInfo);
    }

    /**
     * 已上线事件埋点模块——聚合信息查询
     *
     * @return
     */
    @GetMapping("/released/aggregate/get")
    public HttpResult getReleasedAggregateInfo(){
        ReleasedEventAggregateVO releasedEventAggregateVO = eventPoolFacade
                .getReleasedAggregateInfo();
        return HttpResult.success(releasedEventAggregateVO);
    }

    /**
     * 已上线事件埋点模块——获取事件埋点样例数据
     *
     * @param eventBuryPointId 事件埋点ID
     * @return
     */
    @GetMapping("/released/example/data/get")
    public HttpResult getExampleData(@RequestParam("eventBuryPointId") Long eventBuryPointId){
        String exampleData = eventPoolFacade.getExampleData(eventBuryPointId);
        return HttpResult.success(exampleData);
    }

}
