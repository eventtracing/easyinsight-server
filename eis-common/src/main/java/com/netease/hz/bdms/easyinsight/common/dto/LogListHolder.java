package com.netease.hz.bdms.easyinsight.common.dto;

import com.netease.hz.bdms.easyinsight.common.vo.logcheck.LogCheckLogVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class LogListHolder {
    private List<LogCheckLogVO> list;
}
