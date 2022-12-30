package com.netease.hz.bdms.easyinsight.common.bo.diff;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChangeTuple {

    Boolean isPubParamPackageChanged;

    Boolean isPrvParamChanged;

    Boolean isEventChanged;

    List<List<Long>> newSpms = new ArrayList<>();

    List<List<Long>> deletedSpms = new ArrayList<>();

}
