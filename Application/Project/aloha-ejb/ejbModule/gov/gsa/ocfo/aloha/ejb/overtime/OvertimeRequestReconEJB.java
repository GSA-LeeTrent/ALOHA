package gov.gsa.ocfo.aloha.ejb.overtime;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.OvertimeRequestReconciliation;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;

import java.util.List;

import javax.ejb.Local;

@Local
public interface OvertimeRequestReconEJB {
	public List<OvertimeRequestReconciliation> runRpt(AlohaUser alohaUser, String[] overtimeTypes, String[] otPlanGrade,
			String[] empIds, OTPayPeriod fromPP, OTPayPeriod toPP) throws AlohaServerException;
}
