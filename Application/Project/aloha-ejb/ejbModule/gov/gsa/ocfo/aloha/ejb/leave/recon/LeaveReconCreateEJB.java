package gov.gsa.ocfo.aloha.ejb.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import javax.ejb.Local;

@Local
public interface LeaveReconCreateEJB {

	public void processNewLeaveRequest(AlohaUser employeeSubmitter,
			LeaveReconWizard leaveReconWizard) throws LeaveReconException;

	public LeaveHeader createNewLeaveRequest(AlohaUser employeeSubmitter,
			LeaveReconWizard leaveReconWizard) throws LeaveReconException;
}
