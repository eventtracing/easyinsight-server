package com.netease.hz.bdms.easyinsight.common.param.obj.tracker;

import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTrackerCreateParam {

  /**
   * trackerId，该字段不从前端获取，目前在编辑对象、基线合并对象时起作用， 主要用于保障修改时，trackerId不发生变化
   */
  private Long trackerId;
  /**
   * 终端ID
   */
  @NotNull(message = "终端ID不能为空")
  private Long terminalId;

  private Long terminalParamVersionId;

  /**
   * 需求ID
   */
  @NotNull(message = "需求不能为空")
  private Long requireId;
  /**
   * 任务ID
   */
  @NotNull(message = "任务ID不能为空")
  private Long taskId;

  /**
   * 事件类型ID集合
   */
  @NotEmpty(message = "事件类型不能为空")
  private List<Long> eventIds;
  /**
   * 事件id——参数版本id map
   */
  private Map<Long,Long> eventParamsVersionIdMap;

  /**
   * 关联子对象集合
   */
  private List<Long> parentObjs;

  /**
   * 参数绑定
   */
  private List<ParamBindItermParam> paramBinds;

  private Long terminalVersionId;
  private Long objVersionId;
  /**
   * 初始最近的主干版本，在第一次创建对象时最新的主干分支进行确定，默认为0
   */
  private Long newestMasterTerminalVersionId;
}
