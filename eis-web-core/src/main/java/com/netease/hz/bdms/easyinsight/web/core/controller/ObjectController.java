package com.netease.hz.bdms.easyinsight.web.core.controller;


import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectBasicDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectTrackerInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.TerminalCodeTypeEum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.obj.*;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.obj.*;
import com.netease.hz.bdms.easyinsight.common.vo.release.BaseReleaseVO;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.facade.ObjectFacade;
import com.netease.hz.bdms.easyinsight.service.facade.ReqDesignFacade;
import com.netease.hz.bdms.easyinsight.service.helper.ObjectHelper;
import com.netease.hz.bdms.easyinsight.service.needimpl.TerminalCodeService;
import com.netease.hz.bdms.easyinsight.service.service.impl.LockService;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ??????????????????????????????????????????????????????????????????
 *
 * @author: xumengqiang
 * @date: 2021/12/8 15:53
 */
@Slf4j
@RequestMapping("/eis/v2/obj")
@RestController
public class ObjectController {
    @Resource
    private ObjectFacade objectFacade;

    @Resource
    private ObjectHelper objectHelper;

    @Resource
    private LockService lockService;

    @Resource
    private TerminalCodeService terminalCodeService;

    @Resource
    private ReqDesignFacade reqDesignFacade;

    /**
     * ????????????
     *
     * @param objectCreateParam ????????????????????????????????????
     * @return
     */
    @PostMapping("/create")
    @PermissionAction(requiredPermission = {PermissionEnum.OBJ_CREATE, PermissionEnum.REQ_OBJ_CREATE})
    public HttpResult createObject(@RequestBody @Validated ObjectCreateParam objectCreateParam) {
        if (objectCreateParam == null || objectCreateParam.getReqPoolId() == null) {
            throw new CommonException("????????????");
        }
        String lockKey = "lock_createObject_" + objectCreateParam.getReqPoolId();
        lockService.tryLock(lockKey);
        try {
            objectFacade.createObject(objectCreateParam);
        } finally {
            lockService.releaseLock(lockKey);
        }
        return HttpResult.success();
    }


    /**
     * ???????????????????????????????????????????????????
     *
     * @param objId ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    @GetMapping("/change/check")
    public HttpResult changeCheck(@RequestParam("objId") Long objId,
                                  @RequestParam("reqPoolId") Long reqPoolId){
        Boolean changeFlag = !objectHelper.isChanged(objId, reqPoolId);
        return HttpResult.success(changeFlag);
    }

    /**
     * ????????????
     *
     * @param objectChangeParam ????????????????????????
     * @return
     */
    @PostMapping("/change")
    @PermissionAction(requiredPermission = PermissionEnum.OBJ_EDIT)
    public HttpResult changeObject(@RequestBody @Validated ObjectChangeParam objectChangeParam){
        if (objectChangeParam == null || objectChangeParam.getReqPoolId() == null) {
            throw new CommonException("????????????");
        }
        ObjDetailsVO objDetails = objectFacade.getObjectByHistory(objectChangeParam.getId(), objectChangeParam.getHistoryId(), objectChangeParam.getReqPoolId());
        String lockKey = "lock_changeObject_" + objectChangeParam.getReqPoolId();
        lockService.tryLock(lockKey);
        try {
            objectFacade.changeObject(objectChangeParam, objDetails);
        } finally {
            lockService.releaseLock(lockKey);
        }
        return HttpResult.success();
    }

