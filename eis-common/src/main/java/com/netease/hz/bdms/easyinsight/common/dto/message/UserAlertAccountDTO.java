package com.netease.hz.bdms.easyinsight.common.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户报警平台账户
 *
 * @author: xumengqiang
 * @date: 2021/10/20 12:35
 */
@Data
@Accessors(chain = true)
public class UserAlertAccountDTO {
    // 姓名
    private String name;

    // 邮箱
    private String email;

    // 电话
    private String phone;

    // 是否是有效用户
    private Boolean deleted;

    // 工号
    private String empno;

    // 部门标杆
    private String empdept;

    // 额外信息
    private String moreInfo;

}
