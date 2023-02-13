package com.netease.hz.bdms.easyinsight.common.dto.image;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImageRelationDTO {

  /**
   * 主键ID
   */
  private Integer id;
  /**
   * 服务URL
   */
  private String url;
  /**
   * 关联类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum
   */
  private Integer entityType;
  /**
   * 关联ID
   */
  private Long entityId;
  /**
   * 创建人
   */
  private UserSimpleDTO creator;
  /**
   * 最近更新人
   */
  private UserSimpleDTO updater;
  /**
   * 创建时间
   */
  private Long createTime;
  /**
   * 最近更新时间
   */
  private Long updateTime;
}
