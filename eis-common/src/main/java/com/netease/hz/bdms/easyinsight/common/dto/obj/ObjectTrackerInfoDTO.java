package com.netease.hz.bdms.easyinsight.common.dto.obj;

import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.require.TerminalVersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
//import com.netease.hz.bdms.easyinsight.common.vo.requirement.RequirementVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author: xumengqiang
 * @date: 2021/12/15 15:31
 */

@Data
@Accessors(chain = true)
public class ObjectTrackerInfoDTO {
    /**
     * 对象埋点ID
     */
    private Long id;

    /**
     * 该Tracker属性是基于哪个Tracker改出来的
     */
    private Long preTrackerId;

    /**
     * 终端
     */
    private TerminalSimpleDTO terminal;

    /**
     * 全局公参包ID
     */
    private Long pubParamPackageId;

    /**
     * 全局公参包ID变化情况 {@link com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum}
     */
    private String pubParamPackageIdDiff;

    /**
     * 事件类型
     */
    private List<EventSimpleDTO> events;

    /**
     * 事件id —— 事件公参版本Id Map
     */
    private Map<Long, Long> eventParamVersionIdMap;

    /**
     * 事件id —— 其关联的事件公参变化情况 {@link com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum}
     */
    private Map<Long, String> eventParamVersionDiff;

    /**
     * 父对象
     */
    private List<ObjectBasicDTO> parentObjects;

    /**
     * 私有参数绑定信息
     */
    private List<ParamBindItemDTO> privateParam;

    /**
     * tracker内容
     */
    private Map<String, String> trackerContents;

}
