package com.netease.hz.bdms.easyinsight.dao.model;

import java.sql.Timestamp;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Session {

  /**
   * 自增id
   */
  protected Long id;

  /**
   * 创建时间
   */
  protected Timestamp createTime;

  /**
   * 更新时间
   */
  protected Timestamp updateTime;

  /**
   * 域ID
   */
  private Long domainId;

  /**
   * 凭证
   */
  private String token;

  /**
   * 用户id
   */
  private Long userId;

  /**
   * 过期时间
   */
  private Timestamp expireTime;

  public Session setExpireTime(Object expireTime) {
    if(expireTime != null) {
      if(expireTime instanceof Timestamp) {
        this.expireTime = (Timestamp) expireTime;
      }else if(expireTime instanceof Date) {
        this.expireTime = new Timestamp(((Date)expireTime).getTime());
      }else if(expireTime instanceof Long) {
        this.expireTime = new Timestamp((Long)expireTime);
      }else if(expireTime instanceof Integer) {
        this.expireTime = new Timestamp((Integer)expireTime*1000l);
      }else if(expireTime instanceof String) {
        this.expireTime = Timestamp.valueOf((String)expireTime);
      }
    }
    return this;
  }
}