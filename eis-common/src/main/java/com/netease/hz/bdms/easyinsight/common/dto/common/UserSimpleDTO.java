package com.netease.hz.bdms.easyinsight.common.dto.common;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class UserSimpleDTO {
  private static final long serialVersionUID = 1L;

  /**
   * id（对应数据库主键ID）
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

  public UserSimpleDTO(String email, String userName) {
    this.email = email;
    this.userName = userName;
  }

  public UserSimpleDTO(Long id, String email, String userName) {
    this.id = id;
    this.email = email;
    this.userName = userName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserSimpleDTO)) {
      return false;
    }
    UserSimpleDTO that = (UserSimpleDTO) o;
    return Objects.equal(getEmail(), that.getEmail());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getEmail());
  }

}
