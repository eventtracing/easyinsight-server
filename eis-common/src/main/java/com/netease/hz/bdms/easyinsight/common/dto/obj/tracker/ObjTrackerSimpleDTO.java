package com.netease.hz.bdms.easyinsight.common.dto.obj.tracker;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class ObjTrackerSimpleDTO {
  /**
   * 自增id
   */
  private Long id;
  /**
   * 对象主键ID
   */
  private Long objId;

  /**
   * 对象历史ID，对象的一个版本是一个ID
   */
  private Long historyId;
  /**
   * 终端ID
   */
  private Long terminalId;
  /**
   * （终端）全局参数包的版本ID
   */
  private Long terminalParamVersionId;
  /**
   * 埋点需求版本
   */
  private Long terminalVersionId;
  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 创建时间
   */
  private Long createTime;
  /**
   * 更新时间
   */
  private Long updateTime;

  /**
   * 创建人
   */
  private UserSimpleDTO creator;

  /**
   * 最近更新人
   */
  private UserSimpleDTO updater;
  /**
   * 需求ID
   */
  private Long requireId;
  /**
   * 终端ID
   */
  private Long taskId;
  /**
   * 上一版本ID
   */
  private Long preTrackerId;
  /**
   * 版本对象ID
   */
  private Long objVersionId;

  /**
   * 状态
   * @see com.netease.hz.bdms.easyinsight.common.enums.ReqTaskStatusEnum
   */
  private Integer status;
  /**
   * 合并标识
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjMergeTypeEnum
   */
  private Integer merge;
  /**
   * 需求ID列表,合并对象会使用此字段
   */
  private List<Long> requireIds;
  /**
   * 任务ID列表，合并后的对象会使用此字段
   */
  private List<Long> taskIds;
  /**
   * 合并后的目标对象
   */
  private Long mergeTargetId;
  /**
   * 初始最近的主干版本，在第一次创建对象时最新的主干分支进行确定，默认为0
   */
  private Long newestMasterTerminalVersionId;


  public ObjTrackerSimpleDTO setCreateTime(Object createTime) {
    if(createTime != null) {
      if(createTime instanceof Timestamp) {
        this.createTime = ((Timestamp) createTime).getTime();
      }else if(createTime instanceof Date) {
        this.createTime = ((Date) createTime).getTime();
      }else if(createTime instanceof Long) {
        this.createTime = (Long)createTime;
      }else if(createTime instanceof Integer) {
        this.createTime = (Integer)createTime*1000l;
      }else if(createTime instanceof String) {
        this.createTime = Long.parseLong((String)createTime);
      }
    }
    return this;
  }

  public ObjTrackerSimpleDTO setUpdateTime(Object updateTime) {
    if(updateTime != null) {
      if(updateTime instanceof Timestamp) {
        this.updateTime = ((Timestamp) updateTime).getTime();
      }else if(updateTime instanceof Date) {
        this.updateTime = ((Date) updateTime).getTime();
      }else if(updateTime instanceof Long) {
        this.updateTime = (Long)updateTime;
      }else if(updateTime instanceof Integer) {
        this.updateTime = (Integer)updateTime*1000l;
      }else if(updateTime instanceof String) {
        this.updateTime = Long.parseLong((String)updateTime);
      }
    }
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjTrackerSimpleDTO that = (ObjTrackerSimpleDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
