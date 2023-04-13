package com.netease.hz.bdms.easyinsight.common.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Search {

    String search;

    Long appId;

    String tag;

}
