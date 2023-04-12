package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmCheckInfoDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.spm.*;
import com.netease.hz.bdms.easyinsight.common.vo.PageResultVO;
import com.netease.hz.bdms.easyinsight.common.vo.spm.SpmMapInfoListQueryVO;
import com.netease.hz.bdms.easyinsight.common.vo.spm.SpmMapInfoVO;
import com.netease.hz.bdms.easyinsight.service.facade.SpmFacade;
import com.netease.hz.bdms.easyinsight.service.helper.SpmMapHelper;
import com.netease.hz.bdms.easyinsight.service.service.ArtificialSpmInfoService;
import com.netease.hz.bdms.easyinsight.service.service.SpmInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/et/v1/spm")
public class SpmController {

    @Autowired
    SpmFacade spmFacade;

    @Autowired
    SpmMapHelper spmMapHelper;

    @Autowired
    ArtificialSpmInfoService artificialSpmInfoService;

    @Autowired
    SpmInfoService spmInfoService;

    /**
     * 创建新旧SPM映射
     *
     * @return
     */
    @PostMapping("/relation/create")
    public HttpResult createSpmMapRelations(@RequestBody SpmMapItemCreateParam spmMapItemCreateParam) {
        spmFacade.createSpmMapRelation(spmMapItemCreateParam);
        return HttpResult.success();
    }

    /**
     * 批量绑定标签
     *
     * @return
     */
    @PostMapping("/tag/bind")
    public HttpResult createSpmTagBinding(@RequestBody SpmTagBindsParam spmTagBindsParam){
        spmFacade.createSpmTagBinding(spmTagBindsParam);
        return HttpResult.success();
    }

    /**
     * 批量更新 SPM映射状态信息
     *
     * @return
     */
    @PostMapping("/info/status/update")
    public HttpResult updateSpmMapStatus(@RequestBody @Valid  SpmMapStatusUpdateParam spmMapStatusUpdateParam){
        spmFacade.updateSpmMapStatus(spmMapStatusUpdateParam);
        return HttpResult.success();
    }

    /**
     * 批量更新 SPM映射生效版本信息
     *
     * @return
     */
    @PostMapping("/info/version/update")
    public HttpResult updateSpmMapVersion(@RequestBody SpmMapVersionUpdateParam spmMapVersionUpdateParam){
        spmFacade.updateSpmVersion(spmMapVersionUpdateParam);
        return HttpResult.success();
    }

    /**
     * 更新 SPM映射备注信息
     *
     * @return
     */
    @PostMapping("/info/note/update")
    public HttpResult updateSpmMapNote(@RequestBody SpmMapNoteUpdateParam spmMapNoteUpdateParam){
        spmFacade.updateSpmMapNote(spmMapNoteUpdateParam);
        return HttpResult.success();
    }

    /**
     * SPM 页面展示
     *
     * @return
     */
    @PostMapping("/info/list")
    @PermissionAction(requiredPermission = PermissionEnum.SPM_READ)
    public HttpResult listSpmMapInfo(@RequestBody SpmMapInfoListQueryVO param){
        PageResultVO<SpmMapInfoVO> page= spmFacade.listWithCache(param);
        return HttpResult.success(page);
    }

    /**
     * SPM 搜索
     *
     * @return
     */
    @PostMapping("/info/search")
    @PermissionAction(requiredPermission = PermissionEnum.SPM_READ)
    public HttpResult searchSpmMapInfo(@RequestBody SpmMapInfoListQueryVO param){
        if(param == null || StringUtils.isBlank(param.getSpmOrName())){
            return HttpResult.success();
        }
        List<SpmMapInfoVO> spmMapInfoVOS= spmFacade.search(param);
        return HttpResult.success(new PageResultVO<>(spmMapInfoVOS, param.getPageSize(), param.getPageNum()));
    }

    /**
     * 同步spm映射信息
     *
     * @return
     */
    @GetMapping("/info/synchronize")
    public HttpResult synchronize(){
        spmMapHelper.syncSpmMapInfo();
        return HttpResult.success();
    }

    @GetMapping("/info/test")
    public HttpResult test(){
        artificialSpmInfoService.deleteBySource(-1);
        spmInfoService.deleteBySource(-1);
        return HttpResult.success();
    }

    /**
     * 转化spm映射信息
     *
     * @return
     */
    @GetMapping("/info/transform")
    public HttpResult transform(){
        spmMapHelper.transform();
        return HttpResult.success();
    }

    /**
     * SPM 编辑(仅支持手动添加的spm)
     *
     * @return
     */
    @PostMapping("/info/edit")
    @PermissionAction(requiredPermission = PermissionEnum.SPM_READ)
    public HttpResult editSpmInfo(@RequestBody SpmInfoUpdateParam spmInfoUpdateParam){
        spmFacade.createAndupdateSpmInfo(spmInfoUpdateParam);
        return HttpResult.success();
    }

    /**
     * SPM 校验
     * @return
     */
    @PostMapping("/info/check")
    @PermissionAction(requiredPermission = PermissionEnum.SPM_READ)
    public HttpResult checkSpmInfo(@RequestBody String spmInfo){
        SpmCheckInfoDTO spmCheckInfoDTO = spmFacade.checkSpmInfo(spmInfo);
        return HttpResult.success(spmCheckInfoDTO);
    }


}
