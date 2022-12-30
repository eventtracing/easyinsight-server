package com.netease.hz.bdms.easyinsight.dao.model.rbac;

import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.AuthType;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * 权限表 eis_auth 对应的实体类
 *
 * @author wangliangyuan
 * @date 2021-08-02 下午 02:30
 */
@Data
public class Auth {
    /**
     * 自增id
     */
    private Long id;

    /**
     * 权限名称
     */
    private String authName;

    /**
     * 权限编码
     */
    private Integer authCode;

    /**
     * 父级权限编码，{@link GlobalConst#DEFAULT_PARENT_CODE_OF_ROOT_AUTH} 表示没有父级
     */
    private Integer authParentCode;

    /**
     * 权限类型
     * {@link AuthType}
     */
    private Integer authType;

    /**
     * 序号，可用于调整页面侧边栏的菜单顺序
     */
    private Integer authSort;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Auth auth = (Auth) o;
        return authCode.equals(auth.authCode) &&
                authParentCode.equals(auth.authParentCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authCode, authParentCode);
    }
}