package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;

import java.util.Map;

@Data
public class LogCheckDetailVO {

    private Map<String, Object> log;
    private Map<String, Object> rule;
    private String spmOwner;

}
