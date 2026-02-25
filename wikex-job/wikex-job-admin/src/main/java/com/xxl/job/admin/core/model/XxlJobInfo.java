package com.xxl.job.admin.core.model;

import java.util.Date;

/**
 * xxl-job info
 * 
 * @author william 2016-1-12 18:25:49
 */
public class XxlJobInfo {

	private int id; // Primary key ID

	private int jobGroup; // Executor primary key ID
	private String jobDesc;

	private Date addTime;
	private Date updateTime;

	private String author; // Responsible person
	private String alarmEmail; // Alarm email

	private String scheduleType; // Schedule type
	private String scheduleConf; // Schedule configuration, value meaning depends on schedule type
	private String misfireStrategy; // Schedule misfire strategy

	private String executorRouteStrategy; // Executor route strategy
	private String executorHandler; // Executor, task Handler name
	private String executorParam; // Executor, task parameters
	private String executorBlockStrategy; // Blocking handling strategy
	private int executorTimeout; // Task execution timeout, unit seconds
	private int executorFailRetryCount; // Failure retry count

	private String glueType; // GLUE type #com.xxl.job.core.glue.GlueTypeEnum
	private String glueSource; // GLUE source code
	private String glueRemark; // GLUE remark
	private Date glueUpdatetime; // GLUE update time

	private String childJobId; // Child job IDs, multiple IDs separated by commas

	private int triggerStatus; // Schedule status: 0 - stopped, 1 - running
	private long triggerLastTime; // Last trigger time
	private long triggerNextTime; // Next trigger time

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(int jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAlarmEmail() {
		return alarmEmail;
	}

	public void setAlarmEmail(String alarmEmail) {
		this.alarmEmail = alarmEmail;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getScheduleConf() {
		return scheduleConf;
	}

	public void setScheduleConf(String scheduleConf) {
		this.scheduleConf = scheduleConf;
	}

	public String getMisfireStrategy() {
		return misfireStrategy;
	}

	public void setMisfireStrategy(String misfireStrategy) {
		this.misfireStrategy = misfireStrategy;
	}

	public String getExecutorRouteStrategy() {
		return executorRouteStrategy;
	}

	public void setExecutorRouteStrategy(String executorRouteStrategy) {
		this.executorRouteStrategy = executorRouteStrategy;
	}

	public String getExecutorHandler() {
		return executorHandler;
	}

	public void setExecutorHandler(String executorHandler) {
		this.executorHandler = executorHandler;
	}

	public String getExecutorParam() {
		return executorParam;
	}

	public void setExecutorParam(String executorParam) {
		this.executorParam = executorParam;
	}

	public String getExecutorBlockStrategy() {
		return executorBlockStrategy;
	}

	public void setExecutorBlockStrategy(String executorBlockStrategy) {
		this.executorBlockStrategy = executorBlockStrategy;
	}

	public int getExecutorTimeout() {
		return executorTimeout;
	}

	public void setExecutorTimeout(int executorTimeout) {
		this.executorTimeout = executorTimeout;
	}

	public int getExecutorFailRetryCount() {
		return executorFailRetryCount;
	}

	public void setExecutorFailRetryCount(int executorFailRetryCount) {
		this.executorFailRetryCount = executorFailRetryCount;
	}

	public String getGlueType() {
		return glueType;
	}

	public void setGlueType(String glueType) {
		this.glueType = glueType;
	}

	public String getGlueSource() {
		return glueSource;
	}

	public void setGlueSource(String glueSource) {
		this.glueSource = glueSource;
	}

	public String getGlueRemark() {
		return glueRemark;
	}

	public void setGlueRemark(String glueRemark) {
		this.glueRemark = glueRemark;
	}

	public Date getGlueUpdatetime() {
		return glueUpdatetime;
	}

	public void setGlueUpdatetime(Date glueUpdatetime) {
		this.glueUpdatetime = glueUpdatetime;
	}

	public String getChildJobId() {
		return childJobId;
	}

	public void setChildJobId(String childJobId) {
		this.childJobId = childJobId;
	}

	public int getTriggerStatus() {
		return triggerStatus;
	}

	public void setTriggerStatus(int triggerStatus) {
		this.triggerStatus = triggerStatus;
	}

	public long getTriggerLastTime() {
		return triggerLastTime;
	}

	public void setTriggerLastTime(long triggerLastTime) {
		this.triggerLastTime = triggerLastTime;
	}

	public long getTriggerNextTime() {
		return triggerNextTime;
	}

	public void setTriggerNextTime(long triggerNextTime) {
		this.triggerNextTime = triggerNextTime;
	}
}
