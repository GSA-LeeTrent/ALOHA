package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;

public abstract class OTDenyAbstractMB extends OTStatusChangeAbstractMB {
	private static final long serialVersionUID = -9155878251599980360L;

	// INSTANCE MEMBER
	private String denialRemarks;

	// INIT
	protected void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTIndivStatus.CodeValues.DENIED;
	}
	// EVENT
	public String onDeny() {
		return this.onStatusChange();
	}
	// GETTER
	public String getDenialRemarks() {
		return denialRemarks;
	}
	// SETTER
	public void setDenialRemarks(String denialRemarks) {
		this.denialRemarks = denialRemarks;
	}	
}