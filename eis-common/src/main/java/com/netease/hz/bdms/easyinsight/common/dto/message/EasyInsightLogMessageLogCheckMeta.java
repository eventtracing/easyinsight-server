package com.netease.hz.bdms.easyinsight.common.dto.message;

import lombok.Data;

/**
 * 稽查基本元数据
 */
@Data
public class EasyInsightLogMessageLogCheckMeta {

    String pt_build_type; //根据buildType字段进行分区，目前分为debug(包含debug+dev)，release(包含release+ beta)

    String user_id;	  //用户ID

    String device_id;  //设备ID

    String ip;  //IP

    String app_ver;//app版本号

    String os;  //终端类型

    String os_ver;  //os版本号

    String net_status;  //网络状态，4g/wifi等

    Long client_timestamp;  //客户端时间(ms)

    Long server_timestamp;  //服务端时间(ms)

    String action;  //行为code，如播放、点击等

    String spm;  //_spm坑位（带位置）

    Long act_seq;  //元素访问序号

    Long pg_step;  //页面访问序号

    String scm;  //_scm

    Long duration;  //时长(ms)

    String sessid;  //_sessid冷启动生成的sessionid

    String sid_refer;  //_sidrefer上次冷启动生成的sessionid
}
