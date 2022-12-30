package com.netease.hz.bdms.easyinsight.common.dto.param.parambind;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamBindItemDTO {
  /**
   * 参数ID
   */
  private Long id;

  /**
   * 参数名
   */
  private String code;

  /**
   * 参数中文名称
   */
  private String name;

  /**
   * 参数类型
   */
  private Integer paramType;

  /**
   * 参数值类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum
   */
  private Integer valueType;

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
  /**
   * 参数所有元素
   */
  private List<ParamValueSimpleDTO> values;
  /**
   * 绑定元素ID
   */
  private Long bindId;

  /**
   * 选中的元素，即参数值ID
   */
  private List<Long> selectedValues;

  /**
   * 本参数变化情况 {@link com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum}
   */
  private String diffType;

  /**
   * selectedValues 变化情况 {@link com.netease.hz.bdms.easyinsight.common.enums.DiffTypeEnum}
   */
  private String selectedValuesDiff;


  public ParamBindItemDTO setCreateTime(Object createTime) {
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

  public ParamBindItemDTO setUpdateTime(Object updateTime) {
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
