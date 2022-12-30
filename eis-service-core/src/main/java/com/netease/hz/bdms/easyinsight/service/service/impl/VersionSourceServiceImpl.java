package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.VersionBaseVO;
import com.netease.hz.bdms.easyinsight.service.service.VersionSourceComposer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class VersionSourceServiceImpl implements InitializingBean {

    @Resource
    private List<VersionSourceComposer> versionSourceComposers;

    private Map<Integer, VersionSourceComposer> composerMap = new HashMap<>();

    public void composeSourceInfo(Collection<? extends VersionBaseVO> versionBaseVOs) {
        if (CollectionUtils.isEmpty(versionBaseVOs)) {
            return;
        }
        versionBaseVOs.forEach(versionBaseVO -> {
            if (versionBaseVO.getSource() == null) {
                return;
            }
            VersionSourceComposer versionSourceComposer = composerMap.get(versionBaseVO.getSource());
            boolean composed = false;
            if (versionSourceComposer != null) {
                composed = versionSourceComposer.compose(versionBaseVO);
            }
            // 如果未被组装，则组装兜底数据
            if (!composed) {
                composeAsDefault(versionBaseVO);
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(versionSourceComposers)) {
            versionSourceComposers.forEach(t -> {
                if (t.getType() == null) {
                    return;
                }
                composerMap.put(t.getType().getType(), t);
            });
        }
    }

    private void composeAsDefault(VersionBaseVO versionBaseVO) {
        versionBaseVO.setSourceCreateTime(new Date(0L));
        versionBaseVO.setSourceStatus(VersionSourceStatusEnum.OTHER.getType());
        versionBaseVO.setSourceStatusDesc(VersionSourceStatusEnum.OTHER.getDesc());
        versionBaseVO.setPlanReleaseTime("未知");
    }
}
