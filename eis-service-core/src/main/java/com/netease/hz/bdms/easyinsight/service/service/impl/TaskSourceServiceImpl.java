package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.enums.TaskSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.vo.requirement.TaskDetailVO;
import com.netease.hz.bdms.easyinsight.service.service.TaskSourceComposer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class TaskSourceServiceImpl implements InitializingBean {

    @Resource
    private List<TaskSourceComposer> taskSourceComposers;

    private Map<Integer, TaskSourceComposer> composerMap = new HashMap<>();

    public void composeSourceInfo(Collection<TaskDetailVO> taskDetailVOs) {
        if (CollectionUtils.isEmpty(taskDetailVOs)) {
            return;
        }
        taskDetailVOs.forEach(taskDetailVO -> {
            if (taskDetailVO.getSource() == null) {
                return;
            }
            TaskSourceComposer taskSourceComposer = composerMap.get(taskDetailVO.getSource());
            boolean composed = false;
            if (taskSourceComposer != null) {
                composed = taskSourceComposer.compose(taskDetailVO);
            }
            // 如果没被组装，塞默认值
            if (!composed) {
                composeAsDefault(taskDetailVO);
            }
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(taskSourceComposers)) {
            taskSourceComposers.forEach(t -> {
                if (t.getType() == null) {
                    return;
                }
                composerMap.put(t.getType().getType(), t);
            });
        }
    }

    private void composeAsDefault(TaskDetailVO taskDetailVO) {
        taskDetailVO.setSourceCreateTime(new Date(0L));
        taskDetailVO.setSourceStatus(TaskSourceStatusEnum.OTHER.getType());
        taskDetailVO.setSourceStatusDesc(TaskSourceStatusEnum.OTHER.getDesc());
        taskDetailVO.setPlanReleaseTime("未知");
    }
}
