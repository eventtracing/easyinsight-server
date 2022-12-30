package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.version.VersionCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.version.VersionSetParam;
import com.netease.hz.bdms.easyinsight.service.facade.VersionFacade;
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

@Slf4j
@RequestMapping("/et/v1/version")
@RestController
public class VersionController {
  @Autowired
  private VersionFacade versionFacade;

  /**
   * 在 事件类型 的列表操作里点击 “参数管理”
   * 在 终端类型 的列表操作里点击 “参数管理”
   * 都会调用这个接口,因此添加了
   * 注解 @PermissionAction(requiredPermission = {PermissionEnum.EVENT_TYPE_PARAM_MAN,PermissionEnum.TERMINAL_PARAM_MAN})
   *
   * @param search
   * @param entityId
   * @param entityType
   * @return
   */
  @PermissionAction(requiredPermission = {PermissionEnum.EVENT_TYPE_PARAM_MAN, PermissionEnum.TERMINAL_PARAM_MAN})
  @GetMapping("/list")
  public HttpResult listVersions(@RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "entityId") Long entityId,
                                 @RequestParam(name = "entityType") Integer entityType) {
    return HttpResult.success(versionFacade.listVersions(entityId, entityType, search));
  }

  @PutMapping("/setusing")
  public HttpResult setVersion(@RequestBody @Validated VersionSetParam param) {
    versionFacade.setVersion(param);
    return HttpResult.success();
  }

  @PostMapping("/create")
  public HttpResult createVersion(@RequestBody @Validated VersionCreateParam param) {
    return HttpResult.success(versionFacade.createVersion(param));
  }

  @DeleteMapping("/delete")
  public HttpResult deleteVersion(@RequestParam(value = "versionId") Long versionId,
      @RequestParam(name = "entityId") Long entityId,
      @RequestParam(name = "entityType") Integer entityType) {
    versionFacade.deleteVersion(versionId, entityId, entityType);
    return HttpResult.success();
  }
}
