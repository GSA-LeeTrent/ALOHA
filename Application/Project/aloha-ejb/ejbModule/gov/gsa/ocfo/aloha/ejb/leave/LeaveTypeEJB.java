package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveTypeEJB {
	public LeaveType getLeaveType(long leaveTypeId) throws AlohaServerException;
	public List<LeaveType> getLeaveTypes(String primaryCode) throws AlohaServerException;
	public LeaveType getLeaveType(String primaryCode, String secondaryCode) throws AlohaServerException;
	public List<LeaveType> getAllLeaveTypes() throws AlohaServerException;
	//2012-10-02 JJM  48969: New for Eff date of leave type
	public List<LeaveType> getAllLeaveTypesEff(Date ppEffDate) throws AlohaServerException;
	public List<LeaveType> getAllLeaveTypesEffOBO(Date ppEffDate) throws AlohaServerException;
}
