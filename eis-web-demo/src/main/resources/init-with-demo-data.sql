CREATE DATABASE  IF NOT EXISTS `eis` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `eis`;
-- MySQL dump 10.13  Distrib 8.0.22, for macos10.15 (x86_64)
--
-- Host: localhost    Database: eis
-- ------------------------------------------------------
-- Server version	8.0.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `eis_aitificial_spm_info`
--

DROP TABLE IF EXISTS `eis_aitificial_spm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_aitificial_spm_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `spm` varchar(255) NOT NULL COMMENT 'spm',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '映射状态，详情见SpmMapStatusEnum',
  `spm_status` tinyint NOT NULL DEFAULT '1' COMMENT 'spm状态',
  `version` varchar(64) DEFAULT NULL COMMENT '映射生效版本',
  `spm_tag` varchar(255) DEFAULT NULL COMMENT 'SPM标签',
  `spm_old_list` varchar(255) DEFAULT NULL COMMENT '旧spm列表',
  `note` varchar(255) DEFAULT NULL COMMENT 'SPM映射备注',
  `app_id` bigint NOT NULL COMMENT '产品信息',
  `terminal_id` bigint DEFAULT NULL COMMENT '终端ID',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT '来源',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_appId_spm` (`app_id`,`spm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手动添加spm信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_aitificial_spm_info`
--

LOCK TABLES `eis_aitificial_spm_info` WRITE;
/*!40000 ALTER TABLE `eis_aitificial_spm_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_aitificial_spm_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_all_tracker_release`
--

DROP TABLE IF EXISTS `eis_all_tracker_release`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_all_tracker_release` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `terminal_id` bigint DEFAULT NULL COMMENT '终端id',
  `terminal_release_id` bigint NOT NULL COMMENT '端发布id，关联表eis_terminal_release_history',
  `obj_id` bigint NOT NULL COMMENT '对象id',
  `tracker_id` bigint NOT NULL COMMENT '关联埋点trackerId，关联表eis_obj_terminal_tracker',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `app_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trid` (`terminal_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='已发布各个端版本血缘图各对象对应trackerId（图中的点信息）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_all_tracker_release`
--

LOCK TABLES `eis_all_tracker_release` WRITE;
/*!40000 ALTER TABLE `eis_all_tracker_release` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_all_tracker_release` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_app`
--

DROP TABLE IF EXISTS `eis_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_app` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '域ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '域名称',
  `domain_id` bigint NOT NULL DEFAULT '0' COMMENT '域ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_domainId` (`name`,`domain_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_app`
--

LOCK TABLES `eis_app` WRITE;
/*!40000 ALTER TABLE `eis_app` DISABLE KEYS */;
INSERT INTO `eis_app` VALUES (1,'init_demo','DEMO',1,'DEMO','SYSTEM','SYSTEM','SYSTEM','SYSTEM','2022-12-30 14:58:10','2022-12-30 14:58:10');
/*!40000 ALTER TABLE `eis_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_app_relation`
--

DROP TABLE IF EXISTS `eis_app_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_app_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_app_id` bigint DEFAULT NULL COMMENT '父空间id',
  `app_id` bigint DEFAULT NULL COMMENT '子空间id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_app_id_app_id` (`parent_app_id`,`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='空间关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_app_relation`
--

LOCK TABLES `eis_app_relation` WRITE;
/*!40000 ALTER TABLE `eis_app_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_app_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_auth`
--

DROP TABLE IF EXISTS `eis_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `auth_name` varchar(128) NOT NULL DEFAULT '' COMMENT '权限名称',
  `auth_code` int NOT NULL COMMENT '权限编码',
  `auth_parent_code` int NOT NULL COMMENT '父级权限编码，-1 表示没有父级',
  `auth_type` int NOT NULL COMMENT '权限类型：1-菜单; 2-按钮',
  `auth_sort` int NOT NULL COMMENT '序号，可用于调整页面侧边栏的菜单顺序',
  `description` varchar(255) DEFAULT '' COMMENT '权限描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`auth_code`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8 COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_auth`
--

LOCK TABLES `eis_auth` WRITE;
/*!40000 ALTER TABLE `eis_auth` DISABLE KEYS */;
INSERT INTO `eis_auth` VALUES (1,'埋点管理',1,-1,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(2,'需求管理',50,1,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(3,'需求管理',100,50,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(4,'页面查询权限',1000,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(5,'增加需求',1001,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(6,'编辑需求',1002,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(7,'新建对象',1003,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(8,'编辑对象',1004,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(9,'设计完成',1005,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(10,'审核确认',1006,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(11,'开发完成',1007,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(12,'测试完成',1008,100,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(13,'发布管理',101,50,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(14,'页面查询权限',1009,101,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(15,'编辑版本号',1010,101,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(16,'发布上线',1011,101,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(17,'对象管理',51,1,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(18,'页面查询权限',1012,51,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(19,'新建对象',1013,51,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(20,'变更对象',1014,51,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(21,'元数据管理',52,1,0,3,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(22,'参数管理',102,52,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(23,'页面查询权限',1015,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(24,'添加参数',1016,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(25,'编辑参数',1017,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(26,'删除参数',1020,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(27,'添加参数含义',1018,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(28,'取值管理',1019,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(29,'编辑参数含义',1065,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(30,'删除参数含义',1066,102,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(31,'参数模版',103,52,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(32,'页面查询权限',1021,103,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(33,'添加模版',1022,103,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(34,'编辑模版',1023,103,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(35,'复制模版',1024,103,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(36,'删除模版',1025,103,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(37,'事件类型',104,52,0,3,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(38,'页面查询权限',1026,104,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(39,'添加事件类型',1027,104,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(40,'编辑事件类型',1028,104,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(41,'删除事件类型',1029,104,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(42,'事件类型-参数管理',1030,104,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(43,'终端管理',105,52,0,4,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(44,'页面查询权限',1031,105,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(45,'添加终端类型',1032,105,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(46,'编辑终端类型',1033,105,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(47,'删除终端类型',1034,105,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(48,'终端类型-参数管理',1035,105,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(49,'埋点测试',2,-1,0,3,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(50,'实时测试',53,2,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(51,'规则校验',1036,53,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(52,'校验结果保存',1037,53,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(53,'实时日志',1038,53,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(54,'需求测试',54,2,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(55,'产品管理',4,-1,0,4,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(56,'产品信息',58,4,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(57,'成员管理',59,4,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(58,'页面查询权限',1043,59,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(59,'添加成员',1044,59,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(60,'移除成员',1045,59,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(61,'编辑成员',1046,59,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(62,'角色管理',60,4,0,3,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(63,'页面查询权限',1047,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(64,'新增角色',1048,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(65,'编辑角色',1049,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(66,'删除角色',1050,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(67,'添加成员',1051,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(68,'移除成员',1052,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(69,'功能权限',1053,60,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(70,'域管理',3,-1,0,5,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(71,'域信息',55,3,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(72,'页面查询权限',1059,55,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(73,'成员管理',56,3,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(74,'页面查询权限',1039,56,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(75,'新增成员',1040,56,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(76,'移除成员',1041,56,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(77,'编辑成员',1042,56,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(78,'产品配置',57,3,0,3,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(79,'页面查询权限',1060,57,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(80,'新建产品',1061,57,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(81,'编辑产品',1062,57,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(82,'删除产品',1063,57,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(83,'页面查询',1064,57,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(84,'平台管理',5,-1,0,6,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(85,'域配置',61,5,0,1,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(86,'页面查询权限',1054,61,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(87,'新建域',1055,61,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(88,'编辑域',1056,61,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(89,'删除域',1057,61,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(90,'访问该域',1058,61,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(91,'页面查询权限',1067,62,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(92,'页面查询权限',1068,63,1,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(93,'埋点任务',106,50,0,2,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(94,'已上线事件',64,1,0,4,'','2022-12-30 15:04:22','2022-12-30 15:04:22'),(95,'规则管理',107,52,0,2147483647,'','2022-12-30 15:04:22','2022-12-30 15:04:22');
/*!40000 ALTER TABLE `eis_auth` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_check_history`
--

DROP TABLE IF EXISTS `eis_check_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_check_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `tracker_id` bigint NOT NULL DEFAULT '0' COMMENT '埋点ID',
  `log` text COMMENT '日志',
  `rule` text COMMENT '规则',
  `check_result` tinyint NOT NULL DEFAULT '1' COMMENT '验证结果，1表示通过，2表示不通过',
  `indicators` text COMMENT '验证指标',
  `spm` varchar(255) NOT NULL DEFAULT '' COMMENT 'spm',
  `event_code` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型code',
  `event_name` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型名称',
  `log_server_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '日志获取时间',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '测试类型，1表示实时测试，2表示需求测试',
  `saver_email` varchar(64) NOT NULL DEFAULT '' COMMENT '保存人的邮箱',
  `saver_name` varchar(64) NOT NULL DEFAULT '' COMMENT '保存人的姓名',
  `save_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_trackerid_spm` (`tracker_id`,`spm`),
  KEY `idx_trackerid_eventcode` (`tracker_id`,`event_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_check_history`
--

LOCK TABLES `eis_check_history` WRITE;
/*!40000 ALTER TABLE `eis_check_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_check_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_cid_info`
--

DROP TABLE IF EXISTS `eis_cid_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_cid_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `appId` bigint DEFAULT NULL COMMENT 'appId',
  `target` varchar(200) DEFAULT NULL COMMENT 'cid所属的目标，如对象ID、SPM',
  `bindType` varchar(20) DEFAULT NULL COMMENT '绑定类型，如绑定对象ID（OBJECT），绑定SPM（SPM）',
  `cid` varchar(100) DEFAULT NULL COMMENT 'CID',
  `cidName` varchar(50) DEFAULT NULL COMMENT 'CID 名字',
  `ext` varchar(200) DEFAULT NULL COMMENT 'CID扩展信息',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target_appId_bindType_cid` (`target`,`appId`,`bindType`,`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CID绑定关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_cid_info`
--

LOCK TABLES `eis_cid_info` WRITE;
/*!40000 ALTER TABLE `eis_cid_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_cid_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_common_kv`
--

DROP TABLE IF EXISTS `eis_common_kv`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_common_kv` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(20) NOT NULL COMMENT '业务编码',
  `k` varchar(50) NOT NULL COMMENT 'key',
  `v` varchar(255) NOT NULL COMMENT 'value',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_k_v` (`code`,`k`,`v`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用KV表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_common_kv`
--

LOCK TABLES `eis_common_kv` WRITE;
/*!40000 ALTER TABLE `eis_common_kv` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_common_kv` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_domain`
--

DROP TABLE IF EXISTS `eis_domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_domain` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='域信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_domain`
--

LOCK TABLES `eis_domain` WRITE;
/*!40000 ALTER TABLE `eis_domain` DISABLE KEYS */;
INSERT INTO `eis_domain` VALUES (1,'intern','默认域','','',NULL,'默认域','SYSTEM','SYSTEM','SYSTEM','SYSTEM','2022-12-30 14:58:10','2022-12-30 14:58:10');
/*!40000 ALTER TABLE `eis_domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_event`
--

DROP TABLE IF EXISTS `eis_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_event` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '事件类型名称',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
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
  UNIQUE KEY `uniq_appid_code` (`app_id`,`code`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='终端信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_event`
--

LOCK TABLES `eis_event` WRITE;
/*!40000 ALTER TABLE `eis_event` DISABLE KEYS */;
INSERT INTO `eis_event` VALUES (11,'_ev','对象曝光开始',1,'element view:打点时机为视觉漏出','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[2]'),(12,'_es','流滑动',1,'element slide :对象滑动，打点时机为滑动结束','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[1,2]'),(13,'_pd','页面曝光结束',1,'page viewend:页面视觉消失','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,'[1,3]'),(14,'_ac','app冷启动',1,'app active ：app冷启动','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[1]'),(15,'_ao','app退出到后台',1,'app out：app退到后台','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[1]'),(16,'_ai','app进入前台',1,'app in：app进入前台','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[1]'),(17,'_ed','对象曝光结束',1,'element viewend:对象视觉消失','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[2]'),(18,'_elc','对象长按',1,'element long click：对象长按','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[2]'),(19,'_pv','页面曝光开始',1,'page view:打点时机为：视觉露出','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,'[1,3,4]'),(20,'_ec','对象点击',1,'element click ：对象点击','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',0,'[1,2,3]');
/*!40000 ALTER TABLE `eis_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_event_bury_point`
--

DROP TABLE IF EXISTS `eis_event_bury_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_event_bury_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint NOT NULL COMMENT '需求组Id',
  `event_id` bigint NOT NULL COMMENT '事件Id',
  `event_param_package_id` bigint NOT NULL DEFAULT '0' COMMENT '事件公参版本包Id',
  `terminal_id` bigint NOT NULL COMMENT '关联端Id',
  `terminal_param_package_id` bigint NOT NULL DEFAULT '0' COMMENT '端公参版本包Id',
  `terminal_release_id` bigint NOT NULL DEFAULT '0' COMMENT '端发布版本Id',
  `pre_id` bigint NOT NULL DEFAULT '0' COMMENT '上一次操作的Id（暂时用不到）',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件埋点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_event_bury_point`
--

LOCK TABLES `eis_event_bury_point` WRITE;
/*!40000 ALTER TABLE `eis_event_bury_point` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_event_bury_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_image_relation`
--

DROP TABLE IF EXISTS `eis_image_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_image_relation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `entity_id` bigint NOT NULL DEFAULT '0' COMMENT '关联ID',
  `entity_type` tinyint NOT NULL DEFAULT '0' COMMENT '关联元素类型，1表示终端，2表示事件类型，3表示对象，4表示模板',
  `url` varchar(512) NOT NULL COMMENT 'URL地址',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图片信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_image_relation`
--

LOCK TABLES `eis_image_relation` WRITE;
/*!40000 ALTER TABLE `eis_image_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_image_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_obj_all_relation_release`
--

DROP TABLE IF EXISTS `eis_obj_all_relation_release`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_obj_all_relation_release` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `obj_id` bigint NOT NULL COMMENT '对象id',
  `parent_obj_id` bigint DEFAULT NULL COMMENT '父对象id',
  `terminal_id` bigint NOT NULL COMMENT '终端id',
  `terminal_release_id` bigint NOT NULL COMMENT '端发布id，关联表eis_terminal_release_history',
  `app_id` bigint NOT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_terminal_release_id` (`terminal_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='已发布各版本全量血缘图（图中的边信息）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_obj_all_relation_release`
--

LOCK TABLES `eis_obj_all_relation_release` WRITE;
/*!40000 ALTER TABLE `eis_obj_all_relation_release` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_obj_all_relation_release` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_obj_basic`
--

DROP TABLE IF EXISTS `eis_obj_basic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_obj_basic` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `oid` varchar(255) NOT NULL DEFAULT '' COMMENT '对象oid',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '对象名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '对象类型，1表示页面，2表示元素',
  `description` varchar(5000) DEFAULT '' COMMENT '描述',
  `priority` varchar(4) NOT NULL DEFAULT 'P1' COMMENT '优先级',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `ext` varchar(1024) DEFAULT '' COMMENT '扩展字段',
  `special_type` varchar(50) DEFAULT NULL COMMENT '特殊类型，如桥梁等',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_name` (`app_id`,`name`),
  UNIQUE KEY `uniq_appid_oid` (`app_id`,`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象基本信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_obj_basic`
--

LOCK TABLES `eis_obj_basic` WRITE;
/*!40000 ALTER TABLE `eis_obj_basic` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_obj_basic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_obj_change_history`
--

DROP TABLE IF EXISTS `eis_obj_change_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_obj_change_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `obj_id` bigint NOT NULL COMMENT '对象id',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `req_pool_id` bigint NOT NULL COMMENT '需求组ID',
  `type` tinyint NOT NULL COMMENT '操作类型，1为新建对象，2为变更对象',
  `consistency` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否多端一致',
  `conflict_status` varchar(20) DEFAULT NULL COMMENT '冲突状态',
  PRIMARY KEY (`id`),
  KEY `idx_req_pool_id` (`req_pool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象变更历史记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_obj_change_history`
--

LOCK TABLES `eis_obj_change_history` WRITE;
/*!40000 ALTER TABLE `eis_obj_change_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_obj_change_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_obj_tag`
--

DROP TABLE IF EXISTS `eis_obj_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_obj_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `obj_id` bigint NOT NULL DEFAULT '0' COMMENT '对象ID',
  `history_id` bigint NOT NULL DEFAULT '0' COMMENT '对象变更ID',
  `tag_id` bigint NOT NULL DEFAULT '0' COMMENT '标签ID',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象与标签之间的映射关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_obj_tag`
--

LOCK TABLES `eis_obj_tag` WRITE;
/*!40000 ALTER TABLE `eis_obj_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_obj_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_obj_terminal_tracker`
--

DROP TABLE IF EXISTS `eis_obj_terminal_tracker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_obj_terminal_tracker` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `obj_id` bigint NOT NULL COMMENT '对象基本信息ID',
  `obj_history_id` bigint NOT NULL COMMENT '对象新建、变更的标识ID，关联表eis_obj_change_history的主键',
  `req_pool_id` bigint NOT NULL COMMENT '需求组ID',
  `terminal_id` bigint NOT NULL COMMENT '终端ID',
  `terminal_release_id` bigint NOT NULL DEFAULT '0' COMMENT '发布版本ID，关联表eis_terminal_release_history的主键，\n在相关对象上线时填入',
  `pub_param_package_id` bigint DEFAULT NULL COMMENT '全局公参的参数包ID',
  `pre_tracker_id` bigint DEFAULT '0' COMMENT '当前对象上一版本的埋点信息',
  `app_id` bigint NOT NULL COMMENT '产品ID',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象端tracker';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_obj_terminal_tracker`
--

LOCK TABLES `eis_obj_terminal_tracker` WRITE;
/*!40000 ALTER TABLE `eis_obj_terminal_tracker` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_obj_terminal_tracker` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_obj_tracker_event`
--

DROP TABLE IF EXISTS `eis_obj_tracker_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_obj_tracker_event` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `tracker_id` bigint NOT NULL DEFAULT '0' COMMENT '对象埋点ID',
  `event_id` bigint NOT NULL DEFAULT '0' COMMENT '事件类型ID',
  `event_param_version_id` bigint NOT NULL DEFAULT '0' COMMENT '（事件类型）事件公参参数包的版本ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_trackerid_eventid` (`tracker_id`,`event_id`),
  KEY `idx_eventid` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象埋点的事件信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_obj_tracker_event`
--

LOCK TABLES `eis_obj_tracker_event` WRITE;
/*!40000 ALTER TABLE `eis_obj_tracker_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_obj_tracker_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_param`
--

DROP TABLE IF EXISTS `eis_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_param` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '参数名',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '参数中文名称',
  `param_type` tinyint NOT NULL DEFAULT '0' COMMENT '参数类型，如1 全局公参，2事件公参，3对象标准私参，4.对象业务私参，0.不确定',
  `value_type` tinyint NOT NULL DEFAULT '0' COMMENT '值类型，如1常量，2变量， 0不确定',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code_name` (`app_id`,`code`,`name`,`param_type`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_param`
--

LOCK TABLES `eis_param` WRITE;
/*!40000 ALTER TABLE `eis_param` DISABLE KEYS */;
INSERT INTO `eis_param` VALUES (11,'carrier','运营商',2,2,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(12,'_actseq','交互深度',2,2,1,'- 用户交互深度，每次交互（点击/滑动等）都会+1 - 在当前根page的一次曝光生命周期内自增，如果页面重新曝光了，则先置0 - 页面曝光，不自增','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(13,'es_params','滑动距离',2,2,1,'滑动结束较滑动开始的偏移量','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(14,'imei','终端唯一标识',2,2,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(15,'_duration','时长',2,2,1,'单位ms； ！注意！对于pd、ed等曝光结束事件，SDK内部已经直接计算好了，不需要业务方再开发','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(16,'device','设备标识',2,2,1,'Identifier https://www.theiphonewiki.com/wiki/Models','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(17,'resolution','屏幕分辨率',2,2,1,'格式1125x2436','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(18,'oaid','oaid',2,2,1,'移动安全联盟针对该问题联合国内手机厂商推出补充设备标准体系方案，选择 OAID 字段作为 IMEI 等的替代字段','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(20,'log_time','客户端日志时间',1,2,1,'unixtime','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(21,'os_ver','系统版本',1,2,1,'系统版本','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(22,'appver','app版本',1,2,1,'app版本','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(23,'ip','ip',1,2,1,'ip','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(24,'os','操作系统',1,2,1,'操作系统','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(25,'device_id','deviceId',1,2,1,'deviceId','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(26,'s_ctraceid','请求traceID',3,2,1,'请求traceID','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(27,'s_cid','对象ID',3,2,1,'对象ID','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(28,'s_ctype','对象类型',3,2,1,'对象类型','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(29,'s_ctraceid','请求ID',2,2,1,'请求ID','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12');
/*!40000 ALTER TABLE `eis_param` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_param_bind`
--

DROP TABLE IF EXISTS `eis_param_bind`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_param_bind` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `param_id` bigint NOT NULL DEFAULT '0' COMMENT '参数ID',
  `entity_id` bigint NOT NULL DEFAULT '0' COMMENT '关联元素ID，如终端ID，或参数ID，或对象埋点ID',
  `entity_type` tinyint NOT NULL DEFAULT '0' COMMENT '关联类型，如1终端，2事件类型， 3关联对象埋点， 4参数模板',
  `version_id` bigint NOT NULL DEFAULT '0' COMMENT '版本，不指定时默认为0',
  `not_empty` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否非空，如1表示非空，0是可为空',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
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
  UNIQUE KEY `uniq_paramid_entityid_entitytype` (`param_id`,`entity_id`,`entity_type`,`version_id`),
  KEY `idx_entityid` (`entity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数绑定信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_param_bind`
--

LOCK TABLES `eis_param_bind` WRITE;
/*!40000 ALTER TABLE `eis_param_bind` DISABLE KEYS */;
INSERT INTO `eis_param_bind` VALUES (16,13,12,2,12,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(17,15,13,2,13,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(18,11,14,2,14,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(19,14,14,2,14,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(20,18,14,2,14,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(21,16,14,2,14,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(22,17,14,2,14,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(23,15,15,2,15,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(24,16,16,2,16,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(25,18,16,2,16,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(26,14,16,2,16,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(27,11,16,2,16,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12',1,0,1),(28,15,17,2,17,1,1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13',1,0,1),(29,12,18,2,18,1,1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13',1,0,1),(30,12,20,2,20,1,1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13',1,0,1);
/*!40000 ALTER TABLE `eis_param_bind` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_param_bind_value`
--

DROP TABLE IF EXISTS `eis_param_bind_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_param_bind_value` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `bind_id` bigint NOT NULL DEFAULT '0' COMMENT '参数绑定ID',
  `param_value_id` bigint NOT NULL DEFAULT '0' COMMENT '参数值ID',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_bindid_paramvalueid` (`bind_id`,`param_value_id`),
  KEY `idx_param_value_id` (`param_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数绑定的取值信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_param_bind_value`
--

LOCK TABLES `eis_param_bind_value` WRITE;
/*!40000 ALTER TABLE `eis_param_bind_value` DISABLE KEYS */;
INSERT INTO `eis_param_bind_value` VALUES (1,16,13,1,'','','','','','2022-12-30 15:11:18','2022-12-30 15:11:18'),(2,17,19,1,'','','','','','2022-12-30 15:11:22','2022-12-30 15:11:22'),(3,18,17,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(4,19,14,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(5,20,12,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(6,21,16,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(7,22,15,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(8,23,19,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(9,24,16,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(10,25,12,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(11,26,14,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(12,27,17,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(13,28,19,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(14,29,18,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34'),(15,30,18,1,'','','','','','2022-12-30 15:11:34','2022-12-30 15:11:34');
/*!40000 ALTER TABLE `eis_param_bind_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_param_pool`
--

DROP TABLE IF EXISTS `eis_param_pool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_param_pool` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '参数名',
  `param_type` tinyint NOT NULL DEFAULT '0' COMMENT '参数类型，如1 全局公参，2事件公参，3对象标准私参，4.对象业务私参',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code` (`app_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数池信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_param_pool`
--

LOCK TABLES `eis_param_pool` WRITE;
/*!40000 ALTER TABLE `eis_param_pool` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_param_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_param_value`
--

DROP TABLE IF EXISTS `eis_param_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_param_value` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `param_id` bigint NOT NULL DEFAULT '0' COMMENT '参数ID',
  `code` varchar(64) NOT NULL DEFAULT '' COMMENT '参数名',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '参数中文名称',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_paramid_code` (`app_id`,`param_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数值信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_param_value`
--

LOCK TABLES `eis_param_value` WRITE;
/*!40000 ALTER TABLE `eis_param_value` DISABLE KEYS */;
INSERT INTO `eis_param_value` VALUES (12,18,'.*','.*',1,'.*','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(13,13,'.*','json格式字符串',1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(14,14,'.*','.*',1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(15,17,'.*','.*',1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(16,16,'.*','.*',1,'设备标识：https://www.theiphonewiki.com/wiki/Models','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(17,11,'.*','.*',1,'','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(18,12,'^[0-9]*$','数字格式',1,'- 用户交互深度，每次交互（点击/滑动等）都会+1 - 在当前根page的一次曝光生命周期内自增，如果页面重新曝光了，则先置0 - 页面曝光，不自增','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(19,15,'^[0-9]*$','数字格式',1,'ms','','','','','2022-12-30 15:11:13','2022-12-30 15:11:13'),(20,20,'.*','*',1,'unixtime','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(21,21,'.*','*',1,'系统版本','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(22,22,'.*','*',1,'app版本','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(23,23,'.*','*',1,'ip','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(24,24,'.*','*',1,'操作系统','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(25,25,'.*','*',1,'deviceId','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(26,26,'.*','*',1,'请求traceID','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(27,27,'.*','*',1,'与s_ctype类型对应的id','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(28,28,'image','image',1,'图片','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(29,28,'song','song',1,'歌曲','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(30,29,'.*','*',1,'请求traceID','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12');
/*!40000 ALTER TABLE `eis_param_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_realtime_branch_ignore`
--

DROP TABLE IF EXISTS `eis_realtime_branch_ignore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_realtime_branch_ignore` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversationId` varchar(20) DEFAULT NULL COMMENT '会话id',
  `branchKey` varchar(500) DEFAULT NULL COMMENT '分支key',
  `content` varchar(500) DEFAULT NULL COMMENT '分支信息JSON',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversationId_branchKey` (`conversationId`,`branchKey`),
  KEY `idx_conversationId_createTime` (`conversationId`,`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实时测试分支忽略情况';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_realtime_branch_ignore`
--

LOCK TABLES `eis_realtime_branch_ignore` WRITE;
/*!40000 ALTER TABLE `eis_realtime_branch_ignore` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_realtime_branch_ignore` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_realtime_test_record`
--

DROP TABLE IF EXISTS `eis_realtime_test_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_realtime_test_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'session id',
  `userId` bigint NOT NULL DEFAULT '0' COMMENT '测试uid',
  `userName` varchar(60) NOT NULL DEFAULT '' COMMENT '测试用户名',
  `reqName` varchar(60) NOT NULL DEFAULT '' COMMENT '测试需求',
  `baseVersion` varchar(60) NOT NULL DEFAULT '' COMMENT '测试基准版本',
  `terminal` varchar(60) NOT NULL DEFAULT '' COMMENT '测试终端',
  `appVersion` varchar(60) NOT NULL DEFAULT '' COMMENT 'app版本',
  `failedNum` bigint DEFAULT '0' COMMENT '失败数量',
  `status` int NOT NULL DEFAULT '0' COMMENT '(1:初始化 2:测试中 -3:测试完成)',
  `extInfo` varchar(1024) DEFAULT '' COMMENT '扩展信息',
  `saveTime` bigint NOT NULL DEFAULT '0' COMMENT '记录保存时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `appId` bigint NOT NULL DEFAULT '0' COMMENT '应用id',
  `taskId` bigint DEFAULT '0',
  `testResult` int DEFAULT '0' COMMENT '测试结果（0-未通过，1-通过，2-部分通过）',
  PRIMARY KEY (`id`),
  KEY `idx_userId_time` (`userId`,`saveTime`),
  KEY `idx_req_time` (`reqName`,`saveTime`),
  KEY `idx_bas_time` (`baseVersion`,`saveTime`),
  KEY `idx_ter_time` (`terminal`,`saveTime`),
  KEY `idx_saveTime` (`saveTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试会话记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_realtime_test_record`
--

LOCK TABLES `eis_realtime_test_record` WRITE;
/*!40000 ALTER TABLE `eis_realtime_test_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_realtime_test_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_release_relation`
--

DROP TABLE IF EXISTS `eis_release_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_release_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_release_id` bigint DEFAULT NULL COMMENT '父空间发布id',
  `release_id` bigint DEFAULT NULL COMMENT '子空间发布id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_release_id_parent_release_id` (`release_id`,`parent_release_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发布ID关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_release_relation`
--

LOCK TABLES `eis_release_relation` WRITE;
/*!40000 ALTER TABLE `eis_release_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_release_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_req_obj_relation`
--

DROP TABLE IF EXISTS `eis_req_obj_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_req_obj_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `obj_id` bigint NOT NULL COMMENT '对象id',
  `req_pool_id` bigint NOT NULL COMMENT '需求组id',
  `parent_obj_id` bigint DEFAULT NULL COMMENT '父对象id',
  `terminal_id` bigint NOT NULL COMMENT '该血缘属于的端id',
  `app_id` bigint NOT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='需求内的对象父子血缘';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_req_obj_relation`
--

LOCK TABLES `eis_req_obj_relation` WRITE;
/*!40000 ALTER TABLE `eis_req_obj_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_req_obj_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_req_pool`
--

DROP TABLE IF EXISTS `eis_req_pool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_req_pool` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `data_owners` varchar(200) DEFAULT NULL COMMENT '数据负责人，会有多个',
  `description` mediumtext COMMENT '描述',
  `editable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可编辑',
  `app_id` bigint NOT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique__index` (`app_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='需求组基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_req_pool`
--

LOCK TABLES `eis_req_pool` WRITE;
/*!40000 ALTER TABLE `eis_req_pool` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_req_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_req_pool_event`
--

DROP TABLE IF EXISTS `eis_req_pool_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_req_pool_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint NOT NULL COMMENT '关联需求组id',
  `event_bury_point_id` bigint NOT NULL COMMENT '事件埋点表id，关联表eis_event_bury_point',
  `event_id` bigint NOT NULL COMMENT '关联事件id（冗余），关联表eis_event',
  `terminal_id` bigint DEFAULT '0' COMMENT '关联终端id',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='埋点设计页——事件埋点待办项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_req_pool_event`
--

LOCK TABLES `eis_req_pool_event` WRITE;
/*!40000 ALTER TABLE `eis_req_pool_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_req_pool_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_req_pool_rel_base_release`
--

DROP TABLE IF EXISTS `eis_req_pool_rel_base_release`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_req_pool_rel_base_release` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint NOT NULL COMMENT '需求组id',
  `terminal_id` bigint NOT NULL COMMENT '终端id',
  `base_release_id` bigint NOT NULL DEFAULT '0' COMMENT '基线发布版本id，关联表eis_terminal_release_history的主键',
  `current_use` tinyint DEFAULT '1' COMMENT '该记录是否当前使用，1代表正在使用，0代表历史使用记录',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `auto_rebase` tinyint DEFAULT '1' COMMENT '是否自动变基，1代表是，0代表不是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='需求组各端关联基线版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_req_pool_rel_base_release`
--

LOCK TABLES `eis_req_pool_rel_base_release` WRITE;
/*!40000 ALTER TABLE `eis_req_pool_rel_base_release` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_req_pool_rel_base_release` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_req_pool_spm`
--

DROP TABLE IF EXISTS `eis_req_pool_spm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_req_pool_spm` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint NOT NULL COMMENT '关联需求组id，关联表eis_req_pool',
  `spm_by_obj_id` varchar(200) NOT NULL DEFAULT '' COMMENT '用对象id构成的spm，代表一个待办项在血缘图中的"坐标"',
  `obj_id` bigint NOT NULL COMMENT '对象id，关联表 eis_obj_basic',
  `obj_history_id` bigint NOT NULL COMMENT '对象变更历史记录id，关联表eis_obj_change_history',
  `terminal_id` bigint NOT NULL COMMENT '关联端id，关联表eis_terminal',
  `req_pool_type` int NOT NULL COMMENT '待办项类型：1:待开发 2:待下线',
  `req_type` varchar(64) NOT NULL COMMENT '需求类型：详细见RequirementTypeEnum枚举类',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `bridge_obj_id` bigint DEFAULT NULL COMMENT '桥梁对象id，如果是桥梁SPM，则bridge是spm_by_obj_id父子空间分界点',
  `bridge_app_id` bigint DEFAULT NULL COMMENT '桥梁对象所属appId',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='埋点设计页spm待办项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_req_pool_spm`
--

LOCK TABLES `eis_req_pool_spm` WRITE;
/*!40000 ALTER TABLE `eis_req_pool_spm` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_req_pool_spm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_req_task`
--

DROP TABLE IF EXISTS `eis_req_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_req_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `requirement_id` bigint NOT NULL COMMENT '关联需求id，来自表eis_requirement_into',
  `task_issue_key` varchar(32) DEFAULT NULL COMMENT '任务在overmind上的key',
  `req_issue_key` varchar(32) DEFAULT NULL COMMENT '关联需求在overmind上的key',
  `task_name` varchar(64) NOT NULL,
  `terminal_id` bigint DEFAULT NULL COMMENT '任务关联端id，来自表eis_terminal',
  `terminal_version` varchar(64) DEFAULT NULL COMMENT '端版本名，来自eis_terminal_version_info',
  `terminal_release_id` bigint DEFAULT NULL COMMENT '关联发布版本id，当任务发布时会填入，\n关联表eis_terminal_release_history',
  `iteration` varchar(64) DEFAULT NULL COMMENT '迭代号（om字段）',
  `status` int NOT NULL COMMENT '任务状态',
  `app_id` bigint DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='需求任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_req_task`
--

LOCK TABLES `eis_req_task` WRITE;
/*!40000 ALTER TABLE `eis_req_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_req_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_requirement_info`
--

DROP TABLE IF EXISTS `eis_requirement_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_requirement_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `req_issue_key` varchar(64) DEFAULT NULL COMMENT '需求在overmind的key',
  `req_name` varchar(200) NOT NULL COMMENT '需求名称',
  `req_pool_id` bigint DEFAULT '0' COMMENT '需求所属的需求组id',
  `source` int NOT NULL COMMENT '来源：1 ——overmind，2——自定义',
  `priority` varchar(10) DEFAULT NULL COMMENT '优先级（om字段）',
  `business_area` varchar(256) DEFAULT NULL COMMENT '业务领域（om字段）',
  `views` varchar(1024) DEFAULT NULL COMMENT '视图（om字段）',
  `team` varchar(256) DEFAULT NULL COMMENT '所属团队（om字段）',
  `om_state` int DEFAULT NULL COMMENT 'om状态（om字段）',
  `description` mediumtext,
  `app_id` bigint DEFAULT NULL,
  `owner_email` varchar(64) DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='需求基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_requirement_info`
--

LOCK TABLES `eis_requirement_info` WRITE;
/*!40000 ALTER TABLE `eis_requirement_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_requirement_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_role`
--

DROP TABLE IF EXISTS `eis_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `role_name` varchar(128) NOT NULL DEFAULT '' COMMENT '角色名',
  `role_level` int DEFAULT NULL COMMENT '角色等级',
  `role_type` int NOT NULL COMMENT '0-platform，1-domain，2-app',
  `type_id` bigint DEFAULT NULL COMMENT '如果type为app，则为appId，为domain则为domainId，为platform则默认为-1',
  `builtIn` bit(1) NOT NULL COMMENT '0-内置角色，1-自定义角色',
  `description` varchar(255) DEFAULT '' COMMENT '角色描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_role`
--

LOCK TABLES `eis_role` WRITE;
/*!40000 ALTER TABLE `eis_role` DISABLE KEYS */;
INSERT INTO `eis_role` VALUES (1,'超级管理员',0,0,-1,_binary '','内置角色','2022-12-30 14:58:10','2022-12-30 14:58:10'),(2,'域负责人',1,1,NULL,_binary '','内置角色','2022-12-30 14:58:10','2022-12-30 14:58:10'),(3,'域管理员',2,1,NULL,_binary '','内置角色','2022-12-30 14:58:10','2022-12-30 14:58:10'),(4,'域普通用户',3,2,NULL,_binary '','内置角色','2022-12-30 14:58:10','2022-12-30 14:58:10'),(5,'产品管理员',4,2,NULL,_binary '','内置角色','2022-12-30 14:58:10','2022-12-30 14:58:10'),(6,'产品普通用户',5,0,NULL,_binary '','内置角色','2022-12-30 14:58:10','2022-12-30 14:58:10');
/*!40000 ALTER TABLE `eis_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_role_auth`
--

DROP TABLE IF EXISTS `eis_role_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_role_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `role_id` bigint NOT NULL COMMENT '角色id',
  `auth_id` bigint NOT NULL COMMENT '权限id',
  `flag` int DEFAULT NULL COMMENT '标志位，用以细分权限，将来扩展用，比如需要细分某个资源的增删改查权限时，可以存4位二进制',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_auth` (`role_id`,`auth_id`)
) ENGINE=InnoDB AUTO_INCREMENT=343 DEFAULT CHARSET=utf8 COMMENT='角色权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_role_auth`
--

LOCK TABLES `eis_role_auth` WRITE;
/*!40000 ALTER TABLE `eis_role_auth` DISABLE KEYS */;
INSERT INTO `eis_role_auth` VALUES (1,1,1,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(2,1,2,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(3,1,3,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(4,1,4,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(5,1,5,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(6,1,6,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(7,1,7,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(8,1,8,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(9,1,9,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(10,1,10,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(11,1,11,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(12,1,12,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(13,1,13,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(14,1,14,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(15,1,15,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(16,1,16,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(17,1,17,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(18,1,18,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(19,1,19,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(20,1,20,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(21,1,21,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(22,1,22,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(23,1,23,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(24,1,24,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(25,1,25,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(26,1,26,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(27,1,27,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(28,1,28,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(29,1,29,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(30,1,30,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(31,1,31,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(32,1,32,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(33,1,33,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(34,1,34,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(35,1,35,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(36,1,36,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(37,1,37,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(38,1,38,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(39,1,39,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(40,1,40,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(41,1,41,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(42,1,42,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(43,1,43,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(44,1,44,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(45,1,45,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(46,1,46,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(47,1,47,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(48,1,48,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(49,1,49,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(50,1,50,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(51,1,51,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(52,1,52,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(53,1,53,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(54,1,54,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(55,1,55,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(56,1,56,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(57,1,57,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(58,1,58,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(59,1,59,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(60,1,60,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(61,1,61,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(62,1,62,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(63,1,63,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(64,1,64,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(65,1,65,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(66,1,66,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(67,1,67,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(68,1,68,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(69,1,69,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(70,1,70,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(71,1,71,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(72,1,72,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(73,1,73,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(74,1,74,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(75,1,75,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(76,1,76,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(77,1,77,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(78,1,78,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(79,1,79,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(80,1,80,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(81,1,81,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(82,1,82,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(83,1,83,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(84,1,84,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(85,1,85,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(86,1,86,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(87,1,87,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(88,1,88,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(89,1,89,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(90,1,90,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(91,1,92,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(92,1,93,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(93,1,94,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(94,1,95,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(95,2,1,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(96,2,2,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(97,2,3,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(98,2,4,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(99,2,5,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(100,2,6,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(101,2,7,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(102,2,8,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(103,2,9,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(104,2,10,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(105,2,11,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(106,2,12,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(107,2,13,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(108,2,14,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(109,2,15,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(110,2,16,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(111,2,17,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(112,2,18,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(113,2,19,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(114,2,20,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(115,2,21,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(116,2,22,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(117,2,23,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(118,2,24,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(119,2,25,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(120,2,26,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(121,2,27,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(122,2,28,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(123,2,29,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(124,2,30,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(125,2,31,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(126,2,32,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(127,2,33,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(128,2,34,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(129,2,35,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(130,2,36,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(131,2,37,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(132,2,38,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(133,2,39,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(134,2,40,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(135,2,41,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(136,2,42,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(137,2,43,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(138,2,44,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(139,2,45,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(140,2,46,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(141,2,47,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(142,2,48,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(143,2,49,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(144,2,50,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(145,2,51,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(146,2,52,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(147,2,53,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(148,2,54,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(149,2,55,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(150,2,56,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(151,2,57,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(152,2,58,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(153,2,59,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(154,2,60,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(155,2,61,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(156,2,62,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(157,2,63,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(158,2,64,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(159,2,65,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(160,2,66,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(161,2,67,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(162,2,68,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(163,2,69,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(164,2,70,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(165,2,71,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(166,2,72,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(167,2,73,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(168,2,74,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(169,2,75,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(170,2,76,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(171,2,77,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(172,2,78,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(173,2,79,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(174,2,80,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(175,2,81,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(176,2,82,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(177,2,83,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(178,2,92,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(179,2,93,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(180,2,94,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(181,2,95,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(182,3,1,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(183,3,2,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(184,3,3,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(185,3,4,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(186,3,5,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(187,3,6,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(188,3,7,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(189,3,8,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(190,3,9,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(191,3,10,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(192,3,11,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(193,3,12,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(194,3,13,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(195,3,14,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(196,3,15,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(197,3,16,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(198,3,17,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(199,3,18,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(200,3,19,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(201,3,20,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(202,3,21,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(203,3,22,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(204,3,23,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(205,3,24,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(206,3,25,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(207,3,26,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(208,3,27,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(209,3,28,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(210,3,29,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(211,3,30,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(212,3,31,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(213,3,32,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(214,3,33,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(215,3,34,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(216,3,35,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(217,3,36,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(218,3,37,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(219,3,38,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(220,3,39,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(221,3,40,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(222,3,41,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(223,3,42,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(224,3,43,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(225,3,44,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(226,3,45,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(227,3,46,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(228,3,47,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(229,3,48,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(230,3,49,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(231,3,50,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(232,3,51,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(233,3,52,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(234,3,53,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(235,3,54,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(236,3,55,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(237,3,56,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(238,3,57,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(239,3,58,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(240,3,59,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(241,3,60,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(242,3,61,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(243,3,62,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(244,3,63,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(245,3,64,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(246,3,65,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(247,3,66,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(248,3,67,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(249,3,68,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(250,3,69,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(251,3,70,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(252,3,71,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(253,3,72,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(254,3,73,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(255,3,74,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(256,3,75,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(257,3,76,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(258,3,77,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(259,3,78,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(260,3,79,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(261,3,80,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(262,3,81,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(263,3,82,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(264,3,83,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(265,3,92,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(266,3,93,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(267,3,94,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(268,3,95,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(269,5,1,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(270,5,2,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(271,5,3,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(272,5,4,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(273,5,5,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(274,5,6,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(275,5,7,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(276,5,8,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(277,5,9,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(278,5,10,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(279,5,11,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(280,5,12,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(281,5,13,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(282,5,14,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(283,5,15,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(284,5,16,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(285,5,17,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(286,5,18,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(287,5,19,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(288,5,20,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(289,5,21,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(290,5,22,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(291,5,23,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(292,5,24,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(293,5,25,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(294,5,26,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(295,5,27,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(296,5,28,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(297,5,29,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(298,5,30,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(299,5,31,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(300,5,32,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(301,5,33,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(302,5,34,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(303,5,35,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(304,5,36,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(305,5,37,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(306,5,38,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(307,5,39,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(308,5,40,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(309,5,41,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(310,5,42,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(311,5,43,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(312,5,44,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(313,5,45,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(314,5,46,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(315,5,47,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(316,5,48,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(317,5,49,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(318,5,50,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(319,5,51,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(320,5,52,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(321,5,53,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(322,5,54,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(323,5,55,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(324,5,56,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(325,5,57,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(326,5,58,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(327,5,59,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(328,5,60,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(329,5,61,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(330,5,62,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(331,5,63,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(332,5,64,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(333,5,65,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(334,5,66,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(335,5,67,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(336,5,68,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(337,5,69,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(338,5,91,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(339,5,92,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(340,5,93,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(341,5,94,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22'),(342,5,95,NULL,'2022-12-30 15:04:22','2022-12-30 15:04:22');
/*!40000 ALTER TABLE `eis_role_auth` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_rule_template`
--

DROP TABLE IF EXISTS `eis_rule_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_rule_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_rule_template`
--

LOCK TABLES `eis_rule_template` WRITE;
/*!40000 ALTER TABLE `eis_rule_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_rule_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_session`
--

DROP TABLE IF EXISTS `eis_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_session` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `domain_id` bigint NOT NULL DEFAULT '0' COMMENT '域ID',
  `token` varchar(64) NOT NULL DEFAULT '' COMMENT '凭证',
  `user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户id',
  `expire_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_token` (`domain_id`,`token`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会话信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_session`
--

LOCK TABLES `eis_session` WRITE;
/*!40000 ALTER TABLE `eis_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_spm_info`
--

DROP TABLE IF EXISTS `eis_spm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_spm_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `spm` varchar(255) NOT NULL COMMENT 'spm',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '映射状态，详情见SpmMapStatusEnum',
  `version` varchar(64) DEFAULT NULL COMMENT '映射生效版本',
  `note` varchar(255) DEFAULT NULL COMMENT 'SPM映射备注',
  `app_id` bigint NOT NULL COMMENT '产品信息',
  `terminal_id` bigint DEFAULT NULL COMMENT '终端ID',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `source` tinyint NOT NULL DEFAULT '0' COMMENT 'spm来源（0-任务同步，1-手动添加）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='spm映射信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_spm_info`
--

LOCK TABLES `eis_spm_info` WRITE;
/*!40000 ALTER TABLE `eis_spm_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_spm_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_spm_map_info`
--

DROP TABLE IF EXISTS `eis_spm_map_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_spm_map_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增Id',
  `spm` varchar(1000) NOT NULL DEFAULT '' COMMENT '不带pos的spm',
  `spm_name` varchar(1000) NOT NULL DEFAULT '' COMMENT '由链路上每个对象名称和竖线组成，当前对象在第一个位置',
  `spm_old` varchar(1000) DEFAULT NULL COMMENT '配置的老埋点SPMID，可为空，多个时需要逐条拆分',
  `platform` varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品类型',
  `spm_status` int NOT NULL DEFAULT '0' COMMENT '每个spm的流转状态',
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
  `spm_check_status` tinyint DEFAULT '1' COMMENT '埋点映射状态，1为待确认，2为双打预发，3为单打预发，4为已上线',
  `spm_app_ver` varchar(64) DEFAULT NULL COMMENT '埋点映射生效版本',
  `tag` varchar(64) DEFAULT NULL COMMENT 'spm标签',
  `is_deployed` tinyint DEFAULT '0' COMMENT 'SPM是否已上过线，0为未上过线，1为上过线',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_spm_map_info`
--

LOCK TABLES `eis_spm_map_info` WRITE;
/*!40000 ALTER TABLE `eis_spm_map_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_spm_map_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_spm_map_item`
--

DROP TABLE IF EXISTS `eis_spm_map_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_spm_map_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `spm_id` bigint NOT NULL COMMENT 'eis_spm_map_info表中的主键ID',
  `spm_old` varchar(255) NOT NULL COMMENT '老埋点SPN字符串',
  `create_email` varchar(64) DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `spm_id_spm_old_uidx` (`spm_id`,`spm_old`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='新旧spm映射关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_spm_map_item`
--

LOCK TABLES `eis_spm_map_item` WRITE;
/*!40000 ALTER TABLE `eis_spm_map_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_spm_map_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_spm_tag`
--

DROP TABLE IF EXISTS `eis_spm_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_spm_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `spm_id` bigint NOT NULL COMMENT 'eis_spm_map_info表中的主键ID',
  `tag_id` bigint NOT NULL COMMENT 'eis_tag表中的主键ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `spm_tag_unidx` (`spm_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='spm标签绑定信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_spm_tag`
--

LOCK TABLES `eis_spm_tag` WRITE;
/*!40000 ALTER TABLE `eis_spm_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_spm_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_tag`
--

DROP TABLE IF EXISTS `eis_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '标签名称',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '标签类型，1为对象标签，2为spm标签',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_name_type` (`app_id`,`name`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_tag`
--

LOCK TABLES `eis_tag` WRITE;
/*!40000 ALTER TABLE `eis_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_task_process`
--

DROP TABLE IF EXISTS `eis_task_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_task_process` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `req_pool_id` bigint NOT NULL COMMENT '关联需求组id，关联表eis_req_pool',
  `req_pool_entity_id` bigint DEFAULT NULL COMMENT '关联需求组待办项id,关联表eis_req_pool_spm、\neis_req_pool_event',
  `req_pool_type` int NOT NULL COMMENT '需求组待办项类型，1:对象埋点spm待开发项,2:对象埋点spm待下线项\n3:事件埋点待开发项',
  `task_id` bigint NOT NULL COMMENT '关联任务id，关联表eis_req_task',
  `status` int NOT NULL,
  `spm_by_obj_id` varchar(256) DEFAULT NULL COMMENT '冗余由对象id构成的spm',
  `obj_id` bigint DEFAULT NULL COMMENT '冗余对象id',
  `event_id` bigint DEFAULT NULL COMMENT '冗余事件id',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务下的关联流程';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_task_process`
--

LOCK TABLES `eis_task_process` WRITE;
/*!40000 ALTER TABLE `eis_task_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_task_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_template`
--

DROP TABLE IF EXISTS `eis_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '模板名称',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_name` (`app_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模板信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_template`
--

LOCK TABLES `eis_template` WRITE;
/*!40000 ALTER TABLE `eis_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_terminal`
--

DROP TABLE IF EXISTS `eis_terminal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_terminal` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '终端名称',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '终端类型，1表示PC, 2表示无线',
  `preset` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否预置，1表示预置的，0表示非预置',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_appid_code_type` (`app_id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='终端信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_terminal`
--

LOCK TABLES `eis_terminal` WRITE;
/*!40000 ALTER TABLE `eis_terminal` DISABLE KEYS */;
INSERT INTO `eis_terminal` VALUES (1,'Android',2,1,1,'预置终端Android','SYSTEM','SYSTEM','SYSTEM','SYSTEM','2022-12-30 14:58:10','2022-12-30 14:58:10'),(2,'iPhone',2,1,1,'预置终端iPhone','SYSTEM','SYSTEM','SYSTEM','SYSTEM','2022-12-30 14:58:10','2022-12-30 14:58:10'),(3,'Web',2,1,1,'预置终端Web','SYSTEM','SYSTEM','SYSTEM','SYSTEM','2022-12-30 14:58:10','2022-12-30 14:58:10');
/*!40000 ALTER TABLE `eis_terminal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_terminal_release_history`
--

DROP TABLE IF EXISTS `eis_terminal_release_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_terminal_release_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `terminal_id` bigint NOT NULL DEFAULT '0' COMMENT '关联端id',
  `app_id` bigint NOT NULL,
  `pre_release_id` bigint NOT NULL DEFAULT '0' COMMENT '上一发布记录id',
  `latest` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否是该端下最新发布记录',
  `terminal_version_id` bigint NOT NULL DEFAULT '0' COMMENT '端版本id',
  `create_email` varchar(64) DEFAULT '',
  `create_name` varchar(64) DEFAULT '',
  `update_email` varchar(64) DEFAULT '',
  `update_name` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='各端发布记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_terminal_release_history`
--

LOCK TABLES `eis_terminal_release_history` WRITE;
/*!40000 ALTER TABLE `eis_terminal_release_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_terminal_release_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_terminal_version_info`
--

DROP TABLE IF EXISTS `eis_terminal_version_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_terminal_version_info` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `num` varchar(255) NOT NULL DEFAULT '' COMMENT '端版本号',
  `name` varbinary(250) NOT NULL COMMENT '端版本名称',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='端版本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_terminal_version_info`
--

LOCK TABLES `eis_terminal_version_info` WRITE;
/*!40000 ALTER TABLE `eis_terminal_version_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `eis_terminal_version_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_user`
--

DROP TABLE IF EXISTS `eis_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `email` varchar(255) NOT NULL COMMENT '邮箱',
  `user_name` varchar(255) DEFAULT NULL COMMENT '中文名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_USER_EMAIL` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2147483648 DEFAULT CHARSET=utf8 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_user`
--

LOCK TABLES `eis_user` WRITE;
/*!40000 ALTER TABLE `eis_user` DISABLE KEYS */;
INSERT INTO `eis_user` VALUES (2147483647,'SYSTEM','SYSTEM','2022-12-30 14:58:10','2022-12-30 14:58:10');
/*!40000 ALTER TABLE `eis_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_user_role`
--

DROP TABLE IF EXISTS `eis_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `role_type` int NOT NULL COMMENT '0-platform，1-domain，2-app',
  `type_id` bigint NOT NULL COMMENT '如果type为app，则为appId，为domain则为domainId，为platform则默认为-1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_user_role` (`role_type`,`type_id`,`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='用户和角色的关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_user_role`
--

LOCK TABLES `eis_user_role` WRITE;
/*!40000 ALTER TABLE `eis_user_role` DISABLE KEYS */;
INSERT INTO `eis_user_role` VALUES (1,2147483647,1,0,-1,'2022-12-30 15:05:26','2022-12-30 15:05:26'),(2,2147483647,2,1,1,'2022-12-30 15:05:26','2022-12-30 15:05:26'),(3,2147483647,3,1,1,'2022-12-30 15:05:26','2022-12-30 15:05:26'),(4,2147483647,4,2,1,'2022-12-30 15:05:26','2022-12-30 15:05:26'),(5,2147483647,5,2,1,'2022-12-30 15:05:26','2022-12-30 15:05:26'),(6,2147483647,6,0,-1,'2022-12-30 15:05:26','2022-12-30 15:05:26');
/*!40000 ALTER TABLE `eis_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eis_version`
--

DROP TABLE IF EXISTS `eis_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eis_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '版本名称',
  `version_source` tinyint NOT NULL DEFAULT '0' COMMENT '版本来源，如1表示jira, 2表示overmind, 3表示手动创建',
  `entity_id` bigint NOT NULL DEFAULT '0' COMMENT '关联元素ID(部分版本非全局概念，而是与关联元素构成版本),如终端ID，或参数ID，或对象ID, 默认为0',
  `entity_type` tinyint NOT NULL DEFAULT '0' COMMENT '关联元素类型(部分版本非全局概念，而是与关联元素构成版本),如1终端，2事件类型， 3关联对象， 4参数模板，默认为0',
  `current_using` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是当前使用版本,1表示是当前使用版本，0表示非当前使用版本',
  `preset` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否预置，1表示预置，0表示非预置',
  `app_id` bigint NOT NULL DEFAULT '0' COMMENT '产品ID',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
  `create_email` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人邮箱',
  `create_name` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人名称',
  `update_email` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的邮箱',
  `update_name` varchar(64) NOT NULL DEFAULT '' COMMENT '最近更新人的名称',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_appid_entityid` (`app_id`,`entity_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='元数据的版本信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eis_version`
--

LOCK TABLES `eis_version` WRITE;
/*!40000 ALTER TABLE `eis_version` DISABLE KEYS */;
INSERT INTO `eis_version` VALUES (11,'V1',3,11,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(12,'V1',3,12,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(13,'V1',3,13,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(14,'V1',3,14,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(15,'V1',3,15,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(16,'V1',3,16,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(17,'V1',3,17,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(18,'V1',3,18,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(19,'V1',3,19,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12'),(20,'V1',3,20,2,0,1,1,'','','','','','2022-12-30 15:11:12','2022-12-30 15:11:12');
/*!40000 ALTER TABLE `eis_version` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-12-30 23:24:20
