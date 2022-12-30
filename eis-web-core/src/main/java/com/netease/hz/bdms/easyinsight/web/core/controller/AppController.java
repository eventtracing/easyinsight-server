package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.app.AppCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.app.AppUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.AppFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/eis/v1/app")
@RestController
public class AppController {
  @Autowired
  private AppFacade appFacade;

  @PostMapping("/create")
  @PermissionAction(requiredPermission = PermissionEnum.PRODUCT_CREATE)
  public HttpResult createApp(@RequestBody @Validated AppCreateParam param) {
    return HttpResult.success(appFacade.createApp(param));
  }

  @PutMapping("/edit")
  @PermissionAction(requiredPermission = PermissionEnum.PRODUCT_EDIT)
  public HttpResult updateApp(@RequestBody @Validated AppUpdateParam param) {
    return HttpResult.success(appFacade.updateApp(param));
  }

  @DeleteMapping("/delete")
  @PermissionAction(requiredPermission = PermissionEnum.PRODUCT_DELETE)
  public HttpResult deleteApp(@RequestParam(name = "id") Long id) {
    return HttpResult.success(appFacade.deleteApp(id));
  }

  @GetMapping("/list")
  @PermissionAction(requiredPermission = PermissionEnum.PRODUCT_READ)
  public HttpResult listApp(@RequestParam(name = "currentPage") Integer currentPage,
      @RequestParam(name = "pageSize") Integer pageSize,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      @RequestParam(name = "orderRule",required = false) String orderRule,
      @RequestParam(name = "search", required = false) String search) {
    PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
    return HttpResult.success(appFacade.listApps(search, pagingSortDTO));
  }

  @GetMapping("/get")
  @PermissionAction(requiredPermission = {PermissionEnum.PRODUCT_READ, PermissionEnum.PRODUCT_INFO})
  public HttpResult getApp(@RequestParam(name = "id") Long id) {
    return HttpResult.success(appFacade.getApp(id));
  }

}
