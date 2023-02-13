package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.event.EventCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.event.EventUpdateParam;
import com.netease.hz.bdms.easyinsight.service.facade.EventFacade;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/et/v1/event")
@RestController
public class EventController {

  @Autowired
  private EventFacade eventFacade;


  @PostMapping("/create")
  @PermissionAction(requiredPermission = PermissionEnum.EVENT_TYPE_CREATE)
  public HttpResult createEvent(@RequestBody @Validated EventCreateParam param) {
    return HttpResult.success(eventFacade.createEvent(param));
  }

  @PutMapping("/edit")
  @PermissionAction(requiredPermission = PermissionEnum.EVENT_TYPE_EDIT)
  public HttpResult updateEvent(@RequestBody @Validated EventUpdateParam param) {
    return HttpResult.success(eventFacade.updateEvent(param));
  }

  @DeleteMapping("/delete")
  @PermissionAction(requiredPermission = PermissionEnum.EVENT_TYPE_DELETE)
  public HttpResult deleteEvent(@RequestParam(name = "id") Long id) {
    return HttpResult.success(eventFacade.deleteEvent(id));
  }

  @GetMapping("/list")
  @PermissionAction(requiredPermission = PermissionEnum.EVENT_TYPE_READ)
  public HttpResult listEvents(@RequestParam(name = "currentPage") Integer currentPage,
      @RequestParam(name = "pageSize") Integer pageSize,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      @RequestParam(name = "orderRule",required = false) String orderRule,
      @RequestParam(name = "search", required = false) String search) {
    PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
    return HttpResult.success(
        eventFacade.listEvents(search, pagingSortDTO));
  }

  @GetMapping("/get")
  @PermissionAction(requiredPermission = PermissionEnum.EVENT_TYPE_READ)
  public HttpResult getEvent(@RequestParam(name = "id") Long id) {
    return HttpResult.success(eventFacade.getEvent(id));
  }

}
