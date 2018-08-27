package gov.gsa.ocfo.aloha.ejb.leave;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.LeaveRequestVariance;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

@Local
public interface LRVarianceReportEJB {
	public List<LeaveRequestVariance> runReport(AlohaUser alohaUser,
			String[] facilityIds, Date fromDate, Date toDate)
			throws AlohaServerException;
}