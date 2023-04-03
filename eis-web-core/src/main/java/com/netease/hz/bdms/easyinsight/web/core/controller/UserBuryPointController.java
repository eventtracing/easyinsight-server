package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.obj.ObjectUserParam;
import com.netease.hz.bdms.easyinsight.service.service.obj.UserBuryPointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 用户录入埋点
 */
@Slf4j
@RequestMapping("/eis/v2/user/point")
@RestController
public class UserBuryPointController {

    @Autowired
    UserBuryPointService userBuryPointService;

    /**
     * 埋点录入
     *
     * @param param 埋点信息
     * @return {@link Boolean}
     */
    @RequestMapping("/create")
    public HttpResult getEventsPool(@RequestBody ObjectUserParam param){
        if(CollectionUtils.isNotEmpty(param.getPointParams())){
            userBuryPointService.userPointEntry(param);
        }
        return HttpResult.success(true);
    }

    /**
     * 埋点删除
     *
     * @param id 用户录入埋点 主键ID
     * @return {@link Boolean}
     */
    @RequestMapping("/delete")
    public HttpResult delete(@RequestParam("id") Long id){
        userBuryPointService.delById(id);
        return HttpResult.success(true);
    }

}
