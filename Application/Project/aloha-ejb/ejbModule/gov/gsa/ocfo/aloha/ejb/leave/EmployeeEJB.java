package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.PayPeriodSchedule;
import gov.gsa.ocfo.aloha.model.ScheduleItem;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.leave.DisabledVetLeaveInfo;

import java.util.List;

import javax.ejb.Local;

@Local
public interface EmployeeEJB {
	public List<AlohaUser> getLeaveApprovers(long employeeId) throws AlohaServerException;
	public List<AlohaUser> getPrimTimekeeper(long employeeId) throws AlohaServerException;
	public List<ScheduleItem> getPayPeriodSchedule(long employeeId, String payPeriodStartDate) throws AlohaServerException;
	public List<KeyValuePair> getLeaveBalances(long employeeId) throws AlohaServerException;
	public List<AlohaUser> getOnBehalfOfEmployees(long employeeId) throws AlohaServerException;
	public List<AlohaUser> getManagedStaffForUser(long employeeId) throws AlohaServerException;
	public boolean getAwsFlagForEmployee(long employeeUserId) throws AlohaServerException;
	public List<PayPeriodSchedule> retrievePayPeriodScheduleForEmployeeAndPayPeriodStartDate
	(long employeeId, String payPeriodStartDate) throws AlohaServerException;
	
	public DisabledVetLeaveInfo getDisabledVetLeaveInfo(long employeeUserId) throws AlohaServerException;
}
