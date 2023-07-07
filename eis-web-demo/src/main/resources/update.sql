CREATE TABLE `eis_user_point_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '埋点 id',
  `line` varchar(64) NOT NULL COMMENT '业务线',
  `terminal` int(32) NOT NULL COMMENT '终端类型',
  `page` varchar(64) NOT NULL COMMENT '页面',
  `subPage` varchar(64) NOT NULL COMMENT '子页面',
  `module` varchar(64) NOT NULL COMMENT '模块',
  `location` varchar(64) NOT NULL COMMENT '坑位',
  `eventId` bigint(20) NOT NULL COMMENT '事件id',
  `image` varchar(256) DEFAULT NULL COMMENT '图片',
  `reqId` bigint(20) NOT NULL COMMENT '关联需求id',
  `designed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已设计(0-否,1-是)',
  `invalid` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否无效需求(0-否,1-是)',
  `consistency` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否多端一致',
  `creator` varchar(64) DEFAULT NULL COMMENT '录入用户',
  `developer` varchar(64) DEFAULT NULL COMMENT '开发用户',
  `extInfo` varchar(512) DEFAULT NULL COMMENT '备注',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_req` (`reqId`, `terminal`)
) ENGINE = InnoDB AUTO_INCREMENT = 24 DEFAULT CHARSET = utf8mb4 COMMENT = '用户录入埋点';

CREATE TABLE `eis_event_obj_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `eventPoolEntityId` bigint(20) NOT NULL COMMENT '事件池埋点id',
  `terminalId` bigint(20) NOT NULL COMMENT '终端类型',
  `objId` bigint(20) NOT NULL COMMENT '对象id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_entityId` (`eventPoolEntityId`),
  KEY `idx_objId` (`objId`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = '事件埋点对象关联表';

CREATE TABLE `eis_permission_apply_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `appId` int(11) NOT NULL COMMENT 'app类型',
  `applyUser` varchar(60) DEFAULT NULL COMMENT '申请人',
  `auditUser` varchar(60) DEFAULT NULL COMMENT '处理人',
  `roleId` bigint(20) NOT NULL COMMENT '角色id',
  `roleName` varchar(60) NOT NULL COMMENT '角色名称',
  `status` int(11) NOT NULL COMMENT '状态 0-开始/1-已完成/-1-已拒绝',
  `description` varchar(1024) DEFAULT '' COMMENT '申请理由',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `applyUserName` varchar(60) DEFAULT NULL COMMENT '申请人名称',
  PRIMARY KEY (`id`),
  KEY `idx_appId_status` (`appId`, `status`)
) ENGINE = InnoDB AUTO_INCREMENT = 25649 DEFAULT CHARSET = utf8mb4 COMMENT = '权限申请记录表';

CREATE TABLE `eis_tracker_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tracker_id` bigint(20) DEFAULT NULL COMMENT 'trackerId',
  `type` varchar(20) DEFAULT '' COMMENT '数据类型',
  `content` varchar(1024) NOT NULL COMMENT '内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tracker_id_type` (`tracker_id`, `type`)
) ENGINE = InnoDB AUTO_INCREMENT = 21858 DEFAULT CHARSET = utf8mb4 COMMENT = '对象端tracker关联内容表';

CREATE TABLE `eis_req_obj_change_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `objId` bigint(20) NOT NULL COMMENT '对象id',
  `reqPoolId` bigint(20) NOT NULL COMMENT '需求组ID',
  `changeType` varchar(256) DEFAULT NULL COMMENT '变更类型',
  `newTrackerInfo` text COMMENT '变更详情',
  `extInfo` varchar(20) DEFAULT NULL COMMENT 'extInfo',
  `createEmail` varchar(64) DEFAULT '',
  `createName` varchar(64) DEFAULT '',
  `updateEmail` varchar(64) DEFAULT '',
  `updateName` varchar(64) DEFAULT '',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reqId_objId` (`reqPoolId`, `objId`)
) ENGINE = InnoDB AUTO_INCREMENT = 11 DEFAULT CHARSET = utf8mb4 COMMENT = '需求池对象变更历史记录';

alter table eis_event_bury_point
add column `extInfo` varchar(512) NOT NULL DEFAULT '' COMMENT '扩展信息';

alter table eis_template
add column `selected_by_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认选中，0表示未选中，1表示选中';

alter table eis_param_bind
add column source varchar(256) NOT NULL DEFAULT '' COMMENT '参数来源' ,
add column sourceDetail varchar(1024) NOT NULL DEFAULT '' COMMENT '参数来源详情' ;