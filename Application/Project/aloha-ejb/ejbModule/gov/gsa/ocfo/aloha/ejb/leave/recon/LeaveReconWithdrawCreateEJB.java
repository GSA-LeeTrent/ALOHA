package gov.gsa.ocfo.aloha.ejb.leave.recon;

import java.util.List;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import javax.ejb.Local;

@Local
public interface LeaveReconWithdrawCreateEJB {

	public void processWithdrawCreateLeaveRequests(AlohaUser employeeSubmitter,
			LeaveReconWizard leaveReconWizard) throws LeaveReconException;

	public List<LeaveDetail> createLeaveWithdrawals(
			AlohaUser employeeSubmitter, LeaveReconWizard leaveReconWizard)
			throws LeaveReconException;
}
