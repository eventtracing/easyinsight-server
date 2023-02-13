package com.netease.hz.bdms.easyinsight.common.vo.task;

import com.netease.hz.bdms.easyinsight.common.bo.lineage.TreeNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskSpmTreeVO {

    List<TreeNode> roots = new ArrayList<>();

    List<TaskProcessSpmEntityVO> entities;

    List<ObjInfo> objInfos;

    @Data
    public static class ObjInfo{
        Long objId;

        String oid;

        String objName;

        Integer objType;

        Long historyId;

        Long terminalId;

        Long reqPoolId;

        /**
         * 标明是其他空间的对象
         */
        Long otherAppId;
    }

}
