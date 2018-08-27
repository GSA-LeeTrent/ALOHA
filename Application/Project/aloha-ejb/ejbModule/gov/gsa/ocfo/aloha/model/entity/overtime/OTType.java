package gov.gsa.ocfo.aloha.model.entity.overtime;

import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="OT_TYPE", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveAllOTType", 
				query="SELECT row FROM OTType row WHERE row.displayOrder <> 0 ORDER BY row.displayOrder ASC"),
	//2012-10-11 JJM 48969: Add queries for restrictive codes			
	@NamedQuery(name="retrieveAllOTTypeEmp", 
				query="SELECT row FROM OTType row WHERE row.displayOrder <> 999 AND row.isRestricted <> 'Y' ORDER BY row.displayOrder ASC"),				
	@NamedQuery(name="retrieveAllOTTypeOBO", 
			query="SELECT row FROM OTType row WHERE row.displayOrder <> 999 ORDER BY row.displayOrder ASC")				
})
public class OTType implements Serializable {
	private static final long serialVersionUID = -1214788366377725728L;
	
	public interface QueryNames{
		public static final String RETRIEVE_ALL = "retrieveAllOTType";
		//2012-10-11 JJM 48969: Add queries for restrictive codes for Employee
		public static final String RETRIEVE_ALL_EMP = "retrieveAllOTTypeEmp";
		//2012-10-11 JJM 48969: Add queries for non-restrictive codes for Timekeeper / Supervisor		
		public static final String RETRIEVE_ALL_OBO = "retrieveAllOTTypeOBO";
	}
//	public interface QueryParamNames {
//		//2012-10-02 JJM: Add parm for ppEffDate
//		public static final String RETRIEVE_ALL_EMP = "ppEffDate";
//		public static final String RETRIEVE_ALL_OBO = "ppEffDate";
//	}	
	public interface CodeValues {
		public static final String UNSELECTED = "00";		
	}
	
	@Id
	@Column(name="TYPE_ID", unique=true, nullable=false, precision=10)
	private long id;
	
	@Column(name="TYPE_CODE", unique=true, nullable=false)
	private String code;

	@Column(name="TYPE_NAME", unique=true, nullable=false)
	private String name;

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

	@Column(name="FUNDING_REQUIRED")
	private String fundingRequired;
	
	@Transient
	private String label;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	public Date getStartDate() {
		return startDate;
	}
	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	public Date getEndDate() {
		return endDate;
	}
	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	public String getIsRestricted() {
		return isRestricted;
	}
	//2012-10-02 JJM 48969: Added for compare to selected pp start date
	public void setIsRestricted(String isRestricted) {
		this.isRestricted = isRestricted;
	}

	public String getFundingRequired() {
		return fundingRequired;
	}

	public void setFundingRequired(String fundingRequired) {
		this.fundingRequired = fundingRequired;
	}

	public boolean isFundingRequired() {
		return 	( (this.fundingRequired != null) 
					&& (this.fundingRequired.trim().equals("Y"))
				);
	}
	public String getLabel() {
		if ( StringUtil.isNullOrEmpty(this.label)) {
			this.label = this.code + " - " + this.name;
		}
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		OTType other = (OTType) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OTType [id=");
		builder.append(id);
		builder.append(", code=");
		builder.append(code);
		builder.append(", name=");
		builder.append(name);
		builder.append(", displayOrder=");
		builder.append(displayOrder);
		builder.append("]");
		return builder.toString();
	}
}