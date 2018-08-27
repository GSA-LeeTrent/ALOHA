package gov.gsa.ocfo.aloha.ejb;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.PayPeriod;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

@Local
public interface PayPeriodEJB {
	public List<Integer> getPayPeriodYears() throws AlohaServerException;
	public Map<Integer, List<PayPeriod>> getAllPayPeriods() throws AlohaServerException;
	public Map<Integer, List<PayPeriod>> getLimitedPayPeriods() throws AlohaServerException;
	public List<PayPeriod> getPayPeriodsForYear(int year) throws AlohaServerException;
}
