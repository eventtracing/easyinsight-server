package com.netease.hz.bdms.easyinsight.common.param.obj.server;

import com.netease.hz.bdms.easyinsight.common.enums.ServerAPITypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ServerApiInfo {

    /**
     * 类型
     * {@link ServerAPITypeEnum}
     */
    private String type;

    /**
     * API的来源
     */
    private String source;

    /**
     * API的ID
     */
    private String id;

    /**
     * API的详情跳转链接
     */
    private String apiDetailUrl;

    /**
     * API的测试跳转链接
     */
    private String apiTestUrl;

    /**
     * 接口参数
     */
    private List<String> params;

    /**
     * 接口路径
     */
    private String path;

    /**
     * 接口描述
     */
    private String desc;
}
