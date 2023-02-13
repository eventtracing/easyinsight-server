package com.netease.hz.bdms.easyinsight.service.service.asynchandle;

public interface VersionReleaseHandler {
    /**
     * 同步任务状态 + 任务进度
     */
    void onTerminalReleaseSuccess(Long terminalId, Long terminalVersionId);
}
