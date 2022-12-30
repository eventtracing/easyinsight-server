package com.netease.hz.bdms.easyinsight.common.param.tag;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TagCreateParam {
  @NotBlank(message = "标签名称不能为空")
  private String name;

  @NotNull(message = "标签类型不能为空")
  private Integer type;
}
