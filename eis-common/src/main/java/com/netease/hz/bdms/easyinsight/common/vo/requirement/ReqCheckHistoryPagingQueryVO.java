package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCheckHistoryPagingQueryVO {

  private Integer currentPage;

  private Integer pageSize;

  private String orderBy;

  private String orderRule;

  private Long processId;

  private Long eventBuryPointId;

  private String spm;
  /**
   * 事件类型
   */
  private String events;

  /**
   * 验证结果：若要查询全部，则不传此字段或传null
   * @see com.netease.hz.bdms.easyinsight.common.enums.CheckResultEnum
   */
  private Integer result;

  /**
   * 任务视图查询时使用
   */
  private Long objId;

  /**
   * 任务视图查询时使用
   */
  private Long reqPoolId;

  /**
   * 任务视图查询时使用
   */
  private Long terminalId;

  /**
   * 任务视图查询时使用
   */
  private Long historyId;
}
