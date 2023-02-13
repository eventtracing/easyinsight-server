package com.netease.hz.bdms.easyinsight.common.bo.diff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDiff {

    private Set<Long> newParents;

    private Set<Long> deletedParents;

}
