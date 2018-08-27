package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;

import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveStatusEJB {
	public LeaveStatus getLeaveStatus(String code) throws AlohaServerException;
	public List<LeaveStatus> getAllLeaveStatuses() throws AlohaServerException;;
	public LeaveStatusTransition getLeaveStatusTransition(String actionCode) throws AlohaServerException;;
	public List<LeaveStatusTransition> getAllLeaveStatusTransitions() throws AlohaServerException;;
	

}
