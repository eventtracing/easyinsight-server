package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对象的各种映射关系
 */
@Accessors(chain = true)
@Data
public class ObjMappings {

    private Map<String,String> allObjNameMap = new HashMap<>();
    private Map<Long,String> objIdToNameMap = new HashMap<>();
    private Map<Long,String> objIdToOidMap = new HashMap<>();
    private Map<String,Long> oidToObjIdMap = new HashMap<>();
    private List<ObjectBasic> objs = new ArrayList<>();
}
