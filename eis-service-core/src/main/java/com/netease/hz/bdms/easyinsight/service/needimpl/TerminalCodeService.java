package com.netease.hz.bdms.easyinsight.service.needimpl;

import com.netease.hz.bdms.easyinsight.common.enums.TerminalCodeTypeEum;
import com.netease.hz.bdms.easyinsight.common.vo.obj.ObjDetailsVO;

/**
 * 端上参数复制，需要自行实现
 */
public interface TerminalCodeService {

    String getCode(TerminalCodeTypeEum terminalCodeTypeEum, ObjDetailsVO objDetailsVO);
}
