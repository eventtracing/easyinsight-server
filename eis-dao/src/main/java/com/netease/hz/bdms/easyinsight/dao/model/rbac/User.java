package com.netease.hz.bdms.easyinsight.dao.model.rbac;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class User {

  private Long id;
  private String email;
  private String userName;
  private Timestamp createTime;
  private Timestamp updateTime;

  public User(String email, String userName) {
    this.email = email;
    this.userName = userName;
  }
}
