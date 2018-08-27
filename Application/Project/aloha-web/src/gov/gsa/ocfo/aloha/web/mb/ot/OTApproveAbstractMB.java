package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;

public abstract class OTApproveAbstractMB extends OTStatusChangeAbstractMB {
	private static final long serialVersionUID = 676287683019907618L;

	// INSTANCE MEMBER
	private String approvalRemarks;

	// INIT
	protected void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTIndivStatus.CodeValues.APPROVED;
	}
	// EVENT
	public String onApprove() {
		return this.onStatusChange();
	}
	// GETTER
	public String getApprovalRemarks() {
		return approvalRemarks;
	}
	// SETTER
	public void setApprovalRemarks(String approvalRemarks) {
		this.approvalRemarks = approvalRemarks;
	}		
}