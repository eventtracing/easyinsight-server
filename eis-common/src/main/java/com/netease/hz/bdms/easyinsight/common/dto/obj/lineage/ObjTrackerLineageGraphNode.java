package com.netease.hz.bdms.easyinsight.common.dto.obj.lineage;

import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.ObjTrackerSimpleDTO;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTrackerLineageGraphNode {
  /**
   * 当前节点
   */
  private ObjTrackerSimpleDTO objTracker;
  /**
   * 父亲集合
   */
  private List<ObjTrackerLineageGraphNode> parents;
  /**
   * 儿子集合
   */
  private List<ObjTrackerLineageGraphNode> sons;
  /**
   * 是否展开父亲
   */
  private boolean expandParent;
  /**
   * 是否展开儿子
   */
  private boolean expandSon;



}
