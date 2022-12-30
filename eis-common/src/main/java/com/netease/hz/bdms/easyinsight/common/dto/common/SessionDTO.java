package com.netease.hz.bdms.easyinsight.common.dto.common;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-08-14
 * @version: 1.0
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class SessionDTO implements Serializable {

  private static final long serialVersionUID = 1L;

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
  private Long expireTime;

  public SessionDTO(Long domainId, String token, Long userId, Long expireTime) {
    this.domainId = domainId;
    this.token = token;
    this.userId = userId;
    this.expireTime = expireTime;
  }



  public SessionDTO setExpireTime(Object expireTime) {
    if(expireTime != null) {
      if(expireTime instanceof Timestamp) {
        this.expireTime = ((Timestamp) expireTime).getTime();
      }else if(expireTime instanceof Date) {
        this.expireTime = ((Date) expireTime).getTime();
      }else if(expireTime instanceof Long) {
        this.expireTime = (Long)expireTime;
      }else if(expireTime instanceof Integer) {
        this.expireTime = (Integer)expireTime*1000l;
      }else if(expireTime instanceof String) {
        this.expireTime = Long.parseLong((String)expireTime);
      }
    }
    return this;
  }

}
