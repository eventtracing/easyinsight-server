package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import lombok.Data;

import java.util.List;

@Data
public class ReqAddAggreVO {

    List<CommonAggregateDTO> users;

    List<TerminalAggreVO> terminals;

}
