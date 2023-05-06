package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_event_bury_point")
public class EisEventBuryPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "req_pool_id")
    private Long reqPoolId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_param_package_id")
    private Long eventParamPackageId;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "terminal_release_id")
    private Long terminalReleaseId;

    @Column(name = "terminal_param_package_id")
    private Long terminalParamPackageId;

    @Column(name = "extInfo")
    private String extInfo;

    @Column(name = "pre_id")
    private Long preId;

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

    public Long getReqPoolId() {
        return reqPoolId;
    }

    public void setReqPoolId(Long reqPoolId) {
        this.reqPoolId = reqPoolId;
    }

    /**
     * @return event_id
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * @param eventId
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * @return event_param_package_id
     */
    public Long getEventParamPackageId() {
        return eventParamPackageId;
    }

    /**
     * @param eventParamPackageId
     */
    public void setEventParamPackageId(Long eventParamPackageId) {
        this.eventParamPackageId = eventParamPackageId;
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

    public Long getTerminalReleaseId() {
        return terminalReleaseId;
    }

    public void setTerminalReleaseId(Long terminalReleaseId) {
        this.terminalReleaseId = terminalReleaseId;
    }

    /**
     * @return terminal_param_package_id
     */
    public Long getTerminalParamPackageId() {
        return terminalParamPackageId;
    }

    /**
     * @param terminalParamPackageId
     */
    public void setTerminalParamPackageId(Long terminalParamPackageId) {
        this.terminalParamPackageId = terminalParamPackageId;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }

    /**
     * @return pre_id
     */
    public Long getPreId() {
        return preId;
    }

    /**
     * @param preId
     */
    public void setPreId(Long preId) {
        this.preId = preId;
    }

    public String getCreateEmail() {
        return createEmail;
    }

    public void setCreateEmail(String createEmail) {
        this.createEmail = createEmail;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public String getUpdateEmail() {
        return updateEmail;
    }

    public void setUpdateEmail(String updateEmail) {
        this.updateEmail = updateEmail;
    }

    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}