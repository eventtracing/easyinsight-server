package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.paramvalue.RuleTemplateSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.param.ObjBusinessPrivateParamCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.ObjBusinessPrivateParamUpdateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.ParamCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.ParamUpdateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.parampool.ParamPoolCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.parampool.ParamPoolUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.facade.ParamFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/et/v1/param")
@RestController
public class ParamController {

    @Autowired
    private ParamFacade paramFacade;


    @PostMapping("/create")  /* 创建对象标准私参 */
    public HttpResult createParam(@RequestBody @Validated ParamCreateParam param) {
        return HttpResult.success(paramFacade.createParam(param));
    }

    @PutMapping("/edit")
    public HttpResult updateParam(@RequestBody @Validated ParamUpdateParam param) {
        return HttpResult.success(paramFacade.updateParam(param));
    }

    @DeleteMapping("/delete")
    public HttpResult deleteParam(@RequestParam(name = "id") Long id) {
        return HttpResult.success(paramFacade.deleteParam(id));
    }

    @GetMapping("/list")
    public HttpResult listParam(@RequestParam(name = "currentPage") Integer currentPage,
                                @RequestParam(name = "pageSize") Integer pageSize,
                                @RequestParam(name = "orderBy", required = false) String orderBy,
                                @RequestParam(name = "orderRule", required = false) String orderRule,
                                @RequestParam(name = "search", required = false) String search,
                                @RequestParam(name = "paramType", required = false) Integer paramType,
                                @RequestParam(name = "valueTypes", required = false) List<Integer> valueTypes,
                                @RequestParam(name = "createEmails", required = false) List<String> createEmails) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
        return HttpResult
                .success(paramFacade.listParams(search, paramType, createEmails, valueTypes, pagingSortDTO));
    }

    @GetMapping("/get")
    public HttpResult getParam(@RequestParam(name = "id") Long id) {
        return HttpResult.success(paramFacade.getParam(id));
    }

    @GetMapping("/listAll")
    public HttpResult listAllParam(@RequestParam(name = "search", required = false) String search,
                                   @RequestParam(name = "paramTypes") List<Integer> paramTypes) {
        return HttpResult.success(paramFacade.listAllParams(search, paramTypes));
    }

    @GetMapping("/aggre")
    public HttpResult aggreParam(@RequestParam(name = "paramType") Integer paramType,
                                 @RequestParam(name = "aggreTypes", required = false) List<Integer> aggreTypes) {
        return HttpResult.success(paramFacade.aggreParam(paramType, aggreTypes));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_READ)
    @GetMapping("/obj_business_private/pool/list")
    public HttpResult listParamPoolItem(
            @RequestParam(name = "search", required = false) String search) {
        return HttpResult.success(paramFacade.listParamPoolItem(search));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_READ)
    @GetMapping("/obj_business_private/pool/list_with_param")
    public HttpResult listParamPoolItemWithDetail(
            @RequestParam(name = "search", required = false) String search) {
        return HttpResult.success(paramFacade.listParamPoolItemWithParam(search));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_CREATE)
    @PostMapping("/obj_business_private/pool/create")
    public HttpResult createParamPoolItem(@RequestBody @Validated ParamPoolCreateParam param) {
        return HttpResult.success(paramFacade.createParamPoolItem(param));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_EDIT)
    @PutMapping("/obj_business_private/pool/edit")
    public HttpResult editParamPoolItem(@RequestBody @Validated ParamPoolUpdateParam param) {
        return HttpResult.success(paramFacade.updateParamPoolItem(param));
    }


    @PermissionAction(requiredPermission = PermissionEnum.PARAM_DELETE)
    @DeleteMapping("/obj_business_private/pool/delete")
    public HttpResult deleteParamPoolItem(@RequestParam(name = "id", required = false) Long id) {
        return HttpResult.success(paramFacade.deleteParamPoolItem(id));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_MEANING_CREATE)
    @PostMapping("/obj_business_private/create")
    public HttpResult createObjBusinessPrivateParam(
            @RequestBody @Validated ObjBusinessPrivateParamCreateParam param) {
        ParamCreateParam paramCreateParam = BeanConvertUtils.convert(param, ParamCreateParam.class);
        paramCreateParam.setParamType(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType());
        return HttpResult.success(paramFacade.createObjBusinessPrivateParam(paramCreateParam));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_MEANING_EDIT)
    @PutMapping("/obj_business_private/edit")
    public HttpResult updateObjBusinessPrivateParam(
            @RequestBody @Validated ObjBusinessPrivateParamUpdateParam param) {
        ParamUpdateParam paramUpdateParam = BeanConvertUtils.convert(param, ParamUpdateParam.class);
        paramUpdateParam.setParamType(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType());
        return HttpResult.success(paramFacade.updateObjBusinessPrivateParam(paramUpdateParam));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_MEANING_DELETE)
    @DeleteMapping("/obj_business_private/delete")
    public HttpResult deleteObjBusinessPrivateParam(@RequestParam(name = "id") Long id) {
        return HttpResult.success(paramFacade.deleteParam(id));
    }

    @GetMapping("/obj_business_private/list")
    public HttpResult listObjBusinessPrivateParam(
            @RequestParam(name = "currentPage") Integer currentPage,
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            @RequestParam(name = "orderRule", required = false) String orderRule,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "valueTypes", required = false) List<Integer> valueTypes,
            @RequestParam(name = "createEmails", required = false) List<String> createEmails) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
        return HttpResult.success(
                paramFacade.listObjBusinessPrivateParams(code, createEmails, search, valueTypes, pagingSortDTO));
    }

    @GetMapping("/obj_business_private/get")
    public HttpResult getObjBusinessPrivateParam(@RequestParam(name = "id") Long id) {
        return HttpResult.success(paramFacade.getObjBusinessPrivateParams(id));
    }

    @GetMapping("/ruletemplate/get")
    public HttpResult getParamRuleTemplate() {
        return HttpResult.success(paramFacade.getParamRuleTemplate());
    }

    @PostMapping("/ruletemplate/add")
    public HttpResult addParamRuleTemplate(@RequestBody RuleTemplateSimpleDTO ruleTemplateSimpleDTO) {
        paramFacade.addParamRuleTemplate(ruleTemplateSimpleDTO);
        return HttpResult.success();
    }

    @GetMapping("/ruletemplate/delete")
    public HttpResult deleteParamRuleTemplate(@RequestParam Long id) {
        paramFacade.deleteParamRuleTemplate(id);
        return HttpResult.success();
    }


    @GetMapping("/bind/description/sync")
    public HttpResult syncParamBindDescription() {
        paramFacade.syncParamBindDescription();
        return HttpResult.success();
    }

}
