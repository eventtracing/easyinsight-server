package com.netease.hz.bdms.easyinsight.web.demo;

import com.netease.hz.bdms.easyinsight.common.enums.TerminalCodeTypeEum;
import com.netease.hz.bdms.easyinsight.common.vo.obj.ObjDetailsVO;
import com.netease.hz.bdms.easyinsight.service.needimpl.TerminalCodeService;
import org.springframework.stereotype.Service;

@Service
public class DemoTerminalCodeService implements TerminalCodeService {

    @Override
    public String getCode(TerminalCodeTypeEum terminalCodeTypeEum, ObjDetailsVO objDetailsVO) {
        return "Copied Code";
    }
}