    /**
     * ??????????????????
     *
     * @param objectBatchChangeParam ????????????????????????
     * @return
     */
    @PostMapping("/batch/change")
    @PermissionAction(requiredPermission = PermissionEnum.OBJ_EDIT)
    public HttpResult batchChangeObject(@RequestBody ObjectBatchChangeParam objectBatchChangeParam){

        List<ObjectBatchParam> objectBatchParams = objectBatchChangeParam.getObjectBatchParams();

        for(ObjectBatchParam objectBatchParam : objectBatchParams) {
            ObjDetailsVO objDetails = objectFacade.getObjectForChange(objectBatchParam.getObjId(), objectBatchParam.getHistoryId());
            ObjectChangeParam objectChangeParam = new ObjectChangeParam();
            objectChangeParam.setId(objDetails.getId())
                    .setReqPoolId(objectBatchChangeParam.getReqPoolId())
                    .setConsistency(objDetails.getConsistency())
                    .setDescription(objDetails.getDescription())
                    .setHistoryId(objDetails.getHistoryId())
                    .setImgUrls(objDetails.getImgUrls())
                    .setOid(objDetails.getOid())
                    .setPriority(objDetails.getPriority())
                    .setHistoryId(objDetails.getHistoryId())
                    .setTagIds(objDetails.getTags().stream().map(TagSimpleDTO::getId).collect(Collectors.toList()));

            List<ObjectTrackerChangeParam> paramTrackers= new ArrayList<>();
            List<ObjectTrackerInfoDTO> objectTrackers = objDetails.getTrackers();
            for(ObjectTrackerInfoDTO objectTrackerInfoDTO : objectTrackers) {
                ObjectTrackerChangeParam objectTrackerChangeParam = new ObjectTrackerChangeParam();
                objectTrackerChangeParam.setEventIds(objectTrackerInfoDTO.getEvents().stream().map(EventSimpleDTO::getId).collect(Collectors.toList()))
                        .setEventParamVersionIdMap(objectTrackerInfoDTO.getEventParamVersionIdMap())
                        .setId(objectTrackerInfoDTO.getId())
                        .setParentObjs(objectTrackerInfoDTO.getParentObjects().stream().map(ObjectBasicDTO::getId).collect(Collectors.toList()))
                        .setPubParamPackageId(objectTrackerInfoDTO.getPubParamPackageId())
                        .setTerminalId(objectTrackerInfoDTO.getTerminal().getId());
                // 4.2 ????????????????????????????????????
                List<ParamBindItemDTO> paramItems = objectTrackerInfoDTO.getPrivateParam();
                List<ParamBindItermParam> paramBinds = new ArrayList<>();
                for (ParamBindItemDTO paramItem : paramItems) {
                    ParamBindItermParam paramBind = new ParamBindItermParam();
                    paramBind.setParamId(paramItem.getId())
                            .setDescription(paramItem.getDescription())
                            .setIsEncode(paramItem.getIsEncode())
                            .setNeedTest(paramItem.getNeedTest())
                            .setMust(paramItem.getMust())
                            .setNotEmpty(paramItem.getNotEmpty())
                            .setValues(paramItem.getValues().stream().filter(value -> paramItem.getSelectedValues().contains(value.getId())).map(ParamValueSimpleDTO::getId).collect(Collectors.toList()));
                    paramBinds.add(paramBind);
                }
                objectTrackerChangeParam.setParamBinds(paramBinds);
                paramTrackers.add(objectTrackerChangeParam);
            }
            objectChangeParam.setTrackers(paramTrackers);

            objectFacade.changeObject(objectChangeParam, objDetails);
        }
        return HttpResult.success();
    }

    /**
     * ????????????????????????
     *
     * @param objectChangeParam ????????????????????????
     * @return
     */
    @PostMapping("/basic/edit")
    @PermissionAction(requiredPermission = PermissionEnum.OBJ_EDIT)
    public HttpResult editBasicObject(@RequestBody ObjectBasicChangeParam objectChangeParam){
        objectFacade.editObjectBasic(objectChangeParam);
        return HttpResult.success();
    }

