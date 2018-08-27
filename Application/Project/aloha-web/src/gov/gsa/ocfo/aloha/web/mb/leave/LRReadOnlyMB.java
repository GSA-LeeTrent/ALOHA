package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.mb.UserMB;

import java.io.Serializable;

import javax.faces.bean.ManagedProperty;

public class LRReadOnlyMB implements Serializable{
	private static final long serialVersionUID = -4356467334636904216L;

	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;

	protected LRMode lrMode;
	
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	public String getMode() {
		return this.lrMode.getText();
	}
	public boolean isInChangeSupervisorMode() {
		return ( (this.isInSubmitOwnMode()) || (this.isInOnBehalfOfMode()) );
	}	
	public boolean isInCancelMode() {
		return ( (this.isInSubmitOwnMode()) || (this.isInOnBehalfOfMode()) );
	}		
	public boolean isInAmendMode() {
		return ( (this.isInSubmitOwnMode()) || (this.isInOnBehalfOfMode()) );
	}	
	public boolean isInWithdrawMode() {
		return ( (this.isInSubmitOwnMode()) || (this.isInOnBehalfOfMode()) );
	}	
	public boolean isInApproverMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.APPROVER)) && (this.userMB.isApprover()) );
	}	
	public boolean isInSubmitOwnMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.SUBMIT_OWN)) && (this.userMB.isSubmitOwn()) );
	}
	public boolean isInOnBehalfOfMode() {
		return ( (this.lrMode != null) && (this.lrMode.equals(LRMode.ON_BEHALF_OF)) && (this.userMB.isOnBehalfOf()) );
	}		
}
