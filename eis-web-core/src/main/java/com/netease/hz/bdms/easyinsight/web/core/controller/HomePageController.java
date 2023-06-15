package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.vo.task.ReqTaskVO;
import com.netease.hz.bdms.easyinsight.service.facade.HomePageFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/et/v1/homepage")
@RestController
public class HomePageController {
  @Autowired
  private HomePageFacade homePageFacade;

  /**
   * 用户首页相关任务预览
   * @param status {@link ProcessStatusEnum}
   * @return {@link List<ReqTaskVO>}
   */
  @PermissionAction(requiredPermission = {PermissionEnum.REQUIREMENT_READ})
  @GetMapping("/relatedReq/list")
  public HttpResult listVersions(@RequestParam(name = "status") Integer status) {
    List<ReqTaskVO> reqTaskVOS = homePageFacade.queryRelatedReqList(status);
    return HttpResult.success(reqTaskVOS);
  }

}
