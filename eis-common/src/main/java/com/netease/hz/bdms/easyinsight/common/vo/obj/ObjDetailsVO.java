package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectEventRelationDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectTrackerInfoDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.param.tag.CidTagInfo;
import com.netease.hz.bdms.easyinsight.common.param.tag.ObjBasicTagDTO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.UnDevelopedEventVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/22 11:12
 */

@Data
@Accessors(chain = true)
public class ObjDetailsVO {
    /**
     * 对象ID
     */
    private Long id;

    /**
     * 对象Oid
     */
    private String oid;

    /**
     * 对象中文名称
     */
    private String name;

    /**
     * 对象类型
     */
    private Integer type;

    /**
     * 对象特殊类型
     */
    private String specialType;

    /**
     * 是否多端一致
     */
    private Boolean consistency;

    /**
     * 对象描述信息
     */
    private String description;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 变更历史ID
     */
    private Long historyId;

    /**
     * 关联图片的url
     */
    private List<String> imgUrls;

    /**
     * 关联标签信息
     */
    private List<TagSimpleDTO> tags;

    /**
     * 对象关联埋点信息
     */
    private List<ObjectTrackerInfoDTO> trackers;

    /**
     * 是否有上线记录
     */
    private Boolean onlineRecord;

    /**
     * 子空间ID，桥梁类型对象使用
     */
    private Long bridgeSubAppId;

    /**
     * 子空间ID下的terminal ID，桥梁类型对象使用，如果为0：挂载本端，否则挂载指定端
     */
    private Long bridgeSubTerminalId;

    /**
     * 对象基本标签
     */
    private ObjBasicTagDTO basicTag;

    /**
     * CID标签信息
     */
    private List<CidTagInfo> cidTagInfos;

    /**
     * 是否解析CID信息
     */
    private boolean analyseCid;

    /**
     * 服务端事件信息
     */
    private List<UnDevelopedEventVO> relationInfos;
}
