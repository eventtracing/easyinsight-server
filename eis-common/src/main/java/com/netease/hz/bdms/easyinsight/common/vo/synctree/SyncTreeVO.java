package com.netease.hz.bdms.easyinsight.common.vo.synctree;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 外部搭建对接
 */
@Data
@Accessors(chain = true)
public class SyncTreeVO {

    /**
     * 空间ID
     */
    private Long appId;

    /**
     * 页面名字，全局唯一
     */
    private String pageName;

    /**
     * 对象列表
     */
    private List<SyncObjVO> nodes;

    /**
     * 时间戳 ms，当前时间1分钟内有效
     */
    private long timestamp;

    /**
     * 开放接口appKey
     */
    private String appKey;

    /**
     * 开放接口签名
     */
    private String sign;
}
