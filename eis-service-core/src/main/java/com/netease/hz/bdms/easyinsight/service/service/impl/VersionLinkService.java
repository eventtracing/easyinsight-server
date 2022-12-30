package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.enums.CommonKVCodeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class VersionLinkService {

    @Resource
    private CommonKVService commonKVService;

    public Map<String, String> getLinks(Set<String> versionName) {
        if (CollectionUtils.isEmpty(versionName)) {
            return new HashMap<>();
        }
        return commonKVService.multiGet(CommonKVCodeEnum.VERSION_OM_LINK.name(), versionName);
    }

    public void updateLink(String versionName, String link) {
        String s = commonKVService.get(CommonKVCodeEnum.VERSION_OM_LINK.name(), versionName);
        if (link.equals(s)) {
            return;
        }
        commonKVService.set(CommonKVCodeEnum.VERSION_OM_LINK.name(), versionName, link);
    }
}
