package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 稽查统计报表统计信息
 */
@Accessors(chain = true)
@Data
public class LogCheckFilterValuesVO {

    private List<String> eventCodes;
    private List<String> oids;
    private List<TagSimpleDTO> tags;
    private List<String> priorities;
    private List<String> spms;
    private List<String> referSpms;
    private List<String> bizRefers;
    private List<String> referTypes;
    private List<String> failKeys;
}
