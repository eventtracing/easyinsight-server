package com.netease.hz.bdms.easyinsight.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ListHolder {
    private List<String> list;
}
