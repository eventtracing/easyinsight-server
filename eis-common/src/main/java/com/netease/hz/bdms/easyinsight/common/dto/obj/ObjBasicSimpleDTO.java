package com.netease.hz.bdms.easyinsight.common.dto.obj;


import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjBasicSimpleDTO {
  /**
   * 自增id
   */
  private Long id;
  /**
   * 对象oid
   */
  private String oid;
  /**
   * 对象名称
   */
  private String name;
  /**
   * 对象类型，1表示页面，2表示元素
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  private Integer type;
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


  public ObjBasicSimpleDTO setCreateTime(Object createTime) {
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

  public ObjBasicSimpleDTO setUpdateTime(Object updateTime) {
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
    ObjBasicSimpleDTO that = (ObjBasicSimpleDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
