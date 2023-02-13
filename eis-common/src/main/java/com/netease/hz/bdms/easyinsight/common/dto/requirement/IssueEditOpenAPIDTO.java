package com.netease.hz.bdms.easyinsight.common.dto.requirement;

import lombok.Data;

@Data
public class IssueEditOpenAPIDTO {

    String issueKey;

	Integer productId;

	String fieldKey;

	String value;

	String updator;

	String category;

}
