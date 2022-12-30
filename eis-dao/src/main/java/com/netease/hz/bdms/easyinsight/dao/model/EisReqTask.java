package com.netease.hz.bdms.easyinsight.dao.model;

import javax.persistence.*;
import java.util.Date;

@Table(name = "eis_req_task")
public class EisReqTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id")
    private Long requirementId;

    @Column(name = "task_issue_key")
    private String taskIssueKey;

    @Column(name = "req_issue_key")
    private String reqIssueKey;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "terminal_version")
    private String terminalVersion;

    @Column(name = "terminal_release_id")
    private Long terminalReleaseId;

    private String iteration;

    private Integer status;

    @Column(name = "app_id")
    private Long appId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "verifier_name")
    private String verifierName;

    @Column(name = "verifier_email")
    private String verifierEmail;

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
     * @return requirement_id
     */
    public Long getRequirementId() {
        return requirementId;
    }

    /**
     * @param requirementId
     */
    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    /**
     * @return task_issue_key
     */
    public String getTaskIssueKey() {
        return taskIssueKey;
    }

    /**
     * @param taskIssueKey
     */
    public void setTaskIssueKey(String taskIssueKey) {
        this.taskIssueKey = taskIssueKey;
    }

    /**
     * @return req_issue_key
     */
    public String getReqIssueKey() {
        return reqIssueKey;
    }

    /**
     * @param reqIssueKey
     */
    public void setReqIssueKey(String reqIssueKey) {
        this.reqIssueKey = reqIssueKey;
    }

    /**
     * @return task_name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return terminal_type
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
     * @return terminal_version
     */
    public String getTerminalVersion() {
        return terminalVersion;
    }

    /**
     * @param terminalVersion
     */
    public void setTerminalVersion(String terminalVersion) {
        this.terminalVersion = terminalVersion;
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
     * @return iteration
     */
    public String getIteration() {
        return iteration;
    }

    /**
     * @param iteration
     */
    public void setIteration(String iteration) {
        this.iteration = iteration;
    }

    /**
     * @return status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(Integer status) {
        this.status = status;
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
     * @return owner_name
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * @param ownerName
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /**
     * @return owner_email
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * @param ownerEmail
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * @return verifier_name
     */
    public String getVerifierName() {
        return verifierName;
    }

    /**
     * @param verifierName
     */
    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    /**
     * @return verifier_email
     */
    public String getVerifierEmail() {
        return verifierEmail;
    }

    /**
     * @param verifierEmail
     */
    public void setVerifierEmail(String verifierEmail) {
        this.verifierEmail = verifierEmail;
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