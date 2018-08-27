package gov.gsa.ocfo.aloha.ejb.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import javax.ejb.Local;

@Local
public interface LeaveReconAmendEJB {

	public void processLeaveRequestAmendment(AlohaUser employeeSubmitter,
			LeaveReconWizard leaveReconWizard) throws LeaveReconException;
}
