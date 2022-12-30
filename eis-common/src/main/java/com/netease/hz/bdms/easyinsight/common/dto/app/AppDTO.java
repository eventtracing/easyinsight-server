package com.netease.hz.bdms.easyinsight.common.dto.app;

import com.netease.hz.bdms.easyinsight.common.dto.common.BaseUserListHolderDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainSimpleDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AppDTO extends BaseUserListHolderDTO {

  /**
   * 产品ID
   */
  private String code;
  /**
   * 产品名称
   */
  private String name;
  /**
   * 域主键ID
   */
  private Long domainId;
  /**
   * 产品描述
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
   * 域信息
   */
  private DomainSimpleDTO domain;


  public AppDTO setCreateTime(Object createTime) {
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

  public AppDTO setUpdateTime(Object updateTime) {
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