    /**
     * ????????????
     *
     * @param objectEditParam ????????????????????????
     * @return
     */
    @PostMapping("/edit")
    @PermissionAction(requiredPermission = PermissionEnum.OBJ_EDIT)
    public HttpResult editObject(@RequestBody @Validated ObjectEditParam objectEditParam){
        if (objectEditParam == null || objectEditParam.getReqPoolId() == null) {
            throw new CommonException("????????????");
        }
        String lockKey = "lock_editObject_" + objectEditParam.getReqPoolId();
        lockService.tryLock(lockKey);
        boolean isResolveConflict;
        try {
            isResolveConflict = objectFacade.editObject(objectEditParam);
        } finally {
            lockService.releaseLock(lockKey);
        }
        if (isResolveConflict) {
            reqDesignFacade.rebaseReqPoolToLatest(objectEditParam.getReqPoolId(), null);
        }
        return HttpResult.success();
    }

    /**
     * ???????????????????????? (???????????????????????????????????????????????????????????????)
     *
     * @param objId ??????ID
     * @param historyId ????????????ID
     * @return
     */
    @GetMapping(value="/get", params={"objId", "historyId","reqPoolId"})
    public HttpResult getObjectInfoByHistory(@RequestParam("objId") Long objId,
                                             @RequestParam("historyId") Long historyId,
                                             @RequestParam("reqPoolId") Long reqPoolId){
        ObjDetailsVO objDetails = objectFacade.getObjectByHistory(objId, historyId, reqPoolId);
        return HttpResult.success(objDetails);
    }

    /**
     * ???????????????????????? (???????????????????????????????????????????????????????????????)?????????diff??????
     *
     * @param objId ??????ID
     * @param historyId ????????????ID
     * @return
     */
    @GetMapping(value="/getByReqPoolIdAndHistoryIdWithDiff", params={"objId", "historyId","reqPoolId"})
    public HttpResult getByReqPoolIdAndHistoryIdWithDiff(@RequestParam("objId") Long objId,
                                             @RequestParam("historyId") Long historyId,
                                             @RequestParam("reqPoolId") Long reqPoolId){
        ObjDetailsVO objDetails = objectFacade.getObjectByHistory(objId, historyId, reqPoolId, true);
        return HttpResult.success(objDetails);
    }

    /**
     * ?????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param objId
     * @param historyId
     * @return
     */
    @GetMapping(value="/get/for/change", params={"objId", "historyId"})
    public HttpResult getObjectForChange(@RequestParam("objId") Long objId,
                                         @RequestParam("historyId") Long historyId){
        ObjDetailsVO objDetails = objectFacade.getObjectForChange(objId, historyId);
        return HttpResult.success(objDetails);
    }

    /**
     * ???????????????????????? (???????????????????????????????????????????????????????????????)
     *
     * @param objId ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    @GetMapping(value="/get", params={"objId", "reqPoolId"})
    public HttpResult getObjectInfoByReqPoolId(@RequestParam("objId") Long objId,
                                               @RequestParam("reqPoolId") Long reqPoolId){
        ObjDetailsVO objDetails = objectFacade.getObjectByReqPoolId(objId, reqPoolId);
        return HttpResult.success(objDetails);
    }

    /**
     * ???????????????????????????????????????????????????release?????????????????????
     *
     * @param objId     ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    @GetMapping(value = "/getBaseLineDiff")
    public HttpResult<ObjDetailsVO> getBaseLineDiff(@RequestParam("objId") Long objId, @RequestParam("reqPoolId") Long reqPoolId) {
        ObjDetailsVO result = objectFacade.getBaseLineDiff(objId, reqPoolId);
        return HttpResult.success(result);
    }

    /**
     * ???????????????????????????????????? (???????????????????????????????????????????????????????????????)?????????diff??????
     *
     * @param objId ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    @GetMapping(value="/getByReqPoolIdWithDiff")
    public HttpResult getObjectInfoByReqPoolIdWithDiff(@RequestParam("objId") Long objId, @RequestParam("reqPoolId") Long reqPoolId){
        ObjDetailsVO objDetails = objectFacade.getObjectByReqPoolIdWithDiff(objId, reqPoolId);
        return HttpResult.success(objDetails);
    }

    /**
     * ????????????????????????
     *
     * @param objId ??????ID
     * @param trackerId ??????ID
     * @return
     */
    @GetMapping(value="/get", params={"objId", "trackerId"})
    public HttpResult getObjInfoByTrackerId(@RequestParam("objId") Long objId,
                                            @RequestParam("trackerId") Long trackerId){
        ObjDetailsVO objDetails = objectFacade.getObjectByTrackerId(objId, trackerId);
        return HttpResult.success(objDetails);
    }


