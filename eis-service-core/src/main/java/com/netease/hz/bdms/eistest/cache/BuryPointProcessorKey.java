package com.netease.hz.bdms.eistest.cache;


import lombok.experimental.UtilityClass;


@UtilityClass
public class BuryPointProcessorKey {

    private static final String buryPointMetaKey = "easy:insight:metainfo:%s";

    private static final String buryPointUserKey = "easy:insight:userinfo:%s";

    private static final String buryPointStatisticsKey = "easy:insight:point:statistics:%s";

    private static final String buryPointExceptionStatisticsKey = "easy:insight:point:exception:statistics:%s";

    private static final String buryPointLogKey = "easy:insight:point:log:%s:%d";

    private static final String buryPointLogCountKey = "easy:insight:point:log:count:%s:%d";

    private static final String buryPointLogUserCountKey = "easy:insight:point:log:user:count:%s:%d";

    private static final String buryPointLogCheckErrorCountKey = "easy:insight:point:log:check:error:count:%s";

    private static final String userTestRecordStaKey = "easy:insight:point:test:record:sta:%s";

    public String getUserTestRecordStaKey(String code) {
        return String.format(userTestRecordStaKey, code);
    }


    public String getBuryPointMetaKey(String code) {
        return String.format(buryPointMetaKey, code);
    }

    public String getBuryPointUserKey(String code) {
        return String.format(buryPointUserKey, code);
    }

    public String getBuryPointStatisticsKey(String code){
        return String.format(buryPointStatisticsKey, code);
    }

    public String getBuryPointExceptionStatisticsKey(String code){
        return String.format(buryPointExceptionStatisticsKey, code);
    }

    public String getBuryPointLogKey(String code, int logType){
        return String.format(buryPointLogKey, code, logType);
    }

    public String getBuryPointLogCountKey(String code, int logType){
        return String.format(buryPointLogCountKey, code, logType);
    }

    public String getBuryPointLogUserCountKey(String code, int logType){
        return String.format(buryPointLogUserCountKey, code, logType);
    }

    public String getBuryPointLogCheckErrorCountKey(String code){
        return String.format(buryPointLogCheckErrorCountKey, code);
    }

}
