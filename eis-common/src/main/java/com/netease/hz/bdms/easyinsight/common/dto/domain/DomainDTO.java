package com.netease.hz.bdms.easyinsight.common.dto.domain;

import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.BaseUserListHolderDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DomainDTO extends BaseUserListHolderDTO {

  /**
   * 域 code
   */
  private String code;
  /**
   * 域中文民
   */
  private String name;

  /**
   * 域创建者
   */
  private UserSimpleDTO creator;
  /**
   * 域最近更新人
   */
  private UserSimpleDTO updater;
  /**
   * 域创建时间
   */
  private Long createTime;
  /**
   * 域最近更新时间
   */
  private Long updateTime;
  /**
   * 包含的产品
   */
  private List<AppSimpleDTO> apps;

  public DomainDTO setCreateTime(Object createTime) {
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

  public DomainDTO setUpdateTime(Object updateTime) {
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
