package gov.gsa.ocfo.aloha.ejb.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import javax.ejb.Local;

@Local
public interface EtamsAmendmentsEJB {

	public String processEtamsAmendments(LeaveReconWizard leaveReconWizard)
			throws LeaveReconException;

}
