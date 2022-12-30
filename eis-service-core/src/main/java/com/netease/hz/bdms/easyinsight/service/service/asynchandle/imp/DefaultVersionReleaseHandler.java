package com.netease.hz.bdms.easyinsight.service.service.asynchandle.imp;

import com.netease.hz.bdms.easyinsight.service.service.asynchandle.VersionReleaseHandler;
import org.springframework.stereotype.Service;

@Service
public class DefaultVersionReleaseHandler implements VersionReleaseHandler {

    @Override
    public void onTerminalReleaseSuccess(Long terminalId, Long terminalVersionId) {
        // do nothing
    }
}
