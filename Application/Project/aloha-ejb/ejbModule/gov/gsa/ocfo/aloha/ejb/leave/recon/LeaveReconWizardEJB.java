package gov.gsa.ocfo.aloha.ejb.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizardOutcome;

import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveReconWizardEJB {
	public List<LeaveReconWizard> retrieveByEmployeeUserId(long employeeUserId) throws LeaveReconException;
	public LeaveReconWizardOutcome doReconciliation(AlohaUser alohaUser, LeaveReconWizard leaveReconWizard) throws LeaveReconException;
}
