package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OmImportVO {

    OmReqVO requirement;

    List<OmTaskVO> tasks;

}
