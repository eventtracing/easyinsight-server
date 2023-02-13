package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjTreeAndTerminalInfoVO {

    /**
     * 对象树结构
     */
    private ObjTreeVO objTree;

    /**
     * 端信息
     */
    private TerminalSimpleDTO terminalSimpleDTO;

}
