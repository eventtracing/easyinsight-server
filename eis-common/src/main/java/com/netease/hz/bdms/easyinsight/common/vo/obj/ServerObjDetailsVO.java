package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum;
import com.netease.hz.bdms.easyinsight.common.param.obj.server.ServerApiInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ServerObjDetailsVO {

    /**
     * 对象ID
     */
    private Long id;

    /**
     * {@link ObjTypeEnum}
     * 对象类型，只能传ObjTypeEnum.namespace = server的
     */
    private Integer type;
    /**
     * 服务端事件名（实际上是oid）
     */
    private String serverEventCode;

    /**
     * 对象中文名称
     */
    private String name;

    /**
     * 对象描述信息
     */
    private String description;

    /**
     * 关联标签信息
     */
    private List<TagSimpleDTO> tags;

    /**
     * API信息
     */
    private List<ServerApiInfo> apiInfos;

    /**
     * 私有参数绑定信息
     */
    private List<ParamBindItemDTO> privateParam;

    /**
     * 终端
     */
    private TerminalSimpleDTO terminal;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 变更历史ID
     */
    private Long historyId;


    /**
     * trackerId
     */
    private Long trackerId;

    /**
     * 该Tracker属性是基于哪个Tracker改出来的
     */
    private Long preTrackerId;
}
