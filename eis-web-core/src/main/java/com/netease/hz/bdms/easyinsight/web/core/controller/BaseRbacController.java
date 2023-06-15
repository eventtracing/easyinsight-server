package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.aop.PermissionAction;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.MenuNodeDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleApplyDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.PermissionAuditEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.PermissionEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.param.auth.*;
import com.netease.hz.bdms.easyinsight.common.param.version.VersionSetParam;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.auth.UserVO;
import com.netease.hz.bdms.easyinsight.common.vo.task.ReqTaskVO;
import com.netease.hz.bdms.easyinsight.service.service.RbacService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限模块 controller
 *
 *
 * @author huzhenhua
 * @modifier wangliangyuan
 * @date 2021-08-02 下午 02:30
 */
@RestController
@RequestMapping("/et/v1/auth")
public class BaseRbacController implements InitializingBean {

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    @Autowired
    private RbacService rbacService;

    private static Map<Long, List<String>> managersOfApp = new HashMap<>();

    /**
     * 分页获取成员列表
     * (适用于域下的成员列表、产品下的成员列表、角色下的成员列表)
     *
     * @param pageableParam
     * @return
     */
    @PermissionAction(requiredPermission ={PermissionEnum.DOMAIN_MEMBER_READ,PermissionEnum.PRODUCT_MEMBER_READ})
    @GetMapping("/user/base/page")
    public HttpResult<PagingResultDTO<? extends UserVO>> memberPage(@Validated MemberListPageableParam pageableParam) {
        PagingResultDTO<UserVO> pagingResultDTO = rbacService.getUserByPage(pageableParam);
        return HttpResult.success(pagingResultDTO);
    }

    /**
     * 获取管理员列表，不需要权限
     * @param appId appId
     */
    @GetMapping("/user/managers")
    public HttpResult<List<String>> listMember(@RequestParam Long appId) {
        if (managersOfApp != null) {
            // 可直接配置appId的审批人员
            List<String> managersSpecified = managersOfApp.get(appId);
            if (CollectionUtils.isNotEmpty(managersSpecified)) {
                return HttpResult.success(managersSpecified);
            }
        }
        // 否则取appId下最早几个管理员
        MemberListPageableParam memberListPageableParam = new MemberListPageableParam();
        memberListPageableParam.setRange(RoleTypeEnum.APP.getCode()); // 查找APP内角色
        memberListPageableParam.setRoleId(5L);  // 管理员
        memberListPageableParam.setCurrentPage(0);
        memberListPageableParam.setPageSize(5);
        memberListPageableParam.setOffset(0);
        memberListPageableParam.setOrderBy("create_time");
        memberListPageableParam.setOrderRule("asc");
        PagingResultDTO<UserVO> userVOList = rbacService.getUserByPage(memberListPageableParam);
        return userVOList == null ? HttpResult.success(new ArrayList<>(0))
                : HttpResult.success(userVOList.getList().stream().map(o -> o.getUserName()).collect(Collectors.toList()));
    }

    /**
     * 不分页的成员列表
     * (适用于域下的成员列表、产品下的成员列表)
     *
     * @param param
     * @return
     */
    @PermissionAction(requiredPermission ={PermissionEnum.DOMAIN_MEMBER_READ,PermissionEnum.PRODUCT_MEMBER_READ})
    @GetMapping("/user/list")
    public HttpResult<List<UserVO>> listMember(@Validated MemberListParam param) {
        List<UserVO> userVOList = rbacService.getUserList(param);
        return HttpResult.success(userVOList);
    }

    /**
     * 添加成员
     * (适用于域下的成员列表、产品下的成员列表、角色下的成员列表)
     *
     * @param createParam
     * @return
     */
    @PermissionAction(requiredPermission ={
            PermissionEnum.DOMAIN_MEMBER_CREATE,
            PermissionEnum.PRODUCT_MEMBER_CREATE,
            PermissionEnum.ROLE_MEMBER_CREATE})
    @PostMapping("/user/create")
    public HttpResult addMember(@RequestBody @Validated UserRoleRelationCreateParam createParam) {
        rbacService.addUser(createParam);
        return HttpResult.success();
    }

    /**
     * 移除成员
     * (适用于域下的成员列表、产品下的成员列表、角色下的成员列表)
     *
     * @param param
     * @return
     */
    @PermissionAction(requiredPermission = {
            PermissionEnum.DOMAIN_MEMBER_REMOVE,
            PermissionEnum.PRODUCT_MEMBER_REMOVE,
            PermissionEnum.ROLE_MEMBER_REMOVE})
    @PostMapping("/user/remove")
    public HttpResult removeMember(@RequestBody @Validated UserRoleRelationDeleteParam param) {
        rbacService.removeUser(param);
        return HttpResult.success();
    }

    /**
     * 更新成员
     * (适用于域下的成员列表、产品下的成员列表)
     * 注意:角色下的成员列表没有更新操作
     *
     * @param updateParam
     * @return
     */
    @PermissionAction(requiredPermission = {PermissionEnum.DOMAIN_MEMBER_EDIT,PermissionEnum.PRODUCT_MEMBER_EDIT})
    @PostMapping("/user/base/update")
    public HttpResult updateMember(@RequestBody @Validated UserRoleRelationUpdateParam updateParam) {
        rbacService.updateUser(updateParam);
        return HttpResult.success();
    }

