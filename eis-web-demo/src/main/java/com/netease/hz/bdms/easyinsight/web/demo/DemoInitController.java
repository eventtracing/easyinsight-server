package com.netease.hz.bdms.easyinsight.web.demo;

import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.constant.DemoConst;
import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.*;
import com.netease.hz.bdms.easyinsight.dao.rbac.*;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.DomainService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DemoInitController {

    @Resource
    private DomainService domainService;

    @Resource
    private AppService appService;

    @Resource
    private TerminalService terminalService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private RoleAuthMapper roleAuthMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 初始化DEMO的数据配置
     */
    @GetMapping("/init")
    public void init() {
        // 0. 插入超级管理员
        if (CollectionUtils.isEmpty(userMapper.getByIds(Collections.singletonList(DemoConst.SYSTEM_USER_ID)))) {
            User user = new User();
            user.setId(DemoConst.SYSTEM_USER_ID);
            user.setEmail(DemoConst.SYSTEM_USER_DTO.getEmail());
            user.setUserName(DemoConst.SYSTEM_USER_DTO.getUserName());
            userMapper.insertById(user);
        }

        // 1. 初始化域
        Long domainId;
        DomainSimpleDTO domainById = domainService.getDomainById(DemoConst.DEMO_DOMAIN_ID);
        if (domainById == null) {
            DomainSimpleDTO domainSimpleDTO = new DomainSimpleDTO();
            domainSimpleDTO.setCode("intern");
            domainSimpleDTO.setName("默认域");
            domainSimpleDTO.setDescription("默认域");
            domainSimpleDTO.setCreator(DemoConst.SYSTEM_SIMPLE_USER_DTO);
            domainSimpleDTO.setUpdater(DemoConst.SYSTEM_SIMPLE_USER_DTO);
            domainSimpleDTO.setOwner(DemoConst.SYSTEM_SIMPLE_USER_DTO);
            domainSimpleDTO.setAdmins(Collections.singletonList(DemoConst.SYSTEM_SIMPLE_USER_DTO));
            domainId = domainService.createDomain(domainSimpleDTO);
        } else {
            domainId = domainById.getId();
        }

        // 2. 初始化空间
        Long appId;
        List<AppSimpleDTO> demoDomainApps = appService.getAppsByDomainId(DemoConst.DEMO_DOMAIN_ID);
        AppSimpleDTO demoApp = null;
        if (CollectionUtils.isNotEmpty(demoDomainApps)) {
            for (AppSimpleDTO o : demoDomainApps) {
                if (DemoConst.DEMO_APP_CODE.equals(o.getCode())) {
                    demoApp = o;
                    break;
                }
            }
        }
        if (demoApp == null) {
            AppSimpleDTO appSimpleDTO = new AppSimpleDTO();
            appSimpleDTO.setCode(DemoConst.DEMO_APP_CODE);
            appSimpleDTO.setName(DemoConst.DEMO_APP_NAME);
            appSimpleDTO.setDomainId(DemoConst.DEMO_DOMAIN_ID);
            appSimpleDTO.setDescription(DemoConst.DEMO_APP_NAME);
            appSimpleDTO.setCreator(DemoConst.SYSTEM_SIMPLE_USER_DTO);
            appSimpleDTO.setUpdater(DemoConst.SYSTEM_SIMPLE_USER_DTO);
            appSimpleDTO.setOwner(DemoConst.SYSTEM_SIMPLE_USER_DTO);
            appSimpleDTO.setAdmins(Collections.singletonList(DemoConst.SYSTEM_SIMPLE_USER_DTO));
            appId = appService.createApp(appSimpleDTO);
        } else {
            appId = demoApp.getId();
        }

        // 3. 初始化端
        List<TerminalSimpleDTO> terminals = terminalService.getByAppId(appId);
        if (CollectionUtils.isEmpty(terminals)) {
            // 使用预置端初始化
            List<TerminalSimpleDTO> presented = terminalService.getPresented(appId, DemoConst.SYSTEM_SIMPLE_USER_DTO);
            presented.forEach(t -> terminalService.create(t));
        }

        // 4. 初始化角色
        List<Role> roles = new ArrayList<>();
        roles.add(createRoleIfNotExist(0, "超级管理员", RoleTypeEnum.PLATFORM, GlobalConst.DEFAULT_PLATFORM_ID, "内置角色"));
        roles.add(createRoleIfNotExist(1, "域负责人", RoleTypeEnum.DOMAIN, null, "内置角色"));
        roles.add(createRoleIfNotExist(2, "域管理员", RoleTypeEnum.DOMAIN, null, "内置角色"));
        roles.add(createRoleIfNotExist(3, "域普通用户", RoleTypeEnum.APP, null, "内置角色"));
        roles.add(createRoleIfNotExist(4, "产品管理员", RoleTypeEnum.APP, null, "内置角色"));
        roles.add(createRoleIfNotExist(5, "产品普通用户", RoleTypeEnum.PLATFORM, null, "内置角色"));

        // 5. 初始化页面权限
        Map<Integer, Long> authCodeToAuthIdMap = new HashMap<>();
        createAuthIfNotExist("埋点管理", 1, -1, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("需求管理", 50, 1, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("需求管理", 100, 50, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1000, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("增加需求", 1001, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑需求", 1002, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("新建对象", 1003, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑对象", 1004, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("设计完成", 1005, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("审核确认", 1006, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("开发完成", 1007, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("测试完成", 1008, 100, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("发布管理", 101, 50, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1009, 101, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑版本号", 1010, 101, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("发布上线", 1011, 101, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("对象管理", 51, 1, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1012, 51, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("新建对象", 1013, 51, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("变更对象", 1014, 51, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("元数据管理", 52, 1, 0, 3, authCodeToAuthIdMap);
        createAuthIfNotExist("参数管理", 102, 52, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1015, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加参数", 1016, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑参数", 1017, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除参数", 1020, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加参数含义", 1018, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("取值管理", 1019, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑参数含义", 1065, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除参数含义", 1066, 102, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("参数模版", 103, 52, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1021, 103, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加模版", 1022, 103, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑模版", 1023, 103, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("复制模版", 1024, 103, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除模版", 1025, 103, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("事件类型", 104, 52, 0, 3, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1026, 104, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加事件类型", 1027, 104, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑事件类型", 1028, 104, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除事件类型", 1029, 104, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("事件类型-参数管理", 1030, 104, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("终端管理", 105, 52, 0, 4, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1031, 105, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加终端类型", 1032, 105, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑终端类型", 1033, 105, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除终端类型", 1034, 105, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("终端类型-参数管理", 1035, 105, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("埋点测试", 2, -1, 0, 3, authCodeToAuthIdMap);
        createAuthIfNotExist("实时测试", 53, 2, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("规则校验", 1036, 53, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("校验结果保存", 1037, 53, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("实时日志", 1038, 53, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("需求测试", 54, 2, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("产品管理", 4, -1, 0, 4, authCodeToAuthIdMap);
        createAuthIfNotExist("产品信息", 58, 4, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("成员管理", 59, 4, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1043, 59, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加成员", 1044, 59, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("移除成员", 1045, 59, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑成员", 1046, 59, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("角色管理", 60, 4, 0, 3, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1047, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("新增角色", 1048, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑角色", 1049, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除角色", 1050, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("添加成员", 1051, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("移除成员", 1052, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("功能权限", 1053, 60, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("域管理", 3, -1, 0, 5, authCodeToAuthIdMap);
        createAuthIfNotExist("域信息", 55, 3, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1059, 55, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("成员管理", 56, 3, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1039, 56, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("新增成员", 1040, 56, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("移除成员", 1041, 56, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑成员", 1042, 56, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("产品配置", 57, 3, 0, 3, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1060, 57, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("新建产品", 1061, 57, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑产品", 1062, 57, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除产品", 1063, 57, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询", 1064, 57, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("平台管理", 5, -1, 0, 6, authCodeToAuthIdMap);
        createAuthIfNotExist("域配置", 61, 5, 0, 1, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限", 1054, 61, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("新建域", 1055, 61, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("编辑域", 1056, 61, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("删除域", 1057, 61, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("访问该域",1058, 61, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限",1067, 62, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("页面查询权限",1068, 63, 1, 2147483647, authCodeToAuthIdMap);
        createAuthIfNotExist("埋点任务",106, 50, 0, 2, authCodeToAuthIdMap);
        createAuthIfNotExist("已上线事件",64, 1, 0, 4, authCodeToAuthIdMap);
        createAuthIfNotExist("规则管理",107, 52, 0, 2147483647, authCodeToAuthIdMap);

        // 6. 角色与页面权限关联
        createRoleAuthIfNotExist(getRoleAuthList(authCodeToAuthIdMap));

        // 7. 赋予SYSTEM账号超级管理员权限
        roles.forEach(role -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(DemoConst.SYSTEM_USER_ID);
            userRole.setRoleId(role.getId());
            userRole.setRoleType(role.getRoleType());
            Long typeId = GlobalConst.DEFAULT_PLATFORM_ID;
            if (RoleTypeEnum.DOMAIN.getCode().equals(role.getRoleType())) {
                typeId = domainId;
            }
            if (RoleTypeEnum.APP.getCode().equals(role.getRoleType())) {
                typeId = appId;
            }
            userRole.setTypeId(typeId);
            if (userRoleMapper.selectByUserIdAndRoleId(userRole.getUserId(), userRole.getRoleId()) == null) {
                userRoleMapper.insert(userRole);
            }
        });
    }

    private Role createRoleIfNotExist(int roleLevel, String name, RoleTypeEnum roleTypeEnum, Long typeId, String desc) {
        Role role = new Role();
        role.setRoleLevel(roleLevel);
        role.setRoleName(name);
        role.setRoleType(roleTypeEnum.getCode());
        role.setTypeId(typeId);
        role.setBuiltin(true);
        role.setDescription(desc);
        List<Role> roles = roleMapper.select(role);
        if (CollectionUtils.isNotEmpty(roles)) {
            Long id = roles.get(0).getId();
            role.setId(id);
            return role;
        }
        roleMapper.insertSelective(role);
        return role;
    }

    private void createAuthIfNotExist(String authName, int authCode, int authParentCode, int authType, int authSort, Map<Integer, Long> authCodeToAuthIdMap) {
        Auth auth = new Auth();
        auth.setAuthName(authName);
        auth.setAuthCode(authCode);
        auth.setAuthParentCode(authParentCode);
        auth.setAuthType(authType);
        auth.setAuthSort(authSort);
        auth.setDescription("");
        List<Auth> auths = authMapper.selectByCodes(Collections.singletonList(authCode));
        if (CollectionUtils.isNotEmpty(auths)) {
            authCodeToAuthIdMap.put(authCode, auths.get(0).getId());
            return;
        }
        authMapper.insert(auth);
        authCodeToAuthIdMap.put(authCode, auth.getId());
    }

    private RoleAuth buildRoleAuth(long roleId, int authCode, Map<Integer, Long> authCodeToAuthIdMap) {
        RoleAuth roleAuth = new RoleAuth();
        roleAuth.setRoleId(roleId);
        roleAuth.setAuthId(authCodeToAuthIdMap.get(authCode));
        return roleAuth;
    }

    private void createRoleAuthIfNotExist(List<RoleAuth> roleAuthList) {
        if (CollectionUtils.isEmpty(roleAuthList)) {
            return;
        }
        roleAuthList = roleAuthList.stream().filter(o -> o.getAuthId() != null).collect(Collectors.toList());
        Map<Long, List<RoleAuth>> groupByRoleId = roleAuthList.stream().collect(Collectors.groupingBy(RoleAuth::getRoleId));
        groupByRoleId.forEach((roleId, list) -> {
            List<RoleAuth> exist = roleAuthMapper.selectByRoleId(roleId);
            Set<Long> existAuthIds = exist.stream().map(RoleAuth::getAuthId).collect(Collectors.toSet());
            Set<Long> targetAuthIds = list.stream().map(RoleAuth::getAuthId).collect(Collectors.toSet());
            List<RoleAuth> toAdd = Sets.difference(targetAuthIds, existAuthIds).stream().map(o -> {
                RoleAuth roleAuth = new RoleAuth();
                roleAuth.setRoleId(roleId);
                roleAuth.setAuthId(o);
                return roleAuth;
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(toAdd)) {
                roleAuthMapper.insertBatch(toAdd);
            }
        });
    }

    private List<RoleAuth> getRoleAuthList( Map<Integer, Long> authCodeToAuthIdMap) {
        List<RoleAuth> roleAuthList = new ArrayList<>();
        roleAuthList.add(buildRoleAuth(1, 1, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 50, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 100, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1000, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1001, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1002, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1003, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1004, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1005, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1006, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1007, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1008, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 101, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1009, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1010, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1011, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 51, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1012, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1013, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1014, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 52, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 102, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1015, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1016, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1017, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1020, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1018, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1019, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1065, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1066, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 103, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1021, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1022, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1023, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1024, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1025, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 104, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1026, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1027, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1028, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1029, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1030, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 105, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1031, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1032, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1033, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1034, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1035, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 2, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 53, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1036, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1037, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1038, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 54, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 4, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 58, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 59, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1043, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1044, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1045, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1046, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 60, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1047, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1048, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1049, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1050, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1051, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1052, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1053, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 3, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 55, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1059, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 56, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1039, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1040, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1041, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1042, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 57, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1060, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1061, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1062, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1063, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1064, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 5, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 61, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1054, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1055, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1056, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1057, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1058, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 50, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 100, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1000, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1001, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1002, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1003, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1004, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1005, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1006, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1007, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1008, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 101, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1009, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1010, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1011, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 51, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1012, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1013, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1014, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 52, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 102, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1015, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1016, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1017, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1020, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1018, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1019, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1065, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1066, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 103, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1021, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1022, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1023, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1024, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1025, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 104, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1026, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1027, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1028, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1029, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1030, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 105, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1031, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1032, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1033, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1034, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1035, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 2, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 53, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1036, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1037, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1038, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 54, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 4, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 58, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 59, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1043, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1044, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1045, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1046, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 60, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1047, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1048, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1049, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1050, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1051, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1052, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1053, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 3, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 55, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1059, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 56, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1039, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1040, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1041, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1042, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 57, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1060, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1061, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1062, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1063, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1064, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 50, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 100, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1000, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1001, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1002, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1003, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1004, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1005, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1006, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1007, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1008, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 101, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1009, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1010, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1011, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 51, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1012, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1013, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1014, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 52, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 102, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1015, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1016, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1017, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1020, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1018, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1019, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1065, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1066, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 103, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1021, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1022, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1023, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1024, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1025, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 104, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1026, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1027, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1028, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1029, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1030, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 105, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1031, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1032, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1033, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1034, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1035, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 2, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 53, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1036, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1037, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1038, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 54, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 4, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 58, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 59, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1043, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1044, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1045, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1046, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 60, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1047, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1048, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1049, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1050, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1051, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1052, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1053, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 3, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 55, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1059, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 56, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1039, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1040, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1041, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1042, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 57, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1060, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1061, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1062, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1063, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1064, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 50, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 100, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1000, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1001, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1002, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1003, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1004, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1005, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1006, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1007, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1008, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 101, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1009, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1010, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1011, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 51, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1012, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1013, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1014, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 52, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 102, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1015, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1016, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1017, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1020, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1018, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1019, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1065, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1066, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 103, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1021, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1022, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1023, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1024, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1025, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 104, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1026, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1027, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1028, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1029, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1030, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 105, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1031, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1032, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1033, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1034, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1035, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 2, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 53, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1036, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1037, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1038, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 54, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 4, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 58, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 59, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1043, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1044, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1045, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1046, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 60, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1047, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1048, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1049, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1050, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1051, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1052, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1053, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1067, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 106, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 64, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 64, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 1068, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 1068, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 1068, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 1068, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 106, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 106, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 106, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 106, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 64, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 64, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 64, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 64, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(1, 107, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(2, 107, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(3, 107, authCodeToAuthIdMap));
        roleAuthList.add(buildRoleAuth(5, 107, authCodeToAuthIdMap));
        return roleAuthList;
    }
}
