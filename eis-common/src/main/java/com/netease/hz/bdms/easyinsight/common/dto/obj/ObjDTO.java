package com.netease.hz.bdms.easyinsight.common.dto.obj;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.ObjTrackerDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjDTO {
  /**
   * 参数ID
   */
  private Long id;

  /**
   * 对象oid
   */
  private String oid;

  /**
   * 参数中文名称
   */
  private String name;

  /**
   * 类别
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  private Integer type;

  /**
   * 产品ID
   */
  private Long appId;

  /**
   * 描述
   */
  private String description;

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

  /**
   * 历史对象ID
   */
  private Long historyId;

  /**
   * 关联图片
   */
  private Set<String> imgUrls;

  /**
   * 是否是多端一致
   */
  private Boolean consistency;

  /**
   * 是否能编辑oid,type字段
   * 若当前尚未有超过一个变更的对象可以进行编辑
   */
  private Boolean editOidOrType;

  /**
   * 对象上对应的埋点信息
   */
  private List<ObjTrackerDTO> trackers;

  private Set<TagSimpleDTO> tags;
  /**
   * 优先级，默认P1
   */
  private String priority;


  public ObjDTO setCreateTime(Object createTime) {
    if(createTime != null) {
      if(createTime instanceof Timestamp) {
        this.createTime = ((Timestamp) createTime).getTime();
      }else if(createTime instanceof Date) {
        this.createTime = ((Date) createTime).getTime();
      }else if(createTime instanceof Long) {
        this.createTime = (Long)createTime;
      }else if(createTime instanceof Integer) {
        this.createTime = (Integer)createTime*1000l;
      }else if(createTime instanceof String) {
        this.createTime = Long.parseLong((String)createTime);
      }
    }
    return this;
  }

  public ObjDTO setUpdateTime(Object updateTime) {
    if(updateTime != null) {
      if(updateTime instanceof Timestamp) {
        this.updateTime = ((Timestamp) updateTime).getTime();
      }else if(updateTime instanceof Date) {
        this.updateTime = ((Date) updateTime).getTime();
      }else if(updateTime instanceof Long) {
        this.updateTime = (Long)updateTime;
      }else if(updateTime instanceof Integer) {
        this.updateTime = (Integer)updateTime*1000l;
      }else if(updateTime instanceof String) {
        this.updateTime = Long.parseLong((String)updateTime);
      }
    }
    return this;
  }
}
