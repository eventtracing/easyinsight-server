package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindCopyParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.ParamBindFacade;
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
@RequestMapping("/et/v1/parambind")
@RestController
public class ParamBindController {

  @Autowired
  private ParamBindFacade paramBindFacade;

  @Deprecated
  @PostMapping("/create")
  public HttpResult createParamBindBind(@RequestBody @Validated ParamBindCreateParam param) {
    paramBindFacade.createParamBind(param);
    return HttpResult.success();
  }

  /*  */
  @PutMapping("/edit")
  public HttpResult updateParamBind(@RequestBody @Validated ParamBindUpdateParam param) {
    paramBindFacade.updateParamBind(param);
    return HttpResult.success();
  }

  @Deprecated
  @DeleteMapping("/delete")
  public HttpResult deleteParamBind(@RequestParam(name = "id") Long id) {
    return HttpResult.success(paramBindFacade.deleteParamBind(id));
  }

  @GetMapping("/get")
  public HttpResult getParamBind(@RequestParam(name = "entityId") Long entityId,
                                 @RequestParam(name = "entityType") Integer entityType,
                                 @RequestParam(name = "versionId", required = false) Long versionId) {
    return HttpResult.success(paramBindFacade.getParamBinds(entityId, entityType, versionId));
  }

  @PostMapping("/copy")
  public HttpResult copyParamBind(@RequestBody @Validated ParamBindCopyParam param) {
    paramBindFacade.copyParamBind(param);
    return HttpResult.success();
  }

}
