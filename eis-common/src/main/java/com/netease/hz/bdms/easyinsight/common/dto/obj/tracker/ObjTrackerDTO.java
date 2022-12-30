package com.netease.hz.bdms.easyinsight.common.dto.obj.tracker;

import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjBasicSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.version.ObjVersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.require.TerminalVersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.OmTaskVO;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.OmReqVO;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTrackerDTO {

  /**
   * 终端
   */
  private TerminalSimpleDTO terminal;
  /**
   * 公参版本id
   */
  private Long terminalParamVersionId;
  /**
   * 事件类型
   */
  private List<EventSimpleDTO> events;
  /**
   * 事件id —— 事件公参版本Id Map
   */
  private Map<Long,Long> eventParamVersionIdMap;
  /**
   * 对象埋点ID
   */
  private Long id;
  /**
   * 子对象
   */
  private Set<ObjBasicSimpleDTO> parentObjs;
  /**
   * 私有参数绑定信息
   */
  private List<ParamBindItemDTO> paramBinds;
  /**
   * 需求
   */
  private OmReqVO require;
  /**
   * 任务
   */
  private OmTaskVO task;

  /**
   * 端版本
   */
  private TerminalVersionSimpleDTO terminalVersion;
  /**
   * 对象版本
   */
  private ObjVersionSimpleDTO objVersion;
  /**
   * 状态: 参考task_rel_obj表中值
   * @see com.netease.hz.bdms.easyinsight.common.enums.ReqTaskStatusEnum
   */
  private Integer status;
}
