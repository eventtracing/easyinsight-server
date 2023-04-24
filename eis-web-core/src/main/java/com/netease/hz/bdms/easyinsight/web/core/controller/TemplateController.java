package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.template.TemplateCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.template.TerminalUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.TemplateFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/et/v1/template")
@RestController
public class TemplateController {

    @Resource
    private TemplateFacade templateFacade;

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_TEMPLATE_CREATE)
    @PostMapping("/create")
    public HttpResult createTemplate(@RequestBody @Validated TemplateCreateParam param) {
        return HttpResult.success(templateFacade.createTemplate(param));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_TEMPLATE_EDIT)
    @PutMapping("/edit")
    public HttpResult updateTemplate(@RequestBody @Validated TerminalUpdateParam param) {
        return HttpResult.success(templateFacade.updateTemplate(param));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_TEMPLATE_DELETE)
    @DeleteMapping("/delete")
    public HttpResult deleteTemplate(@RequestParam(name = "id") Long id) {
        return HttpResult.success(templateFacade.deleteTemplate(id));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_TEMPLATE_READ)
    @GetMapping("/list")
    public HttpResult listTemplate(@RequestParam(name = "currentPage") Integer currentPage, @RequestParam(name = "pageSize") Integer pageSize, @RequestParam(name = "orderBy", required = false) String orderBy, @RequestParam(name = "orderRule", required = false) String orderRule, @RequestParam(name = "search", required = false) String search) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
        return HttpResult.success(templateFacade.listTemplates(search, pagingSortDTO));
    }

    @PermissionAction(requiredPermission = PermissionEnum.PARAM_TEMPLATE_COPY)
    @GetMapping("/get")
    public HttpResult getTemplate(@RequestParam(name = "id", required = false) Long id) {
        if(id != null && id > 0) {
            return HttpResult.success(templateFacade.getTemplate(id));
        }else {
            //返回默认模版
            Long appId = EtContext.get(ContextConstant.APP_ID);
            return HttpResult.success(templateFacade.getDefaultTemplate(appId));
        }
    }
}
