package com.netease.hz.bdms.easyinsight.common.dto.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @author: xumengqiang
 * @date: 2021/11/9 11:37
 */

@Data
@Accessors(chain = true)
public class SpmCheckInfoDTO {

    /**
     * spm校验结果 (0-非法，1-合法, -1-格式错误)
     */
    private int checkResult;

    /**
     * 合法的spm Name
     */
    private String spmName;

    /**
     * 非法的oid
     */
    private String inValidOid;

}
