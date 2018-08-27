package gov.gsa.ocfo.aloha.model.entity.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;

import java.io.Serializable;

public class LeaveReconWizardOutcome implements Serializable {

	private static final long serialVersionUID = -2759272636210162085L;

	public enum LeaveReconTransactionType {
		ALOHA, ETAMS, BOTH
	}
	
	private LeaveReconTransactionType leaveReconTransactionType;
	private String etamsMessage;
	
	public LeaveReconWizardOutcome(LeaveReconWizard leaveReconWizard) throws LeaveReconException {
		
		boolean alohaTransactionRequired = leaveReconWizard.incorrectAlohaLeaveHoursDetected();
		boolean etamsTransactionRequired = leaveReconWizard.incorrectEtamsLeaveHoursDetected();
				
		if ( alohaTransactionRequired == true && etamsTransactionRequired == true ) {
			this.leaveReconTransactionType = LeaveReconTransactionType.BOTH;
		} else if ( alohaTransactionRequired == true && etamsTransactionRequired == false ) {
			this.leaveReconTransactionType = LeaveReconTransactionType.ALOHA;
		} else if ( alohaTransactionRequired == false && etamsTransactionRequired == true ) {
			this.leaveReconTransactionType = LeaveReconTransactionType.ETAMS;
		} else {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("LeaveReconWizard is in an Invalid State: 1) ALOHA Transaction Required: ");
			errMsg.append(alohaTransactionRequired);
			errMsg.append(", ETAMS Transaction Required: ");
			errMsg.append(etamsTransactionRequired);
			errMsg.append("] \nCANNOT CONINTUE");
			throw new LeaveReconException(errMsg.toString());
		}
	}
	
	public LeaveReconTransactionType getLeaveReconTransactionType() {
		return leaveReconTransactionType;
	}
	public void setLeaveReconTransactionType(
			LeaveReconTransactionType leaveReconTransactionType) {
		this.leaveReconTransactionType = leaveReconTransactionType;
	}

	public String getEtamsMessage() {
		return etamsMessage;
	}

	public void setEtamsMessage(String etamsMessage) {
		this.etamsMessage = etamsMessage;
	}
	
	public boolean isEtamsTransaction() {
		return	(	
					( this.leaveReconTransactionType != null )
						&&	(this.leaveReconTransactionType == LeaveReconTransactionType.ETAMS 
								|| this.leaveReconTransactionType == LeaveReconTransactionType.BOTH) 
				);		
	}

	public boolean isAlohaTransaction() {
		return	(
					(this.leaveReconTransactionType != null)
						&& (this.leaveReconTransactionType == LeaveReconTransactionType.ALOHA 
								|| this.leaveReconTransactionType == LeaveReconTransactionType.BOTH) 
				);
	}	
}