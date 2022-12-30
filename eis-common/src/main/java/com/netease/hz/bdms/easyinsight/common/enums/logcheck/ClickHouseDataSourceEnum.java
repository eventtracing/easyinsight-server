package com.netease.hz.bdms.easyinsight.common.enums.logcheck;

public enum ClickHouseDataSourceEnum {

    LOG("Music_EasyInsight_ClientLogBasic_local", "Music_EasyInsight_ClientLogBasic_shard"),
    LOG_MV("Music_EasyInsight_ClientLogBasic_mv_local", "Music_EasyInsight_ClientLogBasic_mv_shard"),
    BRANCH_COVERAGE("Music_EasyInsight_ClientLogBranchCoverage_local", "Music_EasyInsight_ClientLogBranchCoverage_shard"),
    ;

    private String localTable;
    private String shardTable;

    public String getLocalTable() {
        return localTable;
    }

    public String getShardTable() {
        return shardTable;
    }

    ClickHouseDataSourceEnum(String localTable, String shardTable) {
        this.localTable = localTable;
        this.shardTable = shardTable;
    }
}
