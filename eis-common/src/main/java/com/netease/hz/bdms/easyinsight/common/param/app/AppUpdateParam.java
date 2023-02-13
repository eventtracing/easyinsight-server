package com.netease.hz.bdms.easyinsight.common.param.app;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class AppUpdateParam {
  @NotNull(message = "主键ID不能为空")
  private Long id;
  /**
   * 域名称
   */
  @NotBlank(message = "域名称不能为空")
  private String name;

  @NotNull
  private List<UserSimpleDTO> admins;
  /**
   * 描述
   */
  private String description;
}
