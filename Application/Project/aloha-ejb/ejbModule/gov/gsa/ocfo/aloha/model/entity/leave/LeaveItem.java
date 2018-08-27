package gov.gsa.ocfo.aloha.model.entity.leave;

import gov.gsa.ocfo.aloha.model.entity.AuditTrail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * The persistent class for the LR_ITEM database table.
 * 
 */
@Entity
@Table(name="LR_ITEM", schema="ALOHA")
@NamedQueries({
	@NamedQuery(name="retrieveLRItemByEmployeePayPeriodAndLeaveStatus",
	        	query="SELECT row FROM LeaveItem row " +
	        			"WHERE row.leaveDetail.leaveHeader.employee.userId = :employeeUserID " + 
	        			"AND row.leaveDetail.payPeriodStartDate = :ppStartDate " +
	        			"AND row.leaveDetail.leaveStatus.code IN (:leaveStatusCodeList)"),
	@NamedQuery(name="retrieveLRItemByEmployeePayPeriodAndLeaveStatusExcludeLeaveDetailId",
        		query="SELECT row FROM LeaveItem row " +
        				"WHERE row.leaveDetail.leaveHeader.employee.userId = :employeeUserID " + 
        				"AND row.leaveDetail.payPeriodStartDate = :ppStartDate " +
        				"AND row.leaveDetail.leaveStatus.code IN (:leaveStatusCodeList) " + 
        				"AND row.leaveDetail.id <> :leaveDetailIdToExclude" )	        			
})
public class LeaveItem implements Serializable {
	private static final long serialVersionUID = 1511834598721644276L;
	public static final BigDecimal MIN_LEAVE_HOURS = new BigDecimal(0.1D).setScale(1, RoundingMode.HALF_DOWN);;
	
	public interface QueryNames{
		public static final String RETRIEVE_BY_EMPLOYEE_PAY_PERIOD_LEAVE_STATUS = "retrieveLRItemByEmployeePayPeriodAndLeaveStatus";
		public static final String RETRIEVE_BY_EMPLOYEE_PAY_PERIOD_LEAVE_STATUS_EXCLUDE_LEAVE_DETAIL_ID = "retrieveLRItemByEmployeePayPeriodAndLeaveStatusExcludeLeaveDetailId";		
	}
	public interface QueryParamNames {
		public static final String EMPLOYEE_USER_ID = "employeeUserID";	
		public static final String PAY_PERIOD_START_DATE = "ppStartDate";
		public static final String LEAVE_STATUS_CODE_LIST = "leaveStatusCodeList";
		public static final String LEAVE_DETAIL_ID_TO_EXCLUDE = "leaveDetailIdToExclude";		
	}	
	
	@Id
	@SequenceGenerator(name="LR_ITEM_ID_GENERATOR", sequenceName="ALOHA.SEQ_LR_ITEM", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="LR_ITEM_ID_GENERATOR")
	@Column(name="LR_ITEM_ID", unique=true, nullable=false, precision=10)
	private long id;

    @ManyToOne
	@JoinColumn(name="LR_DETAIL_ID", referencedColumnName="LR_DETAIL_ID", nullable=false)
	private LeaveDetail leaveDetail;

    @ManyToOne
	@JoinColumn(name="LR_TYPE_ID",  referencedColumnName="LR_TYPE_ID", nullable=false)
	private LeaveType leaveType;
	
	@Temporal( TemporalType.TIMESTAMP)
	@Column(name="LEAVE_DATE", nullable=false)
	private Date leaveDate;

	@Column(name="LEAVE_HOURS", nullable=false, precision=2, scale=1)
	private BigDecimal leaveHours;

    @Temporal( TemporalType.TIMESTAMP)
	@Column(name="START_TIME")
	private Date startTime;

    @Temporal( TemporalType.TIMESTAMP)
	@Column(name="STOP_TIME")
	private Date stopTime;
    
	@Embedded
	private AuditTrail auditTrail;
  
	@Version
	@Column(name="OPT_LOCK_NBR")
	private long version;

	@Transient
	private boolean captured;
	
	public LeaveItem() {
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LeaveDetail getLeaveDetail() {
		return leaveDetail;
	}

	public void setLeaveDetail(LeaveDetail leaveDetail) {
		this.leaveDetail = leaveDetail;
	}

	public LeaveType getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
	}

	public Date getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
	}

	public BigDecimal getLeaveHours() {
		return leaveHours;
	}

	public void setLeaveHours(BigDecimal leaveHours) {
		this.leaveHours = leaveHours;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public boolean isStartTimePopulated() {
		return (this.startTime != null);
	}
	
	public Date getStopTime() {
		return stopTime;
	}

	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	public AuditTrail getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(AuditTrail auditTrail) {
		this.auditTrail = auditTrail;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public boolean isCaptured() {
		return captured;
	}

	public void setCaptured(boolean captured) {
		this.captured = captured;
	}
	
	public boolean isDisabledVetLeaveItem() {
		
		if ( this.leaveType != null ) {
			return this.leaveType.getPrimaryCode().equals(LeaveType.HardCoding.DISABLED_VET_PRIMARY_CODE);
		}
		return false;
	}
}	
