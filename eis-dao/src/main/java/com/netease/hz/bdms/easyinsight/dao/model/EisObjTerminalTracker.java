package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_obj_terminal_tracker")
public class EisObjTerminalTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obj_id")
    private Long objId;

    @Column(name = "obj_history_id")
    private Long objHistoryId;

    @Column(name = "req_pool_id")
    private Long reqPoolId;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "terminal_release_id")
    private Long terminalReleaseId;

    @Column(name = "pub_param_package_id")
    private Long pubParamPackageId;

    @Column(name = "pre_tracker_id")
    private Long preTrackerId;

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

    /**
     * @return terminal_release_id
     */
    public Long getTerminalReleaseId() {
        return terminalReleaseId;
    }

    /**
     * @param terminalReleaseId
     */
    public void setTerminalReleaseId(Long terminalReleaseId) {
        this.terminalReleaseId = terminalReleaseId;
    }

    /**
     * @return pub_param_package_id
     */
    public Long getPubParamPackageId() {
        return pubParamPackageId;
    }

    /**
     * @param pubParamPackageId
     */
    public void setPubParamPackageId(Long pubParamPackageId) {
        this.pubParamPackageId = pubParamPackageId;
    }

    /**
     * @return pre_tracker_id
     */
    public Long getPreTrackerId() {
        return preTrackerId;
    }

    /**
     * @param preTrackerId
     */
    public void setPreTrackerId(Long preTrackerId) {
        this.preTrackerId = preTrackerId;
    }

    /**
     * @return app_id
     */
    public Long getAppId() {
        return appId;
    }

    /**
     * @param appId
     */
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
}