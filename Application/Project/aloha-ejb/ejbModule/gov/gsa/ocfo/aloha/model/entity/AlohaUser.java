package gov.gsa.ocfo.aloha.model.entity;

import gov.gsa.ocfo.aloha.model.leave.LRVariancePayPeriod;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="USER_DEMOGRAPHICS", schema="USR_FEDDESK_REQUIRED")
public class AlohaUser implements Serializable {
	public static final String SESSION_KEY = AlohaUser.class.getName();
	public static final String SUBMIT_OWN_ROLE_NAME = "SubmitOwn";
	public static final String ON_BEHALF_OF_ROLE_NAME = "OnBehalfOf";
	public static final String APPROVER_ROLE_NAME = "Approver";
	public static final String FACILITY_COORDINATOR_ROLE_NAME = "FacilityCoordinator";
	public static final String OT_FUNDING_REQUIRED_APPROVER = "OTFundingRequiredApprover";
	
	private static final long serialVersionUID = -1708890015510906566L; 
	
	@Id
	@Column(name="USER_ID", unique=true, nullable=false, precision=10)
	private long userId;

	@Column(name="FIRST_NAME", unique=false, nullable=true, length=30)
	private String firstName;
	
	@Column(name="MIDDLE_NAME", unique=false, nullable=true, length=30)
	private String middleName;
	
	@Column(name="LAST_NAME", unique=false, nullable=true, length=30)
	private String lastName;
	
	@Column(name="NT_USER_NAME", unique=false, nullable=true, length=30)
	private String loginName;
	
	@Column(name="EMAIL_ADDRESS", unique=false, nullable=true, length=80)
	private String emailAddress;
	
	@Transient
	private boolean isSubmitOwn;
	@Transient
	private boolean isApprover;
	@Transient
	private boolean isOnBehalfOf;
	@Transient
	private boolean facilityCoordinator;
	@Transient
	private String fullName;
	@Transient
	private long teamId;
	@Transient
	private boolean isAlohaAdmin;
	@Transient
	private String salaryGradeKey;
	@Transient
	private boolean aws;
	@Transient
	private boolean otPolicyAcknowledged;
	@Transient
	private boolean lrVarianceAcknowledged;	
	@Transient
	private LRVariancePayPeriod lrVariancePayPeriod = null;
	@Transient
	private boolean showLeaveReconWizard;
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;			
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;			
	}
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	public boolean isSubmitOwn() {
		return isSubmitOwn;
	}

	public void setSubmitOwn(boolean isSubmitOwn) {
		this.isSubmitOwn = isSubmitOwn;
	}

	public boolean isApprover() {
		return isApprover;
	}

	public void setApprover(boolean isApprover) {
		this.isApprover = isApprover;
	}

	public boolean isOnBehalfOf() {
		return isOnBehalfOf;
	}

	public void setOnBehalfOf(boolean isOnBehalfOf) {
		this.isOnBehalfOf = isOnBehalfOf;
	}
	
	public boolean isFacilityCoordinator() {
		return facilityCoordinator;
	}

	public void setFacilityCoordinator(boolean facilityCoordinator) {
		this.facilityCoordinator = facilityCoordinator;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFullName() {
		return StringUtil.buildFullName(this.firstName, this.middleName, this.lastName);
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLabel() {
		return (this.firstName + " " + (this.getMiddleName()== null ? "" : this.getMiddleName().substring(0,1).toUpperCase()) + " " + this.lastName); 
	}
	public String getValue() {
		return (String.valueOf(this.userId));
	}
	public long getTeamId() {
		return teamId;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}
	
	public String getSalaryGradeKey() {
		return salaryGradeKey;
	}

	public void setSalaryGradeKey(String salaryGradeKey) {
		this.salaryGradeKey = salaryGradeKey;
	}
	
	public boolean isAws() {
		return aws;
	}

	public void setAws(boolean aws) {
		this.aws = aws;
	}
	public boolean isOtPolicyAcknowledged() {
		return otPolicyAcknowledged;
	}
	public void setOtPolicyAcknowledged(boolean otPolicyAcknowledged) {
		this.otPolicyAcknowledged = otPolicyAcknowledged;
	}
	public boolean isLrVarianceAcknowledged() {
		return lrVarianceAcknowledged;
	}
	public void setLrVarianceAcknowledged(boolean lrVarianceAcknowledged) {
		this.lrVarianceAcknowledged = lrVarianceAcknowledged;
	}

	public LRVariancePayPeriod getLrVariancePayPeriod() {
		return lrVariancePayPeriod;
	}

	public void setLrVariancePayPeriod(LRVariancePayPeriod lrVariancePayPeriod) {
		this.lrVariancePayPeriod = lrVariancePayPeriod;
	}
	
	public boolean isShowLeaveReconWizard() {
		return showLeaveReconWizard;
	}

	public void setShowLeaveReconWizard(boolean showLeaveReconWizard) {
		this.showLeaveReconWizard = showLeaveReconWizard;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userId ^ (userId >>> 32));
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
		AlohaUser other = (AlohaUser) obj;
		if (userId != other.userId)
			return false;
		return true;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlohaUser [userId=");
		builder.append(userId);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", middleName=");
		builder.append(middleName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append(", loginName=");
		builder.append(loginName);
		builder.append(", emailAddress=");
		builder.append(emailAddress);
		builder.append(", isSubmitOwn=");
		builder.append(isSubmitOwn);
		builder.append(", isApprover=");
		builder.append(isApprover);
		builder.append(", isOnBehalfOf=");
		builder.append(isOnBehalfOf);
		builder.append(", fullName=");
		builder.append(fullName);
		builder.append(", teamId=");
		builder.append(teamId);
		builder.append(", isAlohaAdmin=");
		builder.append(isAlohaAdmin);
		builder.append(", salaryGradeKey=");
		builder.append(salaryGradeKey);
		builder.append("]");
		return builder.toString();
	}

	public void setAlohaAdmin(boolean isAlohaAdmin) {
		this.isAlohaAdmin = isAlohaAdmin;
	}

	public boolean isAlohaAdmin() {
		return isAlohaAdmin;
	}
}
