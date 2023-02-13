package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.*;
import com.netease.hz.bdms.easyinsight.common.vo.task.ReqTaskVO;
import com.netease.hz.bdms.easyinsight.common.vo.task.TaskProcessVO;
import com.netease.hz.bdms.easyinsight.service.facade.ObjectFacade;
import com.netease.hz.bdms.easyinsight.service.facade.ReqDesignFacade;
import com.netease.hz.bdms.easyinsight.service.facade.ReqPoolListPageFacade;
import com.netease.hz.bdms.easyinsight.service.facade.ReqTaskFacade;
import com.netease.hz.bdms.easyinsight.service.service.impl.LockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/eis/reqDesign")
public class ReqDesignController {

    @Autowired
    ReqDesignFacade reqDesignFacade;

    @Resource
    private ObjectFacade objectFacade;

    @Resource
    private LockService lockService;

    @Resource
    private ReqTaskFacade reqTaskFacade;

    @Resource
    private ReqPoolListPageFacade reqPoolListPageFacade;

    /**
     * 查看对象池spm血缘视图
     * @param reqPoolId
     * @param reqPoolType
     * @return
     */
    @GetMapping("/spmTrees")
    public HttpResult getSpmTrees(Long reqPoolId,
                                  Integer reqPoolType,
                                  boolean showCompleteTree,
                                  @RequestParam(name = "searchStr", required = false) String searchStr,
                                  @RequestParam(name = "showUnAssign", required = false) boolean showUnAssign){
        return HttpResult.success(reqDesignFacade.getSpmTrees(reqPoolId,reqPoolType,showCompleteTree, searchStr, showUnAssign));
    }

    /**
     * 删除任务
     */
    @GetMapping("/deleteProcess")
    public HttpResult getSpmTrees(Long processId) {
        reqDesignFacade.deleteProcess(processId);
        return HttpResult.success();
    }

    /**
     * 查看对象池列表
     * @param reqPoolId
     * @param searchStr
     * @param order 排序字段
     * @param rule 0-升序，1-降序
     * @return
     */
    @GetMapping("/objs")
    public HttpResult getReqObjs(@RequestParam(name = "reqPoolId") Long reqPoolId,
                                 @RequestParam(name = "searchStr", required = false) String searchStr,
                                 @RequestParam(name = "order", required = false) String order,
                                 @RequestParam(name = "rule", required = false) Boolean rule){
        return HttpResult.success(reqDesignFacade.getReqObjs(reqPoolId, searchStr, order, rule));
    }

    /**
     * 指派待办项给任务
     * @param assignVo
     * @return
     */
    @PostMapping("/assign")
    public HttpResult assign(@RequestBody AssignVO assignVo) {
        String lockKey = "lock_assign_" + assignVo.getReqPoolId();
        lockService.tryLock(lockKey);
        try {
            List<AssignEntityVO> assignEntities = assignVo.getAssignEntities();
            // 同步多端指派时。补充其他端的assignEntities
            if (assignVo.isSyncAllTerminal()) {
                assignEntities = handleSyncAssign(assignVo);
            }
            reqDesignFacade.assign(assignEntities, assignVo.getTaskIds());
            return HttpResult.success();
        } finally {
            lockService.releaseLock(lockKey);
        }
    }

