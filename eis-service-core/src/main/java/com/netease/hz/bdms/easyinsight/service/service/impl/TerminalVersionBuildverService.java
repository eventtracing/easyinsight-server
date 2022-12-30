package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.enums.CommonKVCodeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TerminalVersionBuildverService {

    @Resource
    private CommonKVService commonKVService;

    public Map<Long, String> getBuildVersions(Set<Long> terminalVersionIds) {
        if (CollectionUtils.isEmpty(terminalVersionIds)) {
            return new HashMap<>();
        }
        Map<String, String> m = commonKVService.multiGet(CommonKVCodeEnum.T_V_BUILDVER.name(), terminalVersionIds.stream().map(String::valueOf).collect(Collectors.toSet()));
        Map<Long, String> result = new HashMap<>();
        for (Long terminalVersionId : terminalVersionIds) {
            String buildVersion = m.get(String.valueOf(terminalVersionId));
            if (buildVersion != null) {
                result.put(terminalVersionId, buildVersion);
            }
        }
        return result;
    }

    public void updateBuildVersion(long terminalVersionId, String buildver) {
        String k = String.valueOf(terminalVersionId);
        commonKVService.set(CommonKVCodeEnum.T_V_BUILDVER.name(), k, buildver);
    }
}
