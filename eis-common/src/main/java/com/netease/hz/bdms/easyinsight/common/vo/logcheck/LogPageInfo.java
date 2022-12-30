package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.Map;

@Data
public class LogPageInfo<T> extends PageInfo<T> {

    private Map<String, EisAuditPackageInfoVO> packageInfos;
}
