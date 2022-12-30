package com.netease.hz.bdms.easyinsight.common.vo.obj;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: xumengqiang
 * @date: 2022/1/5 11:19
 */
@Data
@Accessors(chain = true)
public class ObjReleaseVO {
    /**
     * 对象ID
     */
    private Long id;

    /**
     * 对象变更ID
     */
    private Long historyId;

    /**
     * 端ID
     */
    private String terminalName;

    /**
     * 发布版本ID
     */
    private Long releaseId;

    /**
     * 端版本名称
     */
    private String terminalVersionName;

    /**
     * 发布人
     */
    private String releaser;

    /**
     * 发布时间
     */
    private Date releaseTime;

}