    private List<AssignEntityVO> handleSyncAssign(AssignVO assignVo) {
        List<ReqSpmTreeVO> spmTrees = reqDesignFacade.getSpmTrees(assignVo.getReqPoolId(), ReqPoolTypeEnum.SPM_DEV.getReqPoolType(), false, null, false);
        if (CollectionUtils.isEmpty(spmTrees)) {
            throw new CommonException("需求池下无对象树，无法指派");
        }
        Set<Long> reqAssignIds = new HashSet<>();
        assignVo.getAssignEntities().forEach(assignEntityVO -> {
            if (assignEntityVO == null || !ReqPoolTypeEnum.SPM_DEV.getReqPoolType().equals(assignEntityVO.getPoolType())) {
                throw new CommonException("双端同步指派只支持SPM需求池");
            }
            reqAssignIds.add(assignEntityVO.getId());
        });

        Map<String, Set<Long>> assignIdsGroupBySpmByObjId = new HashMap<>();
        Set<String> toAssignSpmByObjIdSet = new HashSet<>();
        spmTrees.forEach(reqSpmTreeVO -> {
            if (reqSpmTreeVO == null) {
                return;
            }
            List<ReqSpmEntityVO> reqDevSpmEntities = reqSpmTreeVO.getReqDevSpmEntities();
            if (CollectionUtils.isEmpty(reqDevSpmEntities)) {
                return;
            }
            reqDevSpmEntities.forEach(reqSpmEntityVO -> {
                if (StringUtils.isEmpty(reqSpmEntityVO.getSpmByObjId())) {
                    return;
                }
                // 建立反向映射
                Set<Long> assignIds = assignIdsGroupBySpmByObjId.computeIfAbsent(reqSpmEntityVO.getSpmByObjId(), k -> new HashSet<>());
                assignIds.add(reqSpmEntityVO.getId());
                // 如果是请求指定的ID，记录下spmByObjId
                if (reqAssignIds.contains(reqSpmEntityVO.getId())) {
                    toAssignSpmByObjIdSet.add(reqSpmEntityVO.getSpmByObjId());
                }
            });
        });

        if (CollectionUtils.isEmpty(toAssignSpmByObjIdSet)) {
            throw new CommonException("未找到需要变更的spmByObjId");
        }

        Set<Long> preHandledAssignIds = new HashSet<>();
        toAssignSpmByObjIdSet.forEach(spmByObjId -> {
            Set<Long> ids = assignIdsGroupBySpmByObjId.get(spmByObjId);
            if (CollectionUtils.isNotEmpty(ids)) {
                preHandledAssignIds.addAll(ids);
            }
        });

        if (CollectionUtils.isEmpty(preHandledAssignIds)) {
            throw new CommonException("预处理后，没有需要指派的任务");
        }

        // 重新拼凑请求
        return preHandledAssignIds.stream().map(id -> {
            AssignEntityVO assignEntityVO = new AssignEntityVO();
            assignEntityVO.setId(id);
            assignEntityVO.setPoolType(ReqPoolTypeEnum.SPM_DEV.getReqPoolType());
            return assignEntityVO;
        }).collect(Collectors.toList());
    }

    /**
     * 取消指派
     * @param cancleAssignVO
     * @return
     */
    @PostMapping("/cancleAssign")
    public HttpResult cancelAssign(@RequestBody CancleAssignVO cancleAssignVO){
        reqDesignFacade.cancelAssign(cancleAssignVO.getIds());
        return HttpResult.success();
    }

    /**
     * 取消指派spm
     * @return
     */
    @PostMapping("/spm/cancleAssign")
    public HttpResult cancelSpmAssign(@RequestBody List<CancleSpmAssignVO> cancleAssignVOs){
        reqDesignFacade.cancelSpmAssign(cancleAssignVOs);
        return HttpResult.success();
    }

    /**
     * 取消指派
     * @param cancelAssignBatchVO
     * @return
     */
    @PostMapping("/cancleAssignBatch")
    public HttpResult cancelAssignBatch(@RequestBody CancelAssignBatchVO cancelAssignBatchVO){
        if (cancelAssignBatchVO == null || CollectionUtils.isEmpty(cancelAssignBatchVO.getOidAssignVOS())) {
            throw new CommonException("请勾选至少一个可取消指派的任务（开始、待审核）");
        }
        handleSyncCancel(cancelAssignBatchVO);
        reqDesignFacade.cancelAssignBatch(cancelAssignBatchVO);
        return HttpResult.success();
    }

    private void handleSyncCancel(CancelAssignBatchVO cancelAssignBatchVO) {
        if (!cancelAssignBatchVO.isSyncAllTerminal()) {
            return;
        }
        if (cancelAssignBatchVO.getReqPoolId() == null) {
            throw new CommonException("开启多端同步时，需要传入reqPoolId");
        }
        List<AssignAggreVO> assignAggre = reqDesignFacade.getAssignAggre(new ArrayList<>(0), cancelAssignBatchVO.getReqPoolId(), true);
        List<Long> allTaskIds = assignAggre.stream().flatMap(assignAggreVO -> {
            if (assignAggreVO == null || assignAggreVO.getTargetTasks() == null) {
                return Stream.empty();
            }
            return assignAggreVO.getTargetTasks().stream();
        }).map(AssignAggreVO.AssignTargetTask::getId).collect(Collectors.toList());

        List<OidAssignVO> newList = new ArrayList<>();
        cancelAssignBatchVO.getOidAssignVOS().forEach(oidAssignVO -> {
            if (EntityTypeEnum.OBJTRACKER.getType().equals(oidAssignVO.getType())) {
                allTaskIds.forEach(taskId -> {
                    OidAssignVO assignVO = new OidAssignVO();
                    assignVO.setSpmByObjId(oidAssignVO.getSpmByObjId());
                    assignVO.setEventBuryPointId(null);
                    assignVO.setTaskId(taskId);
                    assignVO.setType(EntityTypeEnum.OBJTRACKER.getType());
                    newList.add(assignVO);
                });
            }
        });
        cancelAssignBatchVO.setOidAssignVOS(newList);

    }

