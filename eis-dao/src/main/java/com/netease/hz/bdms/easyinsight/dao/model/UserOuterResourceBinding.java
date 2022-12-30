package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserOuterResourceBinding {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 用户email
   */
  private String email;
  /**
   * 绑定类型
   */
  private String type;
  /**
   * 绑定value
   */
  private String bindValue;

}
