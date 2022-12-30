package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 饼图
 */
@Accessors(chain = true)
@Data
public class PieVO {

    private List<PieValueVO> values;
}
