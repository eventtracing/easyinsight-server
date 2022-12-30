package com.netease.hz.bdms.easyinsight.common.dto.obj.version;

import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjVersionSimpleDTO {

  /**
   * 对象版本ID
   */
  private Long id;
  /**
   * 对象ID
   */
  private Long objId;
  /**
   * 对象版本号，如V1、V2, 这里存储数字部分
   */
  private Integer objVersionNum;
  /**
   * 对象版本号，如V1、V2
   */
  private String objVersionName;

  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 上一个对象版本的主键ID
   */
  private Long preObjVersionId;
//  /**
//   * 端版本的主键ID
//   */
//  private Long terminalVersionId;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 更新时间
   */
  private Long updateTime;


  public ObjVersionSimpleDTO setCreateTime(Object createTime) {
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

  public ObjVersionSimpleDTO setUpdateTime(Object updateTime) {
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
