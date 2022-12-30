package com.netease.hz.bdms.easyinsight.common.vo.obj;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

@Accessors(chain = true)
@Data
public class OidsGroupingByObjSubTypeVO {

    private Map<String, Set<String>> map;
}
