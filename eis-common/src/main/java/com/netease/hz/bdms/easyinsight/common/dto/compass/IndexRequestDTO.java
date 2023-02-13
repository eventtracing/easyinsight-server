package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author wangyongliang
 * @version 1.0
 * @description: 指标查询dto
 * @date 2022/5/19 9:25
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class IndexRequestDTO extends  PageBaseReqDTO{
    /**
     资源类型
     */
    private String resource;
    /**
     开始时间
     */
    private String startTime;

    /**
     结束时间
     */
    private String endTime;


    /**
     结束时间
     */
    private String spm;
    /**
     结束时间
     */
    private String scm;

    /**
     查询条件-资源入参
     */
    private Map<String,Object> parameter;

    /*
    维度
   */
    private Map<String,Object> dimension;

    /**
     指标
     */
    private IndexDTO index;

    /*
    冗余参数
    */
    private List<Map> items;

}
