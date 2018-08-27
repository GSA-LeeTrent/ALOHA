package gov.gsa.ocfo.aloha.model.entity.leave;

import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;
import javax.persistence.*;
//2012-10-03 JJM Added for startDate and endDate
import java.util.Date;



@Entity
@Table(name="LR_TYPE", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="findByPrimaryLeaveTypeCode",
	        	query="SELECT row FROM LeaveType row WHERE row.primaryCode = :primaryLeaveTypeCode"),
   	@NamedQuery(name="findByPrimaryAndSecondaryLeaveTypeCode",
   	        	query="SELECT row FROM LeaveType row WHERE row.primaryCode = :primaryLeaveTypeCode and row.secondaryCode = :secondaryLeaveTypeCode"),
//SAK 2012-10-18 Changed 0 to 999
   	@NamedQuery(name="retrieveAllLeaveTypes", 
   			query="SELECT row FROM LeaveType row where row.displayOrder <> 999 ORDER BY row.displayOrder ASC"),
   	//2012-10-02 JJM: Add new retrieveAllLeaveTypesEff
	@NamedQuery(name="retrieveAllLeaveTypesEff", 
			query="SELECT row FROM LeaveType row WHERE row.startDate <= :ppEffDate AND row.endDate >= :ppEffDate AND row.displayOrder <> 999 AND row.isRestricted <> 'Y' ORDER BY row.displayOrder ASC"),   			
	@NamedQuery(name="retrieveAllLeaveTypesEffOBO", 
			query="SELECT row FROM LeaveType row WHERE row.startDate <= :ppEffDate AND row.endDate >= :ppEffDate AND row.displayOrder <> 999 ORDER BY row.displayOrder ASC")   			

})
public class LeaveType implements Serializable {
	private static final long serialVersionUID = 6921702035428020186L;

	public interface QueryNames{
		public static final String FIND_BY_PRIMARY_CODE = "findByPrimaryLeaveTypeCode";
		public static final String FIND_BY_PRIMARY_AND_SECONDARY_CODE = "findByPrimaryAndSecondaryLeaveTypeCode";
		public static final String RETRIEVE_ALL = "retrieveAllLeaveTypes";
		//2012-10-02 JJM 48969: Add RETRIEVE_ALL_EFF and RETRIEVE_ALL_EFF_OBO
		public static final String RETRIEVE_ALL_EFF = "retrieveAllLeaveTypesEff";
		public static final String RETRIEVE_ALL_EFF_OBO = "retrieveAllLeaveTypesEffOBO";
	}
	public interface QueryParamNames {
		public static final String FIND_BY_PRIMARY_CODE = "primaryLeaveTypeCode";	
		public static final String FIND_BY_SECONDARY_CODE = "secondaryLeaveTypeCode";
		//2012-10-02 JJM: Add parm for ppEffDate
		public static final String RETRIEVE_ALL_EFF = "ppEffDate";
		public static final String RETRIEVE_ALL_EFF_OBO = "ppEffDate";
	}
	public interface HardCoding {
		public static final String DISABLED_VET_PRIMARY_CODE = "69";
	}

	@Id
	@Column(name="LR_TYPE_ID", unique=true, nullable=false, precision=10)
	private long id;

	@Column(name="PRIMARY_CODE", unique=true, nullable=false, length=2)
	private String primaryCode;

	@Column(name="PRIMARY_CODE_DESC", nullable=false, length=100)
	private String primaryCodeDesc;

	@Column(name="SECONDARY_CODE", unique=true, length=2)
	private String secondaryCode;

	@Column(name="SECONDARY_CODE_DESC", length=100)
	private String secondaryCodeDesc;