    @PostMapping("/assignAggre")
    public HttpResult assignAggre(@RequestBody AssignQueryVO assignQueryVO){
        return HttpResult.success(reqDesignFacade.getAssignAggre(assignQueryVO.getAssignEntities(),assignQueryVO.getReqPoolId(), assignQueryVO.isSyncAllTerminal()));
    }

    /**
     * 对象池删除对象
     * @param objId
     * @param reqPoolId
     * @return
     */
    @GetMapping("/objpool/delete")
    public HttpResult deleteObjPoolEntity(Long objId,Long reqPoolId){
        reqDesignFacade.deleteObjPoolEntity(objId,reqPoolId);
        return HttpResult.success();
    }

    /**
     * 事件埋点池删除待办项
     * @param id
     * @return
     */
    @GetMapping("/eventpool/delete")
    public HttpResult deleteEventPoolEntity(Long id){
        reqDesignFacade.deleteEventPoolEntity(id);
        return HttpResult.success();
    }

    @GetMapping("/tasks")
    public HttpResult tasks(Long reqPoolId){
        return HttpResult.success(reqDesignFacade.getTasks(reqPoolId));
    }

    /**
     * 增加了任务进度返回
     */
    @GetMapping("/tasks/full")
    public HttpResult fullTasks(Long reqPoolId) {
        List<ReqTaskVO> tasks = reqDesignFacade.getTasks(reqPoolId);
        if (CollectionUtils.isNotEmpty(tasks)) {
            List<Long> taskIds = tasks.stream().map(ReqTaskVO::getId).collect(Collectors.toList());
            Map<Long, TaskProcessVO> m = reqTaskFacade.getProcessVosBatch(taskIds);
            if (MapUtils.isNotEmpty(m)) {
                tasks.forEach(t -> t.setTaskProcess(m.get(t.getId())));
            }
        }
        return HttpResult.success(tasks);
    }

    /**
     * 基线变更编辑页
     * @param reqPoolId
     * @return
     */
    @GetMapping("/rebaseView")
    public HttpResult showRebase(Long reqPoolId){
        return HttpResult.success(reqDesignFacade.getRebaseEditVo(reqPoolId));
    }

    /**
     * 基线变更
     * @param rebaseVO
     * @return
     */
    @PostMapping("/rebase")
    public HttpResult rebase(@RequestBody RebaseVO rebaseVO){
        String lockKey = "lock_rebase_" + rebaseVO.getReqPoolId();
        lockService.tryLock(lockKey);
        try {
            reqDesignFacade.reBase(rebaseVO);
            return HttpResult.success();
        } finally {
            lockService.releaseLock(lockKey);
        }

    }

    /**
     * 基线变更历史
     * @param reqPoolId
     * @param terminalId
     * @return
     */
    @GetMapping("/rebaseHistory")
    public HttpResult getBaseChangeHitories(Long reqPoolId,Long terminalId){
        return HttpResult.success(reqDesignFacade.getBaseChangeHitories(reqPoolId,terminalId));
    }

    /**
     * 提醒有最新基线
     * @param reqPoolId
     * @return
     */
    @GetMapping("/rebaseNotice")
    public HttpResult newBaseReleaseNotice(Long reqPoolId){
        return HttpResult.success(reqDesignFacade.newBaseReleaseNotice(reqPoolId));
    }

    /**
     * 待办统计
     * @param id
     * @return
     */
    @GetMapping("/stat")
    public HttpResult getStatistic(Long id){
        ReqPoolStatisticVO result = reqDesignFacade.getStatistic(id);
        reqPoolListPageFacade.doRefreshCache(id, result);
        return HttpResult.success(result);
    }

    /**
     * 是否可编辑
     * @param reqPoolId
     * @return
     */
    @GetMapping("/editable")
    public HttpResult getDesignEditable(Long reqPoolId){
        return HttpResult.success(reqDesignFacade.isEditable(reqPoolId));
    }


    @PostMapping("/spm")
    public HttpResult getSpm(@RequestBody JSONObject jsonObject){
        return HttpResult.success(reqDesignFacade.getSpm(jsonObject));
    }
}