    /**
     * ?????????????????????????????????????????????
     *
     * @param terminalIds ??????ID
     * @param reqPoolId ?????????ID
     * @return
     */
    @GetMapping("/candidate/parents/get")
    public HttpResult getCandidateObjects(@RequestParam("terminalIds") List<Long> terminalIds,
                                          @RequestParam("reqPoolId") Long reqPoolId){
        List<ObjectBasic> candidateParentObjects = objectFacade
                .getCandidateParentObjects(terminalIds, reqPoolId);
        return HttpResult.success(candidateParentObjects);
    }


    /**
     * ???????????????????????????
     *
     * @param objId ??????ID
     * @param terminalId ???ID
     * @param releaseId ???????????????ID
     * @return
     */
    @GetMapping("/example/data/get")
    public HttpResult getExampleData(@RequestParam("objId") Long objId,
                                     @RequestParam("terminalId") Long terminalId,
                                     @RequestParam("releaseId") Long releaseId){
        List<Map<String, Object>> exampleData = objectFacade
                .getObjExampleData(objId, terminalId, releaseId);
        return HttpResult.success(exampleData);
    }

    /**
     * ?????????????????????????????????  // todo ???????????? ????????????
     *
     * @return
     */
    @GetMapping("/list")
    public HttpResult getReleasedObjectList(@RequestParam("releasedId") Long releasedId,
                                            @RequestParam(value = "type", required = false)Integer type,
                                            @RequestParam(value = "tagIds", required = false)List<Long> tagIds,
                                            @RequestParam(value = "search", required = false)String search,
                                            @RequestParam(value = "orderBy", defaultValue = "createTime")String orderBy,
                                            @RequestParam(value = "orderRule",  defaultValue = "descend")String orderRule){
        ObjTreeVO objTreeVO = objectFacade.getReleasedObjTree(
                releasedId, type, tagIds, search, orderBy, orderRule);
        return HttpResult.success(objTreeVO);
    }

