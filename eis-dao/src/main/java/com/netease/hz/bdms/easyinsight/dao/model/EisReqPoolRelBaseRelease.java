package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_req_pool_rel_base_release")
public class EisReqPoolRelBaseRelease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "req_pool_id")
    private Long reqPoolId;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "base_release_id")
    private Long baseReleaseId;

    @Column(name = "current_use")
    private Boolean currentUse;

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

    @Column(name = "auto_rebase")
    private Boolean autoRebase;

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
     * @return base_release_id
     */
    public Long getBaseReleaseId() {
        return baseReleaseId;
    }

    /**
     * @param baseReleaseId
     */
    public void setBaseReleaseId(Long baseReleaseId) {
        this.baseReleaseId = baseReleaseId;
    }

    public Boolean getCurrentUse() {
        return currentUse;
    }

    public void setCurrentUse(Boolean currentUse) {
        this.currentUse = currentUse;
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

    public Boolean getAutoRebase() {
        return autoRebase;
    }

    public void setAutoRebase(Boolean autoRebase) {
        this.autoRebase = autoRebase;
    }
}