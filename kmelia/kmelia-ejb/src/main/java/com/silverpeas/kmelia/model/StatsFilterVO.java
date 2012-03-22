package com.silverpeas.kmelia.model;

import java.util.Date;

public class StatsFilterVO {

  /**
   * the application identifier
   */
  private String instanceId;
  /**
   * the topic identifier
   */
  private Integer topicId;
  /**
   * the start time
   */
  private Date startDate;
  /**
   * the end time
   */
  private Date endDate;
  /**
   * the group identifier
   */
  private Integer groupId;

  public StatsFilterVO() {
  }

  /**
   * @param instanceId
   * @param topicId
   * @param startDate
   * @param endDate
   */
  public StatsFilterVO(String instanceId, Integer topicId, Date startDate, Date endDate) {
    super();
    this.instanceId = instanceId;
    this.topicId = topicId;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * @return the instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * @return the topicId
   */
  public Integer getTopicId() {
    return topicId;
  }

  /**
   * @param topicId the topicId to set
   */
  public void setTopicId(Integer topicId) {
    this.topicId = topicId;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the groupId
   */
  public Integer getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

}
