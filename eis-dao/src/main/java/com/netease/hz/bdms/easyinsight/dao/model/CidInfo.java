package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CidInfo {

  /**
   * 自增id
   */
  private Long id;
  /**
   * appId
   */
  private Long appId;
  /**
   * cid所属的目标，如对象ID、SPM
   */
  private String target;
  /**
   * 绑定类型，如绑定对象ID（OBJECT），绑定SPM（SPM）
   */
  private String bindType;
  /**
   * cid
   */
  private String cid;
  /**
   * cidName
   */
  private String cidName;
  /**
   * ext
   */
  private String ext;

}
