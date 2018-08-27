package gov.gsa.ocfo.aloha.ejb.leave.recon;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;

import javax.ejb.Local;

@Local
public interface LeaveReconWizardPendingEJB {
	
	public void prunePendingWizardItems(LeaveDetail leaveDetail) throws AlohaServerException;
}
