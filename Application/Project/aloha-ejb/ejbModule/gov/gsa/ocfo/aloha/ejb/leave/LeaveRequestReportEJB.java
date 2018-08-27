package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.LeaveRequestReport;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;

import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveRequestReportEJB {

	/*****************************************************************************************************************
	 * NOR USED
	 * *****************************************************************
	 * ********************************************** public
	 * List<LeaveRequestReport> runRpt(int teamId, PayPeriod fromPP, PayPeriod
	 * toPP ) throws AlohaServerException;
	 ******************************************************************************************************************/

	public List<LeaveRequestReport> runRpt(AlohaUser alohaUser,
			String[] empIds, String[] leaveStatus, String[] priLeaveTypes,
			String[] secLeaveTypes, PayPeriod fromPP, PayPeriod toPP)
			throws AlohaServerException;

}
