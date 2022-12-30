package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.service.facade.UserFacade;
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
@RequestMapping("/eis/v1/user")
@RestController
public class UserController {

    @Resource
    private UserFacade userFacade;

    @PostMapping("/create")
    public HttpResult createUser(@RequestBody @Validated UserSimpleDTO userSimpleDTO) {
        return HttpResult.success(userFacade.createUser(userSimpleDTO));
    }

    @PutMapping("/edit")
    public HttpResult updateUser(@RequestBody @Validated UserSimpleDTO userSimpleDTO) {
        return HttpResult.success(userFacade.updateUser(userSimpleDTO));
    }

    @DeleteMapping("/delete")
    public HttpResult deleteUser(@RequestParam(name = "email") String email) {
        return HttpResult.success(userFacade.deleteUser(email));
    }

    @GetMapping("/list")
    public HttpResult listUser(@RequestParam(name = "currentPage") Integer currentPage,
                               @RequestParam(name = "pageSize") Integer pageSize,
                               @RequestParam(name = "orderBy", required = false) String orderBy,
                               @RequestParam(name = "orderRule", required = false) String orderRule,
                               @RequestParam(name = "search", required = false) String search) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, orderBy, orderRule);
        return HttpResult.success(userFacade.listUser(search, pagingSortDTO));
    }

    @GetMapping("/getall")
    public HttpResult getAllUser() {
        return HttpResult.success(userFacade.getUser());
    }
}
