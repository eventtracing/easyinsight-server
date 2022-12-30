CREATE TABLE `eis_app` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '域ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '域名称',
  `domain_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '域ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_domainId` (`name`, `domain_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '产品信息';

CREATE TABLE `eis_app_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_app_id` bigint(20) DEFAULT NULL COMMENT '父空间id',
  `app_id` bigint(20) DEFAULT NULL COMMENT '子空间id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_app_id_app_id` (`parent_app_id`, `app_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '空间关系表';

CREATE TABLE `eis_aitificial_spm_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `spm` varchar(255) NOT NULL COMMENT 'spm',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '映射状态，详情见SpmMapStatusEnum',
  `spm_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT 'spm状态',
  `version` varchar(64) DEFAULT NULL COMMENT '映射生效版本',
  `spm_tag` varchar(255) DEFAULT NULL COMMENT 'SPM标签',
  `spm_old_list` varchar(255) DEFAULT NULL COMMENT '旧spm列表',
  `note` varchar(255) DEFAULT NULL COMMENT 'SPM映射备注',
  `app_id` bigint(20) NOT NULL COMMENT '产品信息',
  `terminal_id` bigint(20) DEFAULT NULL COMMENT '终端ID',
  `source` tinyint(4) NOT NULL DEFAULT '0' COMMENT '来源',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_appId_spm` (`app_id`, `spm`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '手动添加spm信息';

CREATE TABLE `eis_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `auth_name` varchar(128) NOT NULL DEFAULT '' COMMENT '权限名称',
  `auth_code` int(11) NOT NULL COMMENT '权限编码',
  `auth_parent_code` int(11) NOT NULL COMMENT '父级权限编码，-1 表示没有父级',
  `auth_type` int(11) NOT NULL COMMENT '权限类型：1-菜单; 2-按钮',
  `auth_sort` int(11) NOT NULL COMMENT '序号，可用于调整页面侧边栏的菜单顺序',
  `description` varchar(255) DEFAULT '' COMMENT '权限描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`auth_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT = '权限表';

CREATE TABLE `eis_check_history` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `tracker_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '埋点ID',
  `log` text COMMENT '日志',
  `rule` text COMMENT '规则',
  `check_result` tinyint(4) NOT NULL DEFAULT '1' COMMENT '验证结果，1表示通过，2表示不通过',
  `indicators` text COMMENT '验证指标',
  `spm` varchar(255) NOT NULL DEFAULT '' COMMENT 'spm',
  `event_code` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型code',
  `event_name` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型名称',
  `log_server_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '日志获取时间',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '测试类型，1表示实时测试，2表示需求测试',
  `saver_email` varchar(64) NOT NULL DEFAULT '' COMMENT '保存人的邮箱',
  `saver_name` varchar(64) NOT NULL DEFAULT '' COMMENT '保存人的姓名',
  `save_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_trackerid_spm` (`tracker_id`, `spm`),
  KEY `idx_trackerid_eventcode` (`tracker_id`, `event_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '测试记录';

CREATE TABLE `eis_common_kv` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(20) NOT NULL COMMENT '业务编码',
  `k` varchar(50) NOT NULL COMMENT 'key',
  `v` varchar(255) NOT NULL COMMENT 'value',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_k_v` (`code`, `k`, `v`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '通用KV表';

CREATE TABLE `eis_domain` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '域ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '域名称',
  `owner_email` varchar(64) NOT NULL DEFAULT '' COMMENT '负责人邮箱',
  `owner_name` varchar(64) NOT NULL DEFAULT '' COMMENT '负责人中文名',
  `admins` text COMMENT '管理员邮箱列表',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_code` (`code`),
  KEY `idx_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '域信息';

CREATE TABLE `eis_all_tracker_release` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `terminal_id` bigint(20) DEFAULT NULL COMMENT '终端id',
  `terminal_release_id` bigint(20) NOT NULL COMMENT '端发布id，关联表eis_terminal_release_history',
  `obj_id` bigint(20) NOT NULL COMMENT '对象id',
  `tracker_id` bigint(20) NOT NULL COMMENT '关联埋点trackerId，关联表eis_obj_terminal_tracker',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `app_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trid` (`terminal_release_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '已发布各个端版本血缘图各对象对应trackerId（图中的点信息）';

CREATE TABLE `eis_cid_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `appId` bigint(20) DEFAULT NULL COMMENT 'appId',
  `target` varchar(200) DEFAULT NULL COMMENT 'cid所属的目标，如对象ID、SPM',
  `bindType` varchar(20) DEFAULT NULL COMMENT '绑定类型，如绑定对象ID（OBJECT），绑定SPM（SPM）',
  `cid` varchar(100) DEFAULT NULL COMMENT 'CID',
  `cidName` varchar(50) DEFAULT NULL COMMENT 'CID 名字',
  `ext` varchar(200) DEFAULT NULL COMMENT 'CID扩展信息',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_appId_bindType_cid` (`target`, `appId`, `bindType`, `cid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CID绑定关系';

CREATE TABLE `eis_event_bury_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint(20) NOT NULL COMMENT '需求组Id',
  `event_id` bigint(20) NOT NULL COMMENT '事件Id',
  `event_param_package_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '事件公参版本包Id',
  `terminal_id` bigint(20) NOT NULL COMMENT '关联端Id',
  `terminal_param_package_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '端公参版本包Id',
  `terminal_release_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '端发布版本Id',
  `pre_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '上一次操作的Id（暂时用不到）',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '事件埋点表';

CREATE TABLE `eis_obj_all_relation_release` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `obj_id` bigint(20) NOT NULL COMMENT '对象id',
  `parent_obj_id` bigint(20) DEFAULT NULL COMMENT '父对象id',
  `terminal_id` bigint(20) NOT NULL COMMENT '终端id',
  `terminal_release_id` bigint(20) NOT NULL COMMENT '端发布id，关联表eis_terminal_release_history',
  `app_id` bigint(20) NOT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_terminal_release_id` (`terminal_release_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '已发布各版本全量血缘图（图中的边信息）';

CREATE TABLE `eis_obj_change_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `obj_id` bigint(20) NOT NULL COMMENT '对象id',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `req_pool_id` bigint(20) NOT NULL COMMENT '需求组ID',
  `type` tinyint(4) NOT NULL COMMENT '操作类型，1为新建对象，2为变更对象',
  `consistency` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否多端一致',
  `conflict_status` varchar(20) DEFAULT NULL COMMENT '冲突状态',
  PRIMARY KEY (`id`),
  KEY `idx_req_pool_id` (`req_pool_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对象变更历史记录';

CREATE TABLE `eis_obj_terminal_tracker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `obj_id` bigint(20) NOT NULL COMMENT '对象基本信息ID',
  `obj_history_id` bigint(20) NOT NULL COMMENT '对象新建、变更的标识ID，关联表eis_obj_change_history的主键',
  `req_pool_id` bigint(20) NOT NULL COMMENT '需求组ID',
  `terminal_id` bigint(20) NOT NULL COMMENT '终端ID',
  `terminal_release_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '发布版本ID，关联表eis_terminal_release_history的主键，\n在相关对象上线时填入',
  `pub_param_package_id` bigint(20) DEFAULT NULL COMMENT '全局公参的参数包ID',
  `pre_tracker_id` bigint(20) DEFAULT '0' COMMENT '当前对象上一版本的埋点信息',
  `app_id` bigint(20) NOT NULL COMMENT '产品ID',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对象端tracker';

CREATE TABLE `eis_realtime_branch_ignore` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversationId` varchar(20) DEFAULT NULL COMMENT '会话id',
  `branchKey` varchar(500) DEFAULT NULL COMMENT '分支key',
  `content` varchar(500) DEFAULT NULL COMMENT '分支信息JSON',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversationId_branchKey` (`conversationId`, `branchKey`),
  KEY `idx_conversationId_createTime` (`conversationId`, `createTime`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实时测试分支忽略情况';

CREATE TABLE `eis_req_obj_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `obj_id` bigint(20) NOT NULL COMMENT '对象id',
  `req_pool_id` bigint(20) NOT NULL COMMENT '需求组id',
  `parent_obj_id` bigint(20) DEFAULT NULL COMMENT '父对象id',
  `terminal_id` bigint(20) NOT NULL COMMENT '该血缘属于的端id',
  `app_id` bigint(20) NOT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '需求内的对象父子血缘';

CREATE TABLE `eis_req_pool_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint(20) NOT NULL COMMENT '关联需求组id',
  `event_bury_point_id` bigint(20) NOT NULL COMMENT '事件埋点表id，关联表eis_event_bury_point',
  `event_id` bigint(20) NOT NULL COMMENT '关联事件id（冗余），关联表eis_event',
  `terminal_id` bigint(20) DEFAULT '0' COMMENT '关联终端id',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '埋点设计页——事件埋点待办项';

CREATE TABLE `eis_req_pool` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `data_owners` varchar(200) DEFAULT NULL COMMENT '数据负责人，会有多个',
  `description` mediumtext COMMENT '描述',
  `editable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可编辑',
  `app_id` bigint(20) NOT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique__index` (`app_id`, `name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '需求组基本信息表';

CREATE TABLE `eis_req_pool_rel_base_release` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint(20) NOT NULL COMMENT '需求组id',
  `terminal_id` bigint(20) NOT NULL COMMENT '终端id',
  `base_release_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '基线发布版本id，关联表eis_terminal_release_history的主键',
  `current_use` tinyint(4) DEFAULT '1' COMMENT '该记录是否当前使用，1代表正在使用，0代表历史使用记录',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `auto_rebase` tinyint(4) DEFAULT '1' COMMENT '是否自动变基，1代表是，0代表不是',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '需求组各端关联基线版本';

CREATE TABLE `eis_req_pool_spm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint(20) NOT NULL COMMENT '关联需求组id，关联表eis_req_pool',
  `spm_by_obj_id` varchar(200) NOT NULL DEFAULT '' COMMENT '用对象id构成的spm，代表一个待办项在血缘图中的"坐标"',
  `obj_id` bigint(20) NOT NULL COMMENT '对象id，关联表 eis_obj_basic',
  `obj_history_id` bigint(20) NOT NULL COMMENT '对象变更历史记录id，关联表eis_obj_change_history',
  `terminal_id` bigint(20) NOT NULL COMMENT '关联端id，关联表eis_terminal',
  `req_pool_type` int(11) NOT NULL COMMENT '待办项类型：1:待开发 2:待下线',
  `req_type` varchar(64) NOT NULL COMMENT '需求类型：详细见RequirementTypeEnum枚举类',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `bridge_obj_id` bigint(20) DEFAULT NULL COMMENT '桥梁对象id，如果是桥梁SPM，则bridge是spm_by_obj_id父子空间分界点',
  `bridge_app_id` bigint(20) DEFAULT NULL COMMENT '桥梁对象所属appId',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '埋点设计页spm待办项';

CREATE TABLE `eis_req_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `requirement_id` bigint(20) NOT NULL COMMENT '关联需求id，来自表eis_requirement_into',
  `task_issue_key` varchar(32) DEFAULT NULL COMMENT '任务在overmind上的key',
  `req_issue_key` varchar(32) DEFAULT NULL COMMENT '关联需求在overmind上的key',
  `task_name` varchar(64) NOT NULL,
  `terminal_id` bigint(20) DEFAULT NULL COMMENT '任务关联端id，来自表eis_terminal',
  `terminal_version` varchar(64) DEFAULT NULL COMMENT '端版本名，来自eis_terminal_version_info',
  `terminal_release_id` bigint(20) DEFAULT NULL COMMENT '关联发布版本id，当任务发布时会填入，\n关联表eis_terminal_release_history',
  `iteration` varchar(64) DEFAULT NULL COMMENT '迭代号（om字段）',
  `status` int(11) NOT NULL COMMENT '任务状态',
  `app_id` bigint(20) DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `owner_email` varchar(64) DEFAULT NULL,
  `verifier_name` varchar(64) DEFAULT NULL,
  `verifier_email` varchar(64) DEFAULT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '需求任务表';

CREATE TABLE `eis_requirement_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `req_issue_key` varchar(64) DEFAULT NULL COMMENT '需求在overmind的key',
  `req_name` varchar(200) NOT NULL COMMENT '需求名称',
  `req_pool_id` bigint(20) DEFAULT '0' COMMENT '需求所属的需求组id',
  `source` int(11) NOT NULL COMMENT '来源：1 ——overmind，2——自定义',
  `priority` varchar(10) DEFAULT NULL COMMENT '优先级（om字段）',
  `business_area` varchar(256) DEFAULT NULL COMMENT '业务领域（om字段）',
  `views` varchar(1024) DEFAULT NULL COMMENT '视图（om字段）',
  `team` varchar(256) DEFAULT NULL COMMENT '所属团队（om字段）',
  `om_state` int(11) DEFAULT NULL COMMENT 'om状态（om字段）',
  `description` mediumtext,
  `app_id` bigint(20) DEFAULT NULL,
  `owner_email` varchar(64) DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '需求基本信息表';

CREATE TABLE `eis_task_process` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint(20) NOT NULL COMMENT '关联需求组id，关联表eis_req_pool',
  `req_pool_entity_id` bigint(20) DEFAULT NULL COMMENT '关联需求组待办项id,关联表eis_req_pool_spm、\neis_req_pool_event',
  `req_pool_type` int(11) NOT NULL COMMENT '需求组待办项类型，1:对象埋点spm待开发项,2:对象埋点spm待下线项\n3:事件埋点待开发项',
  `task_id` bigint(20) NOT NULL COMMENT '关联任务id，关联表eis_req_task',
  `status` int(11) NOT NULL,
  `spm_by_obj_id` varchar(256) DEFAULT NULL COMMENT '冗余由对象id构成的spm',
  `obj_id` bigint(20) DEFAULT NULL COMMENT '冗余对象id',
  `event_id` bigint(20) DEFAULT NULL COMMENT '冗余事件id',
  `owner_email` varchar(200) DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `verifier_email` varchar(200) DEFAULT NULL,
  `verifier_name` varchar(64) DEFAULT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '任务下的关联流程';

CREATE TABLE `eis_terminal_release_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `terminal_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联端id',
  `app_id` bigint(20) NOT NULL,
  `pre_release_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '上一发布记录id',
  `latest` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否是该端下最新发布记录',
  `terminal_version_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '端版本id',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '各端发布记录';

CREATE TABLE `eis_terminal_version_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `num` varchar(255) NOT NULL DEFAULT '' COMMENT '端版本号',
  `name` varbinary(250) NOT NULL COMMENT '端版本名称',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`, `app_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '端版本信息表';

CREATE TABLE `eis_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型名称',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `selected_by_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认选中，0表示未选中，1表示选中',
  `applicable_obj_types` text COMMENT '适用的对象类型, 值参考ObjTypeEnum, 默认全部选中',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code` (`app_id`, `code`),
  KEY `idx_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '终端信息';

CREATE TABLE `eis_image_relation` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `entity_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联ID',
  `entity_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '关联元素类型，1表示终端，2表示事件类型，3表示对象，4表示模板',
  `url` varchar(512) NOT NULL COMMENT 'URL地址',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图片信息';

CREATE TABLE `eis_obj_basic` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `oid` varchar(255) NOT NULL DEFAULT '' COMMENT '对象oid',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '对象名称',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '对象类型，1表示页面，2表示元素',
  `description` varchar(5000) DEFAULT '' COMMENT '描述',
  `priority` varchar(4) NOT NULL DEFAULT 'P1' COMMENT '优先级',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `ext` varchar(1024) DEFAULT '' COMMENT '扩展字段',
  `special_type` varchar(50) DEFAULT NULL COMMENT '特殊类型，如桥梁等',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_name` (`app_id`, `name`),
  UNIQUE KEY `uniq_appid_oid` (`app_id`, `oid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对象基本信息';

CREATE TABLE `eis_obj_tag` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `obj_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '对象ID',
  `history_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '对象变更ID',
  `tag_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '标签ID',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_objid` (`obj_id`),
  KEY `idx_historyid` (`history_id`),
  KEY `idx_tagid` (`tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对象与标签之间的映射关系';

CREATE TABLE `eis_obj_tracker_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `tracker_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '对象埋点ID',
  `event_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '事件类型ID',
  `event_param_version_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '（事件类型）事件公参参数包的版本ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_trackerid_eventid` (`tracker_id`, `event_id`),
  KEY `idx_eventid` (`event_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对象埋点的事件信息';

CREATE TABLE `eis_param_bind` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `param_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '参数ID',
  `entity_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联元素ID，如终端ID，或参数ID，或对象埋点ID',
  `entity_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '关联类型，如1终端，2事件类型， 3关联对象埋点， 4参数模板',
  `version_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本，不指定时默认为0',
  `not_empty` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否非空，如1表示非空，0是可为空',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `need_test` tinyint(1) DEFAULT '1' COMMENT '是否用于测试，1为默认，需要测试，0为不需要测试',
  `is_encode` tinyint(1) NOT NULL DEFAULT '0' COMMENT '参数对应的上传日志是否使用urlEncode进行编码',
  `must` tinyint(1) DEFAULT '1' COMMENT '是否必须传，1表示必须传，0表示可以不传',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_paramid_entityid_entitytype` (
    `param_id`,
    `entity_id`,
    `entity_type`,
    `version_id`
  ),
  KEY `idx_entityid` (`entity_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参数绑定信息';

CREATE TABLE `eis_param_bind_value` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `bind_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '参数绑定ID',
  `param_value_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '参数值ID',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_bindid_paramvalueid` (`bind_id`, `param_value_id`),
  KEY `idx_param_value_id` (`param_value_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参数绑定的取值信息';

CREATE TABLE `eis_param` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '参数名',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '参数中文名称',
  `param_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '参数类型，如1 全局公参，2事件公参，3对象标准私参，4.对象业务私参，0.不确定',
  `value_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '值类型，如1常量，2变量， 0不确定',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code_name` (`app_id`, `code`, `name`, `param_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参数信息';

CREATE TABLE `eis_param_pool` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '参数名',
  `param_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '参数类型，如1 全局公参，2事件公参，3对象标准私参，4.对象业务私参',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code` (`app_id`, `code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参数池信息';

CREATE TABLE `eis_param_value` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `param_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '参数ID',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '参数名',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '参数中文名称',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_paramid_code` (`app_id`, `param_id`, `code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参数值信息';

CREATE TABLE `eis_release_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_release_id` bigint(20) DEFAULT NULL COMMENT '父空间发布id',
  `release_id` bigint(20) DEFAULT NULL COMMENT '子空间发布id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_release_id_parent_release_id` (`release_id`, `parent_release_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '发布ID关系表';

CREATE TABLE `eis_role_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `auth_id` bigint(20) NOT NULL COMMENT '权限id',
  `flag` int(11) DEFAULT NULL COMMENT '标志位，用以细分权限，将来扩展用，比如需要细分某个资源的增删改查权限时，可以存4位二进制',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_auth` (`role_id`, `auth_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT = '角色权限表';

CREATE TABLE `eis_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `role_name` varchar(128) NOT NULL DEFAULT '' COMMENT '角色名',
  `role_level` int(11) DEFAULT NULL COMMENT '角色等级',
  `role_type` int(11) NOT NULL COMMENT '0-platform，1-domain，2-app',
  `type_id` bigint(20) DEFAULT NULL COMMENT '如果type为app，则为appId，为domain则为domainId，为platform则默认为-1',
  `builtIn` bit(1) NOT NULL COMMENT '0-内置角色，1-自定义角色',
  `description` varchar(255) DEFAULT '' COMMENT '角色描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT = '角色表';

CREATE TABLE `eis_rule_template` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '规则名称',
  `rule` varchar(64) NOT NULL DEFAULT '' COMMENT '规则表达式',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '规则模板';

CREATE TABLE `eis_session` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `domain_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '域ID',
  `token` varchar(64) NOT NULL DEFAULT '' COMMENT '凭证',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '用户id',
  `expire_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_token` (`domain_id`, `token`),
  KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话信息';

CREATE TABLE `eis_spm_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `spm` varchar(255) NOT NULL COMMENT 'spm',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '映射状态，详情见SpmMapStatusEnum',
  `version` varchar(64) DEFAULT NULL COMMENT '映射生效版本',
  `note` varchar(255) DEFAULT NULL COMMENT 'SPM映射备注',
  `app_id` bigint(20) NOT NULL COMMENT '产品信息',
  `terminal_id` bigint(20) DEFAULT NULL COMMENT '终端ID',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `source` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'spm来源（0-任务同步，1-手动添加）',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'spm映射信息';

CREATE TABLE `eis_spm_map_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增Id',
  `spm` varchar(1000) NOT NULL DEFAULT '' COMMENT '不带pos的spm',
  `spm_name` varchar(1000) NOT NULL DEFAULT '' COMMENT '由链路上每个对象名称和竖线组成，当前对象在第一个位置',
  `spm_old` varchar(1000) DEFAULT NULL COMMENT '配置的老埋点SPMID，可为空，多个时需要逐条拆分',
  `platform` varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品类型',
  `spm_status` int(11) NOT NULL DEFAULT '0' COMMENT '每个spm的流转状态',
  `event_code` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型，多个时需要逐条拆分',
  `description` varchar(5000) DEFAULT NULL COMMENT '对象描述',
  `priority` varchar(4) NOT NULL DEFAULT 'P1' COMMENT '优先级',
  `json` mediumtext NOT NULL COMMENT '由spm构成的埋点规则，包括：spm中每个对象的oid及手动配置的私参，构成elist和plist；事件类型和事件公参；全局公参',
  `elist` mediumtext NOT NULL COMMENT '完整json中的elist部分',
  `plist` mediumtext NOT NULL COMMENT '完整json中的plist部分',
  `story_link` varchar(20) DEFAULT '' COMMENT 'overmind中的需求',
  `task_link` varchar(20) DEFAULT '' COMMENT 'overmind中的任务，一般与终端对应',
  `data_owner` varchar(256) DEFAULT NULL COMMENT '数据责任人，填邮箱',
  `assigner` varchar(100) DEFAULT NULL COMMENT '开发责任人，填邮箱',
  `verifier` varchar(100) DEFAULT NULL COMMENT '测试责任人，填邮箱',
  `creator` varchar(100) DEFAULT NULL COMMENT '创建人邮箱',
  `updater` varchar(100) DEFAULT NULL COMMENT '更新人邮箱',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'spm创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'spm更新时间',
  `spm_check_status` tinyint(4) DEFAULT '1' COMMENT '埋点映射状态，1为待确认，2为双打预发，3为单打预发，4为已上线',
  `spm_app_ver` varchar(64) DEFAULT NULL COMMENT '埋点映射生效版本',
  `tag` varchar(64) DEFAULT NULL COMMENT 'spm标签',
  `is_deployed` tinyint(4) DEFAULT '0' COMMENT 'SPM是否已上过线，0为未上过线，1为上过线',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE `eis_spm_map_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `spm_id` bigint(20) NOT NULL COMMENT 'eis_spm_map_info表中的主键ID',
  `spm_old` varchar(255) NOT NULL COMMENT '老埋点SPN字符串',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `spm_id_spm_old_uidx` (`spm_id`, `spm_old`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '新旧spm映射关系表';

CREATE TABLE `eis_spm_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `spm_id` bigint(20) NOT NULL COMMENT 'eis_spm_map_info表中的主键ID',
  `tag_id` bigint(20) NOT NULL COMMENT 'eis_tag表中的主键ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `spm_tag_unidx` (`spm_id`, `tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'spm标签绑定信息';

CREATE TABLE `eis_tag` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '标签名称',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '标签类型，1为对象标签，2为spm标签',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_name_type` (`app_id`, `name`, `type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '标签';

CREATE TABLE `eis_template` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '模板名称',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_name` (`app_id`, `name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '模板信息';

CREATE TABLE `eis_terminal` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '终端名称',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '终端类型，1表示PC, 2表示无线',
  `preset` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否预置，1表示预置的，0表示非预置',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code_type` (`app_id`, `name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '终端信息';

CREATE TABLE `eis_realtime_test_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'session id',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '测试uid',
  `userName` varchar(60) NOT NULL DEFAULT '' COMMENT '测试用户名',
  `reqName` varchar(60) NOT NULL DEFAULT '' COMMENT '测试需求',
  `baseVersion` varchar(60) NOT NULL DEFAULT '' COMMENT '测试基准版本',
  `terminal` varchar(60) NOT NULL DEFAULT '' COMMENT '测试终端',
  `appVersion` varchar(60) NOT NULL DEFAULT '' COMMENT 'app版本',
  `failedNum` bigint(20) DEFAULT '0' COMMENT '失败数量',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '(1:初始化 2:测试中 -3:测试完成)',
  `extInfo` varchar(1024) DEFAULT '' COMMENT '扩展信息',
  `saveTime` bigint(20) NOT NULL DEFAULT '0' COMMENT '记录保存时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `appId` bigint(20) NOT NULL DEFAULT '0' COMMENT '应用id',
  `taskId` bigint(20) DEFAULT '0',
  `testResult` int(11) DEFAULT '0' COMMENT '测试结果（0-未通过，1-通过，2-部分通过）',
  PRIMARY KEY (`id`),
  KEY `idx_userId_time` (`userId`, `saveTime`),
  KEY `idx_req_time` (`reqName`, `saveTime`),
  KEY `idx_bas_time` (`baseVersion`, `saveTime`),
  KEY `idx_ter_time` (`terminal`, `saveTime`),
  KEY `idx_saveTime` (`saveTime`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '测试会话记录表';

CREATE TABLE `eis_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `email` varchar(255) NOT NULL COMMENT '邮箱',
  `user_name` varchar(255) DEFAULT NULL COMMENT '中文名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_USER_EMAIL` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT = '用户表';

CREATE TABLE `eis_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色 ID',
  `role_type` int(11) NOT NULL COMMENT '0-platform，1-domain，2-app',
  `type_id` bigint(20) NOT NULL COMMENT '如果type为app，则为appId，为domain则为domainId，为platform则默认为-1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_user_role` (`role_type`, `type_id`, `user_id`, `role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT = '用户和角色的关联表';

CREATE TABLE `eis_version` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '版本名称',
  `version_source` tinyint(4) NOT NULL DEFAULT '0' COMMENT '版本来源，如1表示jira, 2表示overmind, 3表示手动创建',
  `entity_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '关联元素ID(部分版本非全局概念，而是与关联元素构成版本),如终端ID，或参数ID，或对象ID, 默认为0',
  `entity_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '关联元素类型(部分版本非全局概念，而是与关联元素构成版本),如1终端，2事件类型， 3关联对象， 4参数模板，默认为0',
  `current_using` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是当前使用版本,1表示是当前使用版本，0表示非当前使用版本',
  `preset` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否预置，1表示预置，0表示非预置',
  `app_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_appid_entityid` (`app_id`, `entity_id`),
  KEY `idx_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '元数据的版本信息';