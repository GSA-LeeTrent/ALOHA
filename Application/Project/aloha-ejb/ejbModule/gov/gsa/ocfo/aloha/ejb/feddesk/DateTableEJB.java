package gov.gsa.ocfo.aloha.ejb.feddesk;

import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.feddesk.DateTable;

import java.util.Date;

import javax.ejb.Local;

@Local
public interface DateTableEJB {
	public DateTable retrieveDateTableForToday() throws AlohaServerException;
	public DateTable retrieveDateTableForYearPayPeriodDay(int year, int payPeriod, int day) throws AlohaServerException;
	public Integer   retrieveMaxPayPeriodInDateTableForYear(int year) throws AlohaServerException;
	public DateTable retrieveDateTableForDate(Date anyDate) throws AlohaServerException;
}
