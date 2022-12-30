package com.netease.hz.bdms.easyinsight.dao.model;


import lombok.Data;

import java.util.Date;

@Data
public class SpmMapInfo {
    /**
     * 自增ID
     */
    private Long id;

    /**
     * 不带pos的spm
     */
    private String spm;

    /**
     * 由链路上每个对象名称和竖线组成，当前对象在第一个位置
     */
    private String spmName;

    /**
     * 配置的老埋点SpmID，可为空，多个时需要逐条拆分
     */
    private String spmOld;

    /**
     * 埋点验证状态
     * @see com.netease.hz.bdms.easyinsight.common.enums.SpmMapStatusEnum
     */
    private Integer spmCheckStatus;

    /**
     * 埋点映射生效版本
     */
    private String spmAppVer;

    /**
     * spm标签
     */
    private String tag;

    /**
     * 终端名称
     */
    private String platform;

    /**
     * 产品类型
     */
    private Long appId;

    /**
     * 每个spm的流转状态
     */
    private Integer spmStatus;

    /**
     * 是否已上过线
     */
    private Boolean isDeployed;

    /**
     * 事件类型，多个时需要逐条拆分
     */
    private String eventCode;

    /**
     * 对象描述
     */
    private String description;

    /**
     * 对象优先级
     */
    private String priority;
    /**
     * 由spm构成的埋点规则，包括：spm中每个对象的oid及手动配置的私参，
     * 构成elist和plist；事件类型和事件公参；全局公参
     */
    private String json;

    /**
     * 完整json中的elist部分
     */
    private String elist;

    /**
     * 完整json中的plist部分
     */
    private String plist;

    /**
     * overmind中的需求
     */
    private String storyLink;

    /**
     * overmind中的任务，一般与终端对应
     */
    private String taskLink;

    private String dataOwner; //数据责任人邮箱

    private String assigner; // 开发责任人邮箱

    private String verifier;// 测试责任人邮箱

    private Date createTime; // 创建时间

    private String creator; // 创建人邮箱

    private Date updateTime; // 更新时间

    private String updater; // 更新人邮箱
}

