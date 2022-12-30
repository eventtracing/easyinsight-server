package com.netease.hz.bdms.easyinsight.common.dto.terminal;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;

import com.netease.hz.bdms.easyinsight.common.enums.TerminalBigTypeEnum;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class TerminalSimpleDTO {
  /**
   * 参数ID
   */
  private Long id;

  /**
   * 参数中文名称
   */
  private String name;
  /**
   * 参数类型
   * @see TerminalBigTypeEnum
   */
  private Integer type;
  /**
   * 参数类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.TerminalTypeEnum
   */
  private String terminalType;

  /**
   * 是否预置
   */
  private Boolean preset;


  /**
   * 产品ID
   */
  private Long appId;

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
   * 最近更新时间
   */
  private Long updateTime;

  public TerminalSimpleDTO setCreateTime(Object createTime) {
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

  public TerminalSimpleDTO setUpdateTime(Object updateTime) {
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
