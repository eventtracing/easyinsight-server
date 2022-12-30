package com.netease.hz.bdms.easyinsight.common.param.domain;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
public class DomainCreateParam {
  /**
   * 域ID
   */
  @NotBlank(message = "域ID不能为空")
  @Pattern(regexp = "^[a-z]+$", message = "域ID只能由小写英文字母组成")// 26 个小写英文字母组成的字符串
  private String code;
  /**
   * 域名称
   */
  @NotBlank(message = "域名称不能为空")
  private String name;
  /**
   * 域负责人
   */
  @NotNull(message = "域负责人不能为空")
  private UserSimpleDTO owner;
  /**
   * 域管理员列表
   */
  private List<UserSimpleDTO> admins;
  /**
   * 描述
   */
  private String description;
}
