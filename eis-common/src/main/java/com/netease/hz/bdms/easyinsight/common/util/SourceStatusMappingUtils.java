package com.netease.hz.bdms.easyinsight.common.util;

import com.netease.hz.bdms.easyinsight.common.enums.TaskSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.external.OvermindTaskStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.external.OvermindVersionStatusEnum;

/**
 * 将Overmind等外部状态，映射为内部状态
 */
public class SourceStatusMappingUtils {

    /**
     * 从Overmind任务状态，转为统一的 TaskSourceStatusEnum
     * @param o
     * @return
     */
    public static TaskSourceStatusEnum fromOvermindTaskStatus(OvermindTaskStatusEnum o) {
        if (o == null) {
            return TaskSourceStatusEnum.OTHER;
        }
        if (o == OvermindTaskStatusEnum.START) {
            return TaskSourceStatusEnum.START;
        }
        if (o == OvermindTaskStatusEnum.TO_REVIEW) {
            return TaskSourceStatusEnum.TO_REVIEW;
        }
        if (o == OvermindTaskStatusEnum.TO_ARRANGE) {
            return TaskSourceStatusEnum.TO_ARRANGE;
        }
        if (o == OvermindTaskStatusEnum.ARRANGED) {
            return TaskSourceStatusEnum.ARRANGED;
        }
        if (o == OvermindTaskStatusEnum.ONLINE) {
            return TaskSourceStatusEnum.ONLINE;
        }
        if (o == OvermindTaskStatusEnum.LOOKED_BACK) {
            return TaskSourceStatusEnum.LOOKED_BACK;
        }
        if (o == OvermindTaskStatusEnum.LOOKED_BACK_AND_ACCEPTED) {
            return TaskSourceStatusEnum.LOOKED_BACK_AND_ACCEPTED;
        }
        if (o == OvermindTaskStatusEnum.CLOSED) {
            return TaskSourceStatusEnum.CLOSED;
        }
        return TaskSourceStatusEnum.OTHER;
    }

    /**
     * 从Overmind版本状态，转为统一的 VersionSourceStatusEnum
     * @param o
     * @return
     */
    public static VersionSourceStatusEnum fromOvermindVersionStatus(OvermindVersionStatusEnum o) {
        if (o == null) {
            return VersionSourceStatusEnum.OTHER;
        }
        if (o == OvermindVersionStatusEnum.WAIT_FOR_TEST) {
            return VersionSourceStatusEnum.WAIT_FOR_TEST;
        }
        if (o == OvermindVersionStatusEnum.WAIT_FOR_RELEASE) {
            return VersionSourceStatusEnum.WAIT_FOR_RELEASE;
        }
        if (o == OvermindVersionStatusEnum.RELEASED) {
            return VersionSourceStatusEnum.RELEASED;
        }
        if (o == OvermindVersionStatusEnum.TERMINATED) {
            return VersionSourceStatusEnum.TERMINATED;
        }
        return VersionSourceStatusEnum.OTHER;
    }
}
