package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.terminal.TerminalCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.terminal.TerminalSearchParam;
import com.netease.hz.bdms.easyinsight.common.param.terminal.TerminalUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.TerminalFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/et/v1/terminal")
@RestController
public class TerminalController {

    @Resource
    private TerminalFacade terminalFacade;

    @PermissionAction(requiredPermission = PermissionEnum.TERMINAL_CREATE)
    @PostMapping("/create")
    public HttpResult createTerminal(@RequestBody @Validated TerminalCreateParam param) {
        return HttpResult.success(terminalFacade.createTerminal(param, false));
    }

    @PermissionAction(requiredPermission = PermissionEnum.TERMINAL_EDIT)
    @PutMapping("/edit")
    public HttpResult updateTerminal(@RequestBody @Validated TerminalUpdateParam param) {
        return HttpResult.success(terminalFacade.updateTerminal(param));
    }

    @PermissionAction(requiredPermission = PermissionEnum.TERMINAL_READ)
    @PostMapping("/list")
    public HttpResult listTerminal(@RequestBody @Validated TerminalSearchParam param) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(param.getCurrentPage(), param.getPageSize(),
                param.getOrderBy(), param.getOrderRule());
        return HttpResult.success(terminalFacade.listTerminals(param.getSearch(), param.getTerminalTypes(), pagingSortDTO));
    }

    @GetMapping("/get")
    public HttpResult getTerminal(@RequestParam(name = "id") Long id) {
        return HttpResult.success(terminalFacade.getTerminal(id));
    }

}
