/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.delegatednews.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sc_delegatednews_new")
public class DelegatedNew implements java.io.Serializable {
	
	private static final long serialVersionUID = 9192830552642027995L;

	@Id
	private int pubId;
	@Column(name = "instanceId")
	private String instanceId;
	@Column(name = "status")
	private String status;
	@Column(name = "contributorId")
	private String contributorId;
	@Column(name = "validatorId")
	private String validatorId;
	@Column(name = "beginDate", columnDefinition = "TIMESTAMP")
	private Date beginDate;
	@Column(name = "endDate", columnDefinition = "TIMESTAMP")
	private Date endDate;
	
	public static final String VALID = "Valid";
	public static final String TO_VALIDATE = "ToValidate";
	public static final String REFUSED = "Unvalidate";
	  
	public DelegatedNew() {
    
	}

	public DelegatedNew(int pubId, String instanceId, 
			String contributorId) {
		super();
		this.pubId = pubId;
		this.instanceId = instanceId;
		this.contributorId = contributorId;
		this.status = TO_VALIDATE;
	}

	public int getPubId() {
		return pubId;
	}

	public void setPubId(int pubId) {
		this.pubId = pubId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getContributorId() {
		return contributorId;
	}

	public void setContributorId(String contributorId) {
		this.contributorId = contributorId;
	}

	public String getValidatorId() {
		return validatorId;
	}

	public void setValidatorId(String validatorId) {
		this.validatorId = validatorId;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
	    if (getClass() != obj.getClass()) {
	      return false;
	    }
	    final DelegatedNew other = (DelegatedNew) obj;
	    if (this.pubId != other.pubId) {
	        return false;
	      }
	    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(other.instanceId)) {
	    	return false;
	    }
	    if ((this.status == null) ? (other.status != null) : !this.status.equals(other.status)) {
	    	return false;
	    }
	    if ((this.contributorId == null) ? (other.contributorId != null) : !this.contributorId.equals(other.contributorId)) {
	    	return false;
	    }
	    if ((this.validatorId == null) ? (other.validatorId != null) : !this.validatorId.equals(other.validatorId)) {
	    	return false;
	    }
	    if ((this.beginDate == null) ? (other.beginDate != null) : !this.beginDate.equals(other.beginDate)) {
	    	return false;
	    }
	    if ((this.endDate == null) ? (other.endDate != null) : !this.endDate.equals(other.endDate)) {
	    	return false;
	    }
	    return true;
	  }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((beginDate == null) ? 0 : beginDate.hashCode());
		result = prime * result
				+ ((contributorId == null) ? 0 : contributorId.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result
				+ ((instanceId == null) ? 0 : instanceId.hashCode());
		result = prime * result + pubId;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((validatorId == null) ? 0 : validatorId.hashCode());
		return result;
	}


	  @Override
	  public String toString() {
		  return "DelegatedNew {" + "pubId=" + pubId + ", instanceId=" + instanceId + ", status="
	        + status + ", contributorId=" + contributorId + ", validatorId="
	        + validatorId + ", beginDate=" + beginDate +
	        ", endDate=" + endDate + '}';
  }

}
