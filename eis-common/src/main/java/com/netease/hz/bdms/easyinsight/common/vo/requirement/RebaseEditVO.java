package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class RebaseEditVO {

    List<TerminalBaseVO> terminalBases;

    @Data
    public static class TerminalBaseVO {

        Long terminalId;

        String terminalName;
        //最初的基线端版本名称
        String firstTerminalVersion;
        //当前已选择的基线端版本名称
        String currentTerminalVersion;
        //当前已选择的基线id
        Long currentBaseReleaseId;
        /**
         * 是否有基线合并冲突，有基线合并冲突时，无法变基
         */
        Boolean mergeConflict;
        //下拉框选项
        List<TerminalSelectionVO> selections;
    }

    @Data
    public static class TerminalSelectionVO{
        //基线id
        Long baseReleaseId;
        //对应的端版本名称
        String terminalVersionName;
    }

}
