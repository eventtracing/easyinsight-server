package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.domain.DomainCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.domain.DomainUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.DomainFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/eis/v1/domain")
@RestController
public class DomainController {

    @Autowired
    private DomainFacade domainFacade;

    @PostMapping("/create")
    @PermissionAction(requiredPermission = PermissionEnum.PLATFORM_DOMAIN_CREATE)
    public HttpResult createDomain(@RequestBody @Validated DomainCreateParam param) {
        return HttpResult.success(domainFacade.createDomain(param));
    }

    @PutMapping("/edit")
    @PermissionAction(requiredPermission = PermissionEnum.PLATFORM_DOMAIN_EDIT)
    public HttpResult updateDomain(@RequestBody @Validated DomainUpdateParam param) {
        return HttpResult.success(domainFacade.updateDomain(param));
    }

    @DeleteMapping("/delete")
    @PermissionAction(requiredPermission = PermissionEnum.PLATFORM_DOMAIN_DELETE)
    public HttpResult deleteDomain(@RequestParam(name = "id") Long id) {
        return HttpResult.success(domainFacade.deleteDomain(id));
    }

    @GetMapping("/list")
    @PermissionAction(requiredPermission = PermissionEnum.PLATFORM_MAN_READ)
    public HttpResult listDomains(@RequestParam(name = "currentPage") Integer currentPage,
                                  @RequestParam(name = "pageSize") Integer pageSize,
                                  @RequestParam(name = "orderBy", required = false) String orderBy,
                                  @RequestParam(name = "orderRule", required = false) String orderRule,
                                  @RequestParam(name = "search", required = false) String search) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
        return HttpResult.success(domainFacade.listDomains(search, pagingSortDTO));
    }

    @GetMapping("/get")
    @PermissionAction(requiredPermission = PermissionEnum.DOMAIN_INFO_READ)
    public HttpResult getDomain(@RequestParam(name = "id") Long id) {
        return HttpResult.success(domainFacade.getDomain(id));
    }

    @GetMapping("/getByEmail")
    public HttpResult getAppOfCurrentUser() {
        return HttpResult.success(domainFacade.getAppOfCurrentUser());
    }
}