    /**
     * 域或产品下的角色列表
     *
     * @param range
     * @return
     */
    @PermissionAction(requiredPermission = PermissionEnum.ROLE_READ)
    @GetMapping("/role/list")
    public HttpResult<List<RoleDTO>> listRoles(@RequestParam("range") Integer range) {
        List<RoleDTO> roles = rbacService.getRoleList(range);
        return HttpResult.success(roles);
    }

    /**
     * 创建角色
     *
     * @param createParam
     * @return
     */
    @PermissionAction(requiredPermission = PermissionEnum.ROLE_CREATE)
    @PostMapping("/role/create")
    public HttpResult<Long> createRole(@RequestBody @Validated RoleCreateParam createParam) {
        Long roleId = rbacService.addRole(createParam);
        return HttpResult.success(roleId);
    }

    /**
     * 更新角色
     *
     * @param updateParam
     * @return
     */
    @PermissionAction(requiredPermission = PermissionEnum.ROLE_EDIT)
    @PostMapping("/role/update")
    public HttpResult updateRole(@RequestBody @Validated RoleUpdateParam updateParam) {
        rbacService.updateRole(updateParam);
        return HttpResult.success();
    }

    /**
     * 删除角色
     *
     * @param deleteParam
     * @return
     */
    @PermissionAction(requiredPermission = PermissionEnum.ROLE_DELETE)
    @PostMapping("/role/delete")
    public HttpResult deleteRole(@RequestBody @Validated RoleDeleteParam deleteParam) {
        rbacService.deleteRole(deleteParam.getId());
        return HttpResult.success();
    }

    /**
     * 给角色分配权限
     *
     * @param roleAssignParam
     * @return
     */
    @PostMapping("/role/assign")
    public HttpResult assignFunctionToRole(@RequestBody @Validated RoleAuthParam roleAssignParam) {
        rbacService.assignFunctionToRole(roleAssignParam);
        return HttpResult.success();
    }

    /**
     * 角色的权限树(包含选中状态)
     *
     * @param roleId 角色ID
     * @return
     */
    @PermissionAction(requiredPermission = PermissionEnum.ROLE_AUTH)
    @GetMapping("/menu/list")
    public HttpResult<List<MenuNodeDTO>> getMenuTree(@RequestParam("roleId") Long roleId) {
        List<MenuNodeDTO> menuTree = rbacService.getMenuTree(roleId);
        return HttpResult.success(menuTree);
    }

    /**
     * 获取用户的权限树
     *
     * @param appId 产品ID, 获取在某个产品下的权限时, 必传
     * @return
     */
    @GetMapping("/menu/authorized/list")
    public HttpResult<List<MenuNodeDTO>> getAuthorizedMenu(@RequestParam(value = "appId", required = false) Long appId) {
        List<MenuNodeDTO> menuTreeOfUser = rbacService.getMenuTreeOfUser(appId);
        return HttpResult.success(menuTreeOfUser);
    }


    /**
     * 权限申请
     * @param appId 产品id
     * @param roleId 角色id
     * @param desc 申请理由
     * @return
     */
    @RequestMapping("/permission/apply")
    public HttpResult applyPermission(@RequestParam(value = "appId") Long appId, @RequestParam(value = "roleId") Long roleId, @RequestParam(value = "desc") String desc) {
        return HttpResult.success(rbacService.applyRolePermission(appId, roleId, desc));
    }

    /**
     * 权限申请列表
     * @param appId 产品id
     * @return {@link List<RoleApplyDTO>}
     */
    @PermissionAction(requiredPermission ={PermissionEnum.DOMAIN_MEMBER_READ, PermissionEnum.PRODUCT_MEMBER_READ, PermissionEnum.ROLE_READ})
    @RequestMapping("/permission/list")
    public HttpResult checkPermission(@RequestParam(value = "appId") Long appId, @RequestParam(value = "status", required = false) Integer status) {
        if(status == null){
            status = PermissionAuditEnum.INIT.getChangeType();
        }
        return HttpResult.success(rbacService.getApplyList(appId, status));
    }

    /**
     * 权限审批
     * @param applyId 申请id
     * @param type 1-同意，-1-拒绝
     * @return
     */
    @PermissionAction(requiredPermission ={PermissionEnum.DOMAIN_MEMBER_CREATE, PermissionEnum.PRODUCT_MEMBER_CREATE, PermissionEnum.ROLE_MEMBER_CREATE})
    @RequestMapping("/permission/audit")
    public HttpResult auditPermission(@RequestParam(value = "applyId") Long applyId, @RequestParam(value = "type") Integer type) {
        return HttpResult.success(rbacService.auditRolePermission(applyId, type));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenJSON("managersOfApp", (s) -> managersOfApp = JsonUtils.parseObject(s, new TypeReference<Map<Long, List<String>>>() {}));
    }
}
