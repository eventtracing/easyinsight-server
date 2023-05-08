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

alter table eis_event_bury_point
add column `extInfo` varchar(512) NOT NULL DEFAULT '' COMMENT '扩展信息';

alter table eis_template
add column `selected_by_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认选中，0表示未选中，1表示选中';