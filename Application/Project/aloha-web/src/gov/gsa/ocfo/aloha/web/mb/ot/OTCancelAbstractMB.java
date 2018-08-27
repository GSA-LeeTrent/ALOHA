package gov.gsa.ocfo.aloha.web.mb.ot;

import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;

public abstract class OTCancelAbstractMB extends OTStatusChangeAbstractMB {
	private static final long serialVersionUID = 6015612673102922226L;

	// INSTANCE MEMBER
	private String cancellationRemarks;

	// INIT
	protected void init() {
		super.init();
	}
	// METHOD REQUIRED BY ABSTRACT SUPERCLASS
	protected String getNewStatusCode() {
		return OTIndivStatus.CodeValues.CANCELLED;
	}
	// EVENT
	public String onCancel() {
		return this.onStatusChange();
	}
	// GETTER
	public String getCancellationRemarks() {
		return cancellationRemarks;
	}
	// SETTER
	public void setCancellationRemarks(String cancellationRemarks) {
		this.cancellationRemarks = cancellationRemarks;
	}		
	
	
}