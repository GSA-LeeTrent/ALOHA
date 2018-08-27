package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.LeaveRequestReconciliation;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;

import java.util.List;

import javax.ejb.Local;

@Local
public interface LeaveRequestReconEJB {
	public List<LeaveRequestReconciliation> runRpt(AlohaUser alohaUser,
			String[] priLeaveTypes, String[] secLeaveTypes, String[] empIds,
			PayPeriod fromPP, PayPeriod toPP) throws AlohaServerException;
}
