package gov.gsa.ocfo.aloha.ejb.overtime;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.OvertimeRequestReport;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;

import java.util.List;

import javax.ejb.Local;

@Local
public interface OvertimeRequestReportEJB {
	
	/*********************************************************************************************************************
	 * NOT BEING USED
	 * *******************************************************************************************************************
	public List<OvertimeRequestReport> runRpt(int teamId, PayPeriod fromPP, PayPeriod toPP ) throws AlohaServerException;
	*********************************************************************************************************************/
	public List<OvertimeRequestReport> runRpt(AlohaUser alohaUser, String[] empIds, String[] overtimeStatus, String[] overtimeTypes, 
			String[] otPlanGrade, OTPayPeriod fromPP, OTPayPeriod toPP) throws AlohaServerException;

}