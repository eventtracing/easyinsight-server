package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_req_pool_spm")
public class EisReqPoolSpm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "req_pool_id")
    private Long reqPoolId;

    @Column(name = "spm_by_obj_id")
    private String spmByObjId;

    @Column(name = "obj_id")
    private Long objId;

    @Column(name = "bridge_obj_id")
    private Long bridgeObjId;

    @Column(name = "bridge_app_id")
    private Long bridgeAppId;

    @Column(name = "obj_history_id")
    private Long objHistoryId;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "req_pool_type")
    private Integer reqPoolType;

    @Column(name = "req_type")
    private String reqType;

    @Column(name = "app_id")
    private Long appId;

    @Column(name = "create_email")
    private String createEmail;

    @Column(name = "create_name")
    private String createName;

    @Column(name = "update_email")
    private String updateEmail;

    @Column(name = "update_name")
    private String updateName;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return req_id
     */
    public Long getReqPoolId() {
        return reqPoolId;
    }

    /**
     * @param reqPoolId
     */
    public void setReqPoolId(Long reqPoolId) {
        this.reqPoolId = reqPoolId;
    }

    /**
     * @return spm_by_obj_id
     */
    public String getSpmByObjId() {
        return spmByObjId;
    }

    /**
     * @param spmByObjId
     */
    public void setSpmByObjId(String spmByObjId) {
        this.spmByObjId = spmByObjId;
    }

    /**
     * @return obj_id
     */
    public Long getObjId() {
        return objId;
    }

    /**
     * @param objId
     */
    public void setObjId(Long objId) {
        this.objId = objId;
    }

    /**
     * @return obj_history_id
     */
    public Long getObjHistoryId() {
        return objHistoryId;
    }

    /**
     * @param objHistoryId
     */
    public void setObjHistoryId(Long objHistoryId) {
        this.objHistoryId = objHistoryId;
    }

    /**
     * @return terminal_id
     */
    public Long getTerminalId() {
        return terminalId;
    }

    /**
     * @param terminalId
     */
    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }

    public Integer getReqPoolType() {
        return reqPoolType;
    }

    public void setReqPoolType(Integer reqPoolType) {
        this.reqPoolType = reqPoolType;
    }

    /**
     * @return req_type
     */
    public String getReqType() {
        return reqType;
    }

    /**
     * @param reqType
     */
    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    /**
     * @return create_email
     */
    public String getCreateEmail() {
        return createEmail;
    }

    /**
     * @param createEmail
     */
    public void setCreateEmail(String createEmail) {
        this.createEmail = createEmail;
    }

    /**
     * @return create_name
     */
    public String getCreateName() {
        return createName;
    }

    /**
     * @param createName
     */
    public void setCreateName(String createName) {
        this.createName = createName;
    }

    /**
     * @return update_email
     */
    public String getUpdateEmail() {
        return updateEmail;
    }

    /**
     * @param updateEmail
     */
    public void setUpdateEmail(String updateEmail) {
        this.updateEmail = updateEmail;
    }

    /**
     * @return update_name
     */
    public String getUpdateName() {
        return updateName;
    }

    /**
     * @param updateName
     */
    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    /**
     * @return create_time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return update_time
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getBridgeObjId() {
        return bridgeObjId;
    }

    public void setBridgeObjId(Long bridgeObjId) {
        this.bridgeObjId = bridgeObjId;
    }

    public Long getBridgeAppId() {
        return bridgeAppId;
    }

    public void setBridgeAppId(Long bridgeAppId) {
        this.bridgeAppId = bridgeAppId;
    }
}