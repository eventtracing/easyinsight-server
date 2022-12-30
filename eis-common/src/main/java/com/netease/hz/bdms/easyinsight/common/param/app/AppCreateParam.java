package com.netease.hz.bdms.easyinsight.common.param.app;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Setter
@Getter
public class AppCreateParam {
  /**
   * 产品名称
   */
  @NotBlank(message = "产品名称不能为空")
  private String name;

  /**
   * 管理员
   */
  @NotNull
  @Size(min = 1)
  private List<UserSimpleDTO> admins;

  /**
   * 描述
   */
  private String description;
}
