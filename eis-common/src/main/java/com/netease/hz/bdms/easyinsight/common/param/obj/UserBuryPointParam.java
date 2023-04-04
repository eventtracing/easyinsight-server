package com.netease.hz.bdms.easyinsight.common.param.obj;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;


@Data
@Accessors(chain = true)
public class UserBuryPointParam {
    /**
     * 业务线
     */
    private String line;
    /**
     * 终端类型
     */
    private Long terminal;
    /**
     * 页面
     */
    private String page;
    /**
     * 子页面
     */
    private String subPage;
    /**
     * 模块
     */
    private String module;
    /**
     * 坑位
     */
    private String location;
    /**
     * 事件id
     */
    private Long eventId;
    /**
     * 图片
     */
    private String image;
    /**
     * 需求id
     */
    private Long reqId;
    /**
     * 是否已设计
     */
    private Integer designed;
    /**
     * 是否无效需求
     */
    private Integer invalid;
    /**
     * 是否多端一致
     */
    private Integer consistency;
    /**
     * 数据开发
     */
    private String developer;
    /**
     * 额外信息
     */
    private String extInfo;

}
