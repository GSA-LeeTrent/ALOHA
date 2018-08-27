package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveSupervisorChangeHistory;
import gov.gsa.ocfo.aloha.model.leave.LeaveHeaderRow;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveRequestEJB {
	public void saveLeaveRequest(LeaveHeader leaveHeader)
			throws AlohaServerException;

	public LeaveHeader updateLeaveHeader(LeaveHeader leaveHeader)
			throws AlohaServerException;

	public LeaveDetail updateLeaveDetail(LeaveDetail leaveDetail)
			throws AlohaServerException;

	public LeaveDetail updateLeaveDetail(LeaveDetail leaveDetail,
			LeaveSupervisorChangeHistory supervisorChangeHistory,
			AlohaUser oldSupvervisor) throws AlohaServerException;

	public LeaveHeader findById(long id) throws AlohaServerException;

	public LeaveDetail getLeaveDetail(long leaveDetailId)
			throws AlohaServerException;

	public List<LeaveHeaderRow> getSubmitOwnList(AlohaUser alohaUser)
			throws AlohaServerException;

	public List<LeaveHeaderRow> getOnBehalfOfList(AlohaUser alohaUser)
			throws AlohaServerException;

	public List<LeaveHeaderRow> getApproverList(AlohaUser alohaUser)
			throws AlohaServerException;

	public List<LeaveItem> getPriorLeaveItemsForCreate(long employeeUserId,
			Date ppStartDate) throws AlohaServerException;

	public List<LeaveItem> getPriorLeaveItemsForAmend(long employeeUserId,
			Date ppStartDate, long leaveDetailIdToExclude)
			throws AlohaServerException;
}