    /**
     * ?????????????????????????????????
     *
     * @return
     */
    @GetMapping("/released/graph/get")
    public HttpResult getReleasedObjLineageGraph(@RequestParam("releaseId") Long releaseId,
                                                 @RequestParam("objId")Long objId){
        ObjLineageGraphVO objLineageGraphVO = objectFacade.getReleasedObjLineageGraph(releaseId, objId);
        return HttpResult.success(objLineageGraphVO);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param reqPoolId ?????????ID
     * @param objId ??????ID
     * @return
     */
    @GetMapping("/req/graph/get")
    public HttpResult getReqPoolObjLineageGraph(@RequestParam("terminalId")Long terminalId,
                                                @RequestParam("reqPoolId")Long reqPoolId,
                                                @RequestParam("objId")Long objId){
        ObjLineageGraphVO objLineageGraphVO = objectFacade.getReqPoolObjLineageGraph(terminalId, reqPoolId, objId);
        return HttpResult.success(objLineageGraphVO);
    }


    /**
     * ?????? ??????????????????????????? ???????????????
     *
     * @return
     */
    @GetMapping("/aggregate")
    public HttpResult getAggregateInfo(){
        ObjAggregateVO objAggregateVO = objectFacade.getObjAggregateInfo();
        return HttpResult.success(objAggregateVO);
    }

    /**
     * ???????????????????????? ???????????????
     *
     * @return
     */
    @GetMapping("/cascade/aggregate")
    public HttpResult getObjCascadeAggregate(@RequestParam("objId") Long objId,
                                             @RequestParam("reqPoolId") Long reqPoolId){
        ObjCascadeAggregateVO objCascadeAggregateVO = objectFacade
                .getObjCascadeAggregateInfo(objId, reqPoolId);
        return HttpResult.success(objCascadeAggregateVO);
    }

    /**
     * ??????????????????????????????
     *
     * @param objId ??????ID
     * @param terminalId ???ID
     * @return
     */
    @GetMapping("/release/history/get")
    public HttpResult getObjReleaseHistory(@RequestParam("terminalId")Long terminalId,
                                           @RequestParam("objId")Long objId){

        List<ObjReleaseVO> objReleaseVOList = objectFacade.getObjReleasedHistory(terminalId, objId);
        return HttpResult.success(objReleaseVOList);
    }

    /**
     * ??????/?????????????????????????????????????????????????????????
     *
     * @param terminalId ???ID
     * @param reqPoolId ?????????ID
     * @param search ????????????
     * @return
     */
    @GetMapping("/base/tree")
    public HttpResult getBaseTree(@RequestParam("terminalId") Long terminalId,
                                  @RequestParam("reqPoolId") Long reqPoolId,
                                  @RequestParam(value = "search", required = false) String search,
                                  @RequestParam(value = "tagSearch", required = false) String tagSearch){
        ObjTreeVO objTreeVO = objectFacade.getBaseTree(terminalId, reqPoolId, search, tagSearch);
        return HttpResult.success(objTreeVO);
    }

    /**
     * ??????????????????????????????/??????????????????????????????
     *
     * @param reqPoolId ?????????ID
     * @return
     */
    @GetMapping("/base/version/get")
    public HttpResult getBaseRelease(@RequestParam("reqPoolId") Long reqPoolId){
        List<BaseReleaseVO> baseReleaseVOS = objectFacade.getBaseReleaseVO(reqPoolId);
        return HttpResult.success(baseReleaseVOS);
    }

    /**
     *
     * @param reqPoolId
     * @param terminalId
     * @return
     */
    @GetMapping("/check/loop")
    public HttpResult checkLoop(@RequestParam("reqPoolId") Long reqPoolId,
                                @RequestParam("terminalId") Long terminalId){
        boolean isLoop = objectFacade.checkLoop(reqPoolId, terminalId);
        return HttpResult.success(isLoop);
    }

    /**
     * subObjTypes??????
     */
    @GetMapping("/objSubTypes")
    public HttpResult subObjTypes() {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        return HttpResult.success(objectFacade.objSubTypes());
    }

    /**
     * ???????????????????????????????????????
     */
    @GetMapping("/code-copy/types")
    public HttpResult getCodeCopyTypes(){
        TerminalCodeTypeEum[] values = TerminalCodeTypeEum.values();
        Map<Integer, String> m = new HashMap<>();
        for (TerminalCodeTypeEum value : values) {
            m.put(value.getResult(), value.getDesc());
        }
        return  HttpResult.success(JsonUtils.toJson(m));
    }

    /**
     * ??????????????????
     */
    @GetMapping("/code-copy/get")
    public HttpResult getCodeCopyTypes(@RequestParam("objId") Long objId,
                                       @RequestParam(value = "historyId", required = false) Long historyId,
                                       @RequestParam("reqPoolId") Long reqPoolId,
                                       @RequestParam(value = "terminalId", required = false) Long terminalId,
                                       @RequestParam("type") Integer type) {
        TerminalCodeTypeEum terminalCodeTypeEum = TerminalCodeTypeEum.fromValue(type);
        if (terminalCodeTypeEum == null) {
            return HttpResult.error(ResponseCodeConstant.PARAM_INVALID, "type??????");
        }

        ObjDetailsVO objDetails;
        if (historyId == null) {
            objDetails = objectFacade.getObjectByReqPoolId(objId, reqPoolId);
        } else {
            objDetails = objectFacade.getObjectByHistory(objId, historyId, reqPoolId);
        }
        if (objDetails == null || objDetails.getTrackers() == null) {
            return HttpResult.error(ResponseCodeConstant.PARAM_INVALID, "???????????????");
        }
        if (terminalId != null) {
            objDetails.getTrackers().removeIf(o -> o.getTerminal() == null || o.getTerminal().getId() == null || !o.getTerminal().getId().equals(terminalId));
            if (CollectionUtils.isEmpty(objDetails.getTrackers())) {
                return HttpResult.error(ResponseCodeConstant.PARAM_INVALID, "??????????????????, terminalId="  + terminalId);
            }
        } else {
            TerminalSimpleDTO terminalSimpleDTO = chooseTerminal(terminalCodeTypeEum, objDetails);
            objDetails.getTrackers().removeIf(o -> o.getTerminal() == null || o.getTerminal().getName() == null || !o.getTerminal().getName().equals(terminalSimpleDTO.getName()));
            if (CollectionUtils.isEmpty(objDetails.getTrackers())) {
                return HttpResult.error(ResponseCodeConstant.PARAM_INVALID, "??????????????????, terminalId="  + terminalId);
            }
        }
        String generatedCode = terminalCodeService.getCode(terminalCodeTypeEum, objDetails);
        return HttpResult.success(generatedCode);
    }

    private TerminalSimpleDTO chooseTerminal(TerminalCodeTypeEum terminalCodeType, ObjDetailsVO objDetails) {
        List<TerminalSimpleDTO> terminals = objDetails.getTrackers().stream()
                .map(ObjectTrackerInfoDTO::getTerminal)
                .collect(Collectors.toList());
        List<TerminalSimpleDTO> androidTerminals = new ArrayList<>();
        List<TerminalSimpleDTO> iPhoneTerminals = new ArrayList<>();
        List<TerminalSimpleDTO> webTerminals = new ArrayList<>();
        List<TerminalSimpleDTO> rnTerminals = new ArrayList<>();
        List<TerminalSimpleDTO> otherTerminals = new ArrayList<>();
        terminals.forEach(t -> {
            if ("android".equalsIgnoreCase(t.getName())) {
                androidTerminals.add(t);
                rnTerminals.add(t);
            } else if ("iphone".equalsIgnoreCase(t.getName())) {
                iPhoneTerminals.add(t);
                rnTerminals.add(t);
            } else if ("web".equalsIgnoreCase(t.getName())) {
                webTerminals.add(t);
            } else {
                otherTerminals.add(t);
            }
        });
        if (terminalCodeType == TerminalCodeTypeEum.ANDROID) {
            if (CollectionUtils.isNotEmpty(androidTerminals)) {
                return androidTerminals.get(0);
            }
            // ??????TV???
            if (CollectionUtils.isNotEmpty(otherTerminals)) {
                return otherTerminals.get(0);
            }
            throw new CommonException("??????????????????????????????????????????");
        }
        if (terminalCodeType == TerminalCodeTypeEum.IPHONE) {
            if (CollectionUtils.isNotEmpty(iPhoneTerminals)) {
                return iPhoneTerminals.get(0);
            }
            throw new CommonException("?????????????????????iPhone???????????????");
        }
        if (terminalCodeType == TerminalCodeTypeEum.WEB_DAWN_DIV || terminalCodeType == TerminalCodeTypeEum.WEB_DIV) {
            if (CollectionUtils.isNotEmpty(webTerminals)) {
                return webTerminals.get(0);
            }
            throw new CommonException("?????????????????????web???????????????");
        }
        if (terminalCodeType == TerminalCodeTypeEum.RN_DAWN_VIEW || terminalCodeType == TerminalCodeTypeEum.RN_VIEW) {
            if (CollectionUtils.isNotEmpty(androidTerminals)) {
                return androidTerminals.get(0);
            }
            if (CollectionUtils.isNotEmpty(iPhoneTerminals)) {
                return iPhoneTerminals.get(0);
            }
            throw new CommonException("??????????????????????????????iPhone???????????????");
        }
        throw new CommonException("TerminalCodeTypeEum ??????");
    }

}
