package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.param.paramvalue.ParamValueUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.ParamValueFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/et/v1/paramvalue")
@RestController
public class ParamValueController {

  @Autowired
  private ParamValueFacade paramValueFacade;

  @PermissionAction(requiredPermission = PermissionEnum.VALUE_MAN)
  @PutMapping("/edit")
  public HttpResult editParamValue(@RequestBody @Validated ParamValueUpdateParam param) {
    paramValueFacade.editParamValue(param);
    return HttpResult.success();
  }

  @GetMapping("/list")
  public HttpResult listParamValues(@RequestParam(value = "paramId") Long paramId,
      @RequestParam(value = "search", required = false) String search) {
    return HttpResult.success(paramValueFacade.listParamValue(paramId, search));
  }
}
