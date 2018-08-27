package gov.gsa.ocfo.aloha.model;

public class LeaveRequestVariance extends LeaveRequestReconciliation  {

	private static final long serialVersionUID = 298504386204491646L;
	
	private long facilityId;
	private String orgLocCode;

	
	public long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}

	public String getOrgLocCode() {
		return orgLocCode;
	}

	public void setOrgLocCode(String orgLocCode) {
		this.orgLocCode = orgLocCode;
	}
}