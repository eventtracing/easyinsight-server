package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.tag.TagCreateParam;
import com.netease.hz.bdms.easyinsight.service.facade.TagFacade;

import javax.annotation.Resource;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/et/v1/tag")
@RestController
public class TagController {

    @Resource
    private TagFacade tagFacade;


    @PostMapping("/create")
    public HttpResult createTag(@RequestBody @Valid TagCreateParam param) {
        return HttpResult.success(tagFacade.createTag(param.getName(), param.getType()));
    }

    @GetMapping("/list")
    public HttpResult searchTag(@RequestParam(name = "search", required = false) String search,
                                @RequestParam(name = "type") Integer type,
                                @RequestParam(name = "currentPage") Integer currentPage,
                                @RequestParam(name = "pageSize") Integer pageSize) {
        PagingSortDTO pagingSortDTO = new PagingSortDTO(currentPage, pageSize, null, null);
        return HttpResult.success(tagFacade.searchTag(search, type, pagingSortDTO));
    }

    /**
     * 将已创建的对象标签同步至SPM标签
     *
     * @return
     */
    @GetMapping("/sync")
    public HttpResult syncObjTagToSpmTag(){
        tagFacade.syncTag();
        return HttpResult.success();
    }
}