	@Column(name="DISPLAY_ORDER")
	private int displayOrder;

	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="START_DATE")
	private Date startDate;

	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="END_DATE")
	private Date endDate;

	//2012-10-02 JJM 48969: Added for compare to is_restricted
	@Column(name="IS_RESTRICTED")
	private String isRestricted;

	@Transient
	private String label;

	@Transient
	private String abbreviatedLabel;
	
	public boolean hasSecondaryCode() {
		return (!StringUtil.isNullOrEmpty(this.secondaryCode));
	}
    public LeaveType() {
    }

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPrimaryCode() {
		return this.primaryCode;
	}

	public void setPrimaryCode(String primaryCode) {
		this.primaryCode = primaryCode;
	}

	public String getPrimaryCodeDesc() {
		return this.primaryCodeDesc;
	}

	public void setPrimaryCodeDesc(String primaryCodeDesc) {
		this.primaryCodeDesc = primaryCodeDesc;
	}

	public String getSecondaryCode() {
		return this.secondaryCode;
	}

	public void setSecondaryCode(String secondaryCode) {
		this.secondaryCode = secondaryCode;
	}

	public String getSecondaryCodeDesc() {
		return this.secondaryCodeDesc;
	}

	public void setSecondaryCodeDesc(String secondaryCodeDesc) {
		this.secondaryCodeDesc = secondaryCodeDesc;
	}
	
	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getLabel() {
		if ( StringUtil.isNullOrEmpty(this.label) ) {
			StringBuffer sb = new StringBuffer();
			if ( StringUtil.isNullOrEmpty(this.secondaryCode) ) {
				sb.append(this.primaryCodeDesc); 
				sb.append(" (");
				sb.append(this.primaryCode);
				sb.append(")");				
			} else {
				sb.append(this.primaryCodeDesc); 
				sb.append(" / ");
				sb.append(this.secondaryCodeDesc);
				sb.append(" (");
				sb.append(this.primaryCode);
				sb.append("/");
				sb.append(this.secondaryCode);
				sb.append(")");				
			}
			this.label = sb.toString();
		}
		return this.label;
	}		

	public String getAbbreviatedLabel() {
		if ( StringUtil.isNullOrEmpty(this.abbreviatedLabel) ) {
			StringBuffer sb = new StringBuffer();
			if ( StringUtil.isNullOrEmpty(this.secondaryCode) ) {
				sb.append(this.primaryCode);
				sb.append(" - ");
				sb.append(this.primaryCodeDesc); 
			} else {
				sb.append(this.primaryCode);
				sb.append(" / ");
				sb.append(this.secondaryCode);
			}
			this.abbreviatedLabel = sb.toString();
		}
		return this.abbreviatedLabel;
	}	

	public String getValue() {
		if ( StringUtil.isNullOrEmpty(this.secondaryCode)) {
			return this.primaryCode;
		} else {
			return (this.primaryCode + this.secondaryCode);
		}
	}		

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((primaryCode == null) ? 0 : primaryCode.hashCode());
		result = prime * result
				+ ((secondaryCode == null) ? 0 : secondaryCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LeaveType other = (LeaveType) obj;
		if (id != other.id)
			return false;
		if (primaryCode == null) {
			if (other.primaryCode != null)
				return false;
		} else if (!primaryCode.equals(other.primaryCode))
			return false;
		if (secondaryCode == null) {
			if (other.secondaryCode != null)
				return false;
		} else if (!secondaryCode.equals(other.secondaryCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LeaveType [id=");
		builder.append(id);
		builder.append(", primaryCode=");
		builder.append(primaryCode);
		builder.append(", primaryCodeDesc=");
		builder.append(primaryCodeDesc);
		builder.append(", secondaryCode=");
		builder.append(secondaryCode);
		builder.append(", secondaryCodeDesc=");
		builder.append(secondaryCodeDesc);
		builder.append(", displayOrder=");
		builder.append(displayOrder);
		builder.append("]");
		return builder.toString();
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setIsRestricted(String isRestricted) {
		this.isRestricted = isRestricted;
	}
	public String getIsRestricted() {
		return isRestricted;
	}
}