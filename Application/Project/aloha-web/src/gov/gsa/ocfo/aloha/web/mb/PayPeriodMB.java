package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.ejb.PayPeriodEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.DateUtil;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

@ManagedBean(name="payPeriodMB", eager=false)
@SessionScoped
public class PayPeriodMB implements Serializable {
	private static final long serialVersionUID = -3606851056772525733L;
	@EJB
	private PayPeriodEJB payPeriodEJB;
	
	private SelectItem[] payPeriods = new SelectItem[0];
	private SelectItem[] limitedPayPeriods = new SelectItem[0];
	private SelectItem[] currentAndPastPayPeriods = new SelectItem[0];
	
	private Map<Integer, List<PayPeriod>> payPeriodMap = new TreeMap<Integer, List<PayPeriod>>();
	private Map<Integer, List<PayPeriod>> limitedPayPeriodMap = new TreeMap<Integer, List<PayPeriod>>();
	
	private PayPeriod latestClosedPayPeriod;

	@PostConstruct
	public void refresh() {
		try {
			this.buildSelectItems();
			this.buildLimitedSelectItems();
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			this.clearState();
		} catch (Exception e) {
			e.printStackTrace();
			this.clearState();
		}
	}		
	private void clearState() {
		synchronized(this.payPeriodMap) {
			this.payPeriodMap.clear();
		}
		synchronized(this.payPeriods) {
			this.payPeriods = new SelectItem[0];
		}	
		synchronized(this.limitedPayPeriodMap) {
			this.limitedPayPeriodMap.clear();
		}
		synchronized(this.limitedPayPeriods) {
			this.limitedPayPeriods = new SelectItem[0];
		}		
	}	
	private void buildSelectItems() throws AlohaServerException {
		synchronized(this.payPeriodMap) {
			this.payPeriodMap.clear();
			this.payPeriodMap = this.payPeriodEJB.getAllPayPeriods();
		}
		
		GregorianCalendar today = new GregorianCalendar();
		today.setTime(new Date());
		int currentYear = today.get(Calendar.YEAR);
		int sevenYearsAgo = currentYear - 7;
		
		List<Integer> keyList = new ArrayList<Integer>();
		for ( Integer key : this.payPeriodMap.keySet()) {
			if ( key.intValue() > sevenYearsAgo) {
				keyList.add(key);				
			}
		}
		Collections.reverse(keyList);
		
		synchronized(this.payPeriods) {
			this.payPeriods = null;
			this.payPeriods = new SelectItem[keyList.size()];
			int index = 0;
			for ( Integer year : keyList ) {
				List<PayPeriod> payPeriods = this.payPeriodMap.get(year);
				SelectItem[] selectItemsForYear = this.buildSelectItem(year, payPeriods);
				SelectItemGroup selectItemGroupForYear = new SelectItemGroup(year.toString() + ":", year.toString() 
						+ " Pay Periods", false, selectItemsForYear);
				this.payPeriods[index++] = selectItemGroupForYear;
			}
		}
	}	
	
	private void buildLimitedSelectItems() throws AlohaServerException {
		synchronized(this.limitedPayPeriodMap) {
			this.limitedPayPeriodMap.clear();
			this.limitedPayPeriodMap = this.payPeriodEJB.getLimitedPayPeriods();
		}
		
		GregorianCalendar today = new GregorianCalendar();
		today.setTime(new Date());
		int currentYear = today.get(Calendar.YEAR);
		int sevenYearsAgo = currentYear - 7;
		
		List<Integer> keyList = new ArrayList<Integer>();
		for ( Integer key : this.limitedPayPeriodMap.keySet()) {
			if ( key.intValue() > sevenYearsAgo) {
				keyList.add(key);				
			}
		}
		Collections.reverse(keyList);
		
		synchronized(this.limitedPayPeriods) {
			this.limitedPayPeriods = null;
			this.limitedPayPeriods = new SelectItem[keyList.size()];
			int index = 0;
			for ( Integer year : keyList ) {
				List<PayPeriod> limitedPPs = this.limitedPayPeriodMap.get(year);
				SelectItem[] selectItemsForYear = this.buildSelectItem(year, limitedPPs);
				SelectItemGroup selectItemGroupForYear = new SelectItemGroup(year.toString() + ":", year.toString() 
						+ " Pay Periods", false, selectItemsForYear);
				this.limitedPayPeriods[index++] = selectItemGroupForYear;
			}
		}
	}	

	private synchronized SelectItem[] buildSelectItem(Integer year, List<PayPeriod> payPeriods) {
		SelectItem[] selectItems = new SelectItem[payPeriods.size()];
		int index = 0;
		for ( PayPeriod payPeriod : payPeriods) {
			selectItems[index++] = new SelectItem(payPeriod.getValue(), payPeriod.getLabel());
		}
		return selectItems;
	}	
	
	public PayPeriod getPayPeriodForStartDate(String val) throws ParseException {
		Date startDate = DateUtil.convertStringToDate(val, DateUtil.DateFormats.YYYYMMDD);
		for (Map.Entry<Integer, List<PayPeriod>> entry : this.payPeriodMap.entrySet()) {
			List<PayPeriod> payPeriodList = entry.getValue();
			for (PayPeriod payPeriod : payPeriodList) {
				if (payPeriod.getFromDate().equals(startDate)) {
					return payPeriod;
				}
			}
		}
		return null;
	}
	
	public PayPeriod getPayPeriodForStartDate(Date startDate) {
		for (Map.Entry<Integer, List<PayPeriod>> entry : this.payPeriodMap.entrySet()) {
			List<PayPeriod> payPeriodList = entry.getValue();
			for (PayPeriod payPeriod : payPeriodList) {
				if (payPeriod.getFromDate().equals(startDate)) {
					return payPeriod;
				}
			}
		}
		return null;
	}	
	public SelectItem[] getPayPeriods() {
		if ( (this.payPeriods == null) || (this.payPeriods.length == 0) ) {
			try {
				this.buildSelectItems();
			} catch (AlohaServerException e) {
				e.printStackTrace();
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
				} catch( IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return this.payPeriods;
	}	
	
	public SelectItem[] getLimitedPayPeriods() {
		if ( (this.limitedPayPeriods == null) || (this.limitedPayPeriods.length == 0) ) {
			try {
				this.buildLimitedSelectItems();
			} catch (AlohaServerException e) {
				e.printStackTrace();
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
				} catch( IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return this.limitedPayPeriods;
	}	
	
	
	private PayPeriod findLatestClosedPayPeriod() {
		
		GregorianCalendar datePortionOnlyCal = new GregorianCalendar();
		datePortionOnlyCal.setTime(new Date());
		datePortionOnlyCal.set(Calendar.HOUR_OF_DAY, 0);
		datePortionOnlyCal.set(Calendar.MINUTE, 0);					
		datePortionOnlyCal.set(Calendar.SECOND, 0);
		datePortionOnlyCal.set(Calendar.MILLISECOND, 0);
		Date today = datePortionOnlyCal.getTime();
		
		int currentYear = datePortionOnlyCal.get(Calendar.YEAR);
		//System.out.println("currentYear: " + currentYear);
		
		List<PayPeriod>payPeriodsToEvaluate = new ArrayList<PayPeriod>();
		payPeriodsToEvaluate.addAll(this.findPayPeriodsinYear(currentYear));
		payPeriodsToEvaluate.addAll(this.findPayPeriodsinYear(currentYear - 1));
		
		Collections.sort(payPeriodsToEvaluate, new Comparator<PayPeriod>() {
			@Override
			public int compare(PayPeriod payPeriodOne, PayPeriod payPeriodTwo) {
				
				//System.out.println(payPeriodOne.getYear() + " : " + payPeriodTwo.getYear());
				
				if ( payPeriodOne.getYear() > payPeriodTwo.getYear()) {
					return 1;
				} else if  ( payPeriodOne.getYear() < payPeriodTwo.getYear()) {
					return -1;
				} else {
					
					//System.out.println(payPeriodOne.getPayPeriod() + " : " + payPeriodTwo.getPayPeriod());
					
					if ( payPeriodOne.getPayPeriod() > payPeriodTwo.getPayPeriod() ) {
						return 1;
					} else if ( payPeriodOne.getPayPeriod() < payPeriodTwo.getPayPeriod() ) {
						return -1;
					} else {
						return 0;
					}
				}
			}	
		});	
		
		StringBuilder sb = new StringBuilder();
		for (PayPeriod payPeriod : payPeriodsToEvaluate ) {
			sb.append("\n[" + payPeriod.getPayPeriod() + " : " + payPeriod.getYear() + "]");
		}
		//System.out.println(sb.toString());
		
		int count = payPeriodsToEvaluate.size();
		
		for ( int ii = (count -1); ii >= 0; ii--) {
			PayPeriod payPeriod = payPeriodsToEvaluate.get(ii);
			if ( today.after(payPeriod.getToDate())) {
				return payPeriod;
			}
		}
		return null;
	}

	private List<PayPeriod>findPayPeriodsinYear(int year) {
		for (Map.Entry<Integer, List<PayPeriod>> entry : this.payPeriodMap.entrySet()) {
			if ( entry.getKey() == year) {
				return (entry.getValue());
			}
		}
		return null;
	}
	
	public PayPeriod findCurrentPayPeriod() {
		GregorianCalendar datePortionOnlyCal = new GregorianCalendar();
		datePortionOnlyCal.setTime(new Date());
		datePortionOnlyCal.set(Calendar.HOUR_OF_DAY, 0);
		datePortionOnlyCal.set(Calendar.MINUTE, 0);					
		datePortionOnlyCal.set(Calendar.SECOND, 0);
		datePortionOnlyCal.set(Calendar.MILLISECOND, 0);
		Date today = datePortionOnlyCal.getTime();
		for (Map.Entry<Integer, List<PayPeriod>> entry : this.payPeriodMap.entrySet()) {
			List<PayPeriod> payPeriodList = entry.getValue();
			for (PayPeriod payPeriod : payPeriodList) {
				if ( ( today.equals(payPeriod.getFromDate()) )
						|| ( today.equals(payPeriod.getToDate()) )
						|| ( today.after(payPeriod.getFromDate()) && today.before(payPeriod.getToDate()))
						
				) {
					return payPeriod;
				}
			}
		}
		return null;
	}	
	
	public PayPeriod getLatestClosedPayPeriod() {
		
		if ( this.latestClosedPayPeriod == null ) {
			this.latestClosedPayPeriod = this.findLatestClosedPayPeriod();
		}
		return this.latestClosedPayPeriod;
	}

	public SelectItem[] getCurrentAndPastPayPeriods() {
		if ( (this.currentAndPastPayPeriods == null) || (this.currentAndPastPayPeriods.length == 0) ) {
			try {
				this.buildSelectItemsForCurrentAndPastPayPeriods();
			} catch (AlohaServerException e) {
				e.printStackTrace();
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
				} catch( IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return this.currentAndPastPayPeriods;
	}

	private void buildSelectItemsForCurrentAndPastPayPeriods() throws AlohaServerException {
		GregorianCalendar today = new GregorianCalendar();
		today.setTime(new Date());
		int currentYear = today.get(Calendar.YEAR);
		int sevenYearsAgo = currentYear - 7;
		
		List<Integer> keyList = new ArrayList<Integer>();
		for ( Integer key : this.payPeriodMap.keySet()) {
			if ( key.intValue() > sevenYearsAgo
					&& key.intValue() <= currentYear) {
				keyList.add(key);				
			}
		}
		Collections.reverse(keyList);
		
		synchronized(this.currentAndPastPayPeriods) {
			this.currentAndPastPayPeriods = null;
			this.currentAndPastPayPeriods = new SelectItem[keyList.size()];
			int index = 0;
			for ( Integer year : keyList ) {
				List<PayPeriod> payPeriods = this.payPeriodMap.get(year);
				SelectItem[] selectItemsForYear = this.buildSelectItem(year, payPeriods);
				SelectItemGroup selectItemGroupForYear = new SelectItemGroup(year.toString() + ":", year.toString() 
						+ " Pay Periods", false, selectItemsForYear);
				this.currentAndPastPayPeriods[index++] = selectItemGroupForYear;
			}
		}
	}
}