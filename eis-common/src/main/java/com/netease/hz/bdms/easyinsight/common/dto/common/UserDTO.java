package com.netease.hz.bdms.easyinsight.common.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 虽然现在UserDTO现在与UserSimpleDTO参数无二异，但是担心未来会增加内容，故在线程上下文中ThreadLocalMap存入的是UserDTO对象
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
@NoArgsConstructor
public class UserDTO {

  private static final long serialVersionUID = 1L;

  /**
   * 自增id
   */
  private Long id;

  /**
   * 邮箱（对应账号）
   */
  private String email;

  /**
   * 用户名
   */
  private String userName;

  public UserDTO(String email, String userName) {
    this.email = email;
    this.userName = userName;
  }

}
