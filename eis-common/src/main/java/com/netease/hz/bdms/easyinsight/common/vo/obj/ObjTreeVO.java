package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.bo.lineage.Node;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectInfoDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author: xumengqiang
 * @date: 2021/12/29 15:12
 */
@Data
@Accessors(chain = true)
public class ObjTreeVO {
    /**
     * 对象树结构
     */
    List<Node> tree;

    /**
     * 对象详细信息
     */
    Map<Long, ObjectInfoDTO> objInfoMap;

    /**
     * 需要展开显示的spm列表
     */
    List<String> spmsToExpand;
}
