package gov.gsa.ocfo.aloha.ejb.leave.recon;

import gov.gsa.ocfo.aloha.exception.LeaveReconException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.model.entity.leave.recon.LeaveReconWizard;

import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveReconRetrieveEJB {

	public List<LeaveReconWizard> retrieveLeaveReconWizard(long employeeUserId) throws LeaveReconException;
	public LeaveDetail retrieveLeaveDetail(long leaveDetailId) throws LeaveReconException;
	public LeaveStatus retrieveLeaveStatus(String leaveStatusCode) throws LeaveReconException;
	public LeaveStatusTransition retrieveLeaveStatusTransition(String leaveStatusTransitionCode) throws LeaveReconException;
	public LeaveType retrieveLeaveType(long leaveTypeId) throws LeaveReconException;
}
