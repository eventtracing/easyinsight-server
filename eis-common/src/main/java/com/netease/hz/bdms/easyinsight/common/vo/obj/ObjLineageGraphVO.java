package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.bo.lineage.Node;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author: xumengqiang
 * @date: 2021/12/29 18:51
 */
@Data
public class ObjLineageGraphVO {
    /**
     * 对象的血缘树
     */
    List<Node> tree;

    /**
     * 对象详细信息
     */
    Map<Long, ObjectInfoDTO> objInfoMap;
}
