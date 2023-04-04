package com.netease.hz.bdms.easyinsight.common.dto.param.parambind;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.netease.hz.bdms.easyinsight.common.enums.ParamSourceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamBindSimpleDTO {

  /**
   * 参数绑定关系ID
   */
  private Long id;

  /**
   * 参数ID
   */
  @NotNull(message = "参数不能为空")
  private Long paramId;

  /**
   * 关联元素ID，如终端ID，或参数ID，或对象埋点ID
   */
  private Long entityId;
  /**
   * 关联类型，如1终端，2事件类型， 3关联对象埋点， 4参数模板
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  private Integer entityType;
  /**
   * 版本ID
   */
  private Long versionId;

  /**
   * 是否必须传
   */
  private Boolean must = true;
  /**
   * 是否非空
   */
  private Boolean notEmpty;
  /**
   * 是否用于测试
   */
  private Boolean needTest;
  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 参数对应的上报日志是否使用urlEncode编码
   */
  private Boolean isEncode;
  /**
   * 绑定描述
   */
  private String description;
  /**
   * 参数来源
   * {@link ParamSourceTypeEnum}
   */
  private String source;

  /**
   * 参数来源详情
   */
  private String sourceDetail;
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


  public ParamBindSimpleDTO setCreateTime(Object createTime) {
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

  public ParamBindSimpleDTO setUpdateTime(Object updateTime) {
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
