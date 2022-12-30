package com.netease.hz.bdms.easyinsight.common.dto.require;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TerminalVersionSimpleDTO {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 版本名称
   */
  private String name;

  /**
   * 版本来源，如1表示jira, 2表示overmind, 3表示手动创建
   * @see com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum
   */
  private Integer versionSource;

  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 端ID
   */
  private Long terminalId;
  /**
   * 版本号
   */
  private String versionNum;
  /**
   * 上一版本
   */
  private Long preVersionId;
  /**
   * 描述
   */
  private String description;
  /**
   * 创建人
   */
  private UserSimpleDTO creator;

  /**
   * 最近更新人
   */
  private UserSimpleDTO updater;
  /**
   * 创建时间
   */
  private Long createTime;
  /**
   * 更新时间
   */
  private Long updateTime;

  /**
   * 端版本状态
   * @see com.netease.hz.bdms.easyinsight.common.enums.TerminalVersionStatusEnum
   */
  private Integer status;
  /**
   * 是否是主干分支
   */
  private  Boolean master;

  public TerminalVersionSimpleDTO setCreateTime(Object createTime) {
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

  public TerminalVersionSimpleDTO setUpdateTime(Object updateTime) {
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
}
