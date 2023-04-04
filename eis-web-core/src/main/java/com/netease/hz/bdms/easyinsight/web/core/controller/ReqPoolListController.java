package com.netease.hz.bdms.easyinsight.web.core.controller;


import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReqEntityVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReqPoolCreateVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReqPoolEditVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.ReqPoolPagingListVO;
import com.netease.hz.bdms.easyinsight.service.facade.ReqPoolListPageFacade;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/eis/reqPool")
public class ReqPoolListController {

    @Resource
    private ReqPoolListPageFacade reqPoolListPageFacade;

    /**
     * 创建需求组
     * @param createVO
     * @return
     */
    @PostMapping("/create")
    public HttpResult create(@RequestBody ReqPoolCreateVO createVO){
        reqPoolListPageFacade.createReqPoolBasic(createVO);
        return HttpResult.success();
    }

    /**
     *向需求组添加需求
     * @param reqCreateVo
     * @return
     */
    @PostMapping("/addReq")
    public HttpResult addReqIntoPool(@RequestBody ReqEntityVO reqCreateVo){
        reqPoolListPageFacade.addRequirementsIntoPool(reqCreateVo);
        return HttpResult.success();
    }

    /**
     * 编辑需求组页面视图
     * @param id
     * @return
     */
    @GetMapping("/editView")
    public HttpResult getReqPoolEditView(Long id){
        return HttpResult.success(reqPoolListPageFacade.getReqPoolEditView(id));
    }

    /**
     * 编辑需求组确认
     * @param vo
     * @return
     */
    @PostMapping("/edit")
    public HttpResult editReqPool(@RequestBody ReqPoolEditVO vo){
        reqPoolListPageFacade.editReqPoolBasic(vo);
        return HttpResult.success();
    }

    /**
     * 删除需求组
     * @param id
     * @return
     */
    @GetMapping("/delete")
    public HttpResult deleteReqPool(Long id){
        reqPoolListPageFacade.deleteReqPool(id);
        return HttpResult.success();
    }

    /**
     * 编辑需求页面视图
     * @param id
     * @return
     */
    @GetMapping("/req/editView")
    public HttpResult getReqEditView(Long id){
        return HttpResult.success(reqPoolListPageFacade.getReqEditView(id));
    }

    /**
     * 编辑需求
     * @param editVo
     * @return
     */
    @PostMapping("/req/edit")
    public HttpResult editRequirement(@RequestBody ReqEntityVO editVo){
        reqPoolListPageFacade.editRequirement(editVo);
        return HttpResult.success();
    }

    /**
     * 分页查询
     * @param dataOwnerEmail
     * @param creatorEmail
     * @param
     * @return {@link List<ReqPoolPagingListVO>}
     */
    @GetMapping("/query")
    public HttpResult queryPaging(Long reqPoolId, Long reqId, String dataOwnerEmail, String creatorEmail, Integer status,
                                  String search, String order) {
        return HttpResult.success(reqPoolListPageFacade.pagingQuery(reqPoolId, reqId, dataOwnerEmail, creatorEmail, status, search, order));
    }

    /**
     * 全量查询
     * @return
     */
    @GetMapping("/query/all")
    public HttpResult queryAll(){
        return HttpResult.success(reqPoolListPageFacade.queryAll());
    }

    /**
     * 删除需求
     * @param id
     * @return
     */
    @GetMapping("/req/delete")
    public HttpResult deleteRequirement(Long id){
        reqPoolListPageFacade.deleteRequirement(id);
        return HttpResult.success();
    }

    /**
     * 搜索条件聚合
     * @return
     */
    @GetMapping("/searchAggre")
    public HttpResult getRequireSearchAggre(){
        return HttpResult.success(reqPoolListPageFacade.getRequireSearchAggre());
    }

    /**
     * 新增需求页相关下拉框
     * @return
     */
    @GetMapping("/reqAddAggre")
    public HttpResult getAddAggre(){
        return HttpResult.success(reqPoolListPageFacade.getAddAggre());
    }
}
