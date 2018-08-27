package gov.gsa.ocfo.aloha.web.mb.overtime;

import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

@ManagedBean(name="otPayPeriodMB", eager=false)
@SessionScoped
public class OTPayPeriodMB implements Serializable {
	private static final long serialVersionUID = 3501781136419262868L;

	@EJB
	private OvertimeEJB overtimeEJB;
	
	private SelectItem[] otPayPeriods = new SelectItem[0];
	private Map<Integer, List<OTPayPeriod>> otPayPeriodMap = new TreeMap<Integer, List<OTPayPeriod>>();
	
	private SelectItem[] otLimitedPayPeriods = new SelectItem[0];
	private Map<Integer, List<OTPayPeriod>> otLimitedPayPeriodMap = new TreeMap<Integer, List<OTPayPeriod>>();	
	
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
		synchronized(this.otPayPeriodMap) {
			this.otPayPeriodMap.clear();			
		}
		synchronized(this.otPayPeriods) {
			this.otPayPeriods = new SelectItem[0];			
		}
		synchronized(this.otLimitedPayPeriodMap) {
			this.otLimitedPayPeriodMap.clear();			
		}
		synchronized(this.otLimitedPayPeriods) {
			this.otLimitedPayPeriods = new SelectItem[0];			
		}		
	}
	private void buildSelectItems() throws AlohaServerException{ 
		this.buildOTPayPeriodMap();

		GregorianCalendar today = new GregorianCalendar();
		today.setTime(new Date());
		int currentYear = today.get(Calendar.YEAR);
		int sevenYearsAgo = currentYear - 7;
		
		List<Integer> keyList = new ArrayList<Integer>();
		for ( Integer key : this.otPayPeriodMap.keySet()) {
			if ( key.intValue() > sevenYearsAgo) {
				keyList.add(key);				
			}
		}
		Collections.reverse(keyList);
		
		synchronized(this.otPayPeriods) {
		    this.otPayPeriods = null;
			this.otPayPeriods = new SelectItem[keyList.size()];
			int index = 0;
			for ( Integer year : keyList ) {
				List<OTPayPeriod> otPayPeriods = this.otPayPeriodMap.get(year);
				SelectItem[] selectItemsForYear = this.buildSelectItem(year, otPayPeriods);
				SelectItemGroup selectItemGroupForYear = new SelectItemGroup(year.toString() + ":", year.toString() 
						+ " Pay Periods", false, selectItemsForYear);
				this.otPayPeriods[index++] = selectItemGroupForYear;
			}
		}
	}
	
	private void buildOTPayPeriodMap() throws AlohaServerException {		
		List<OTPayPeriod> list = this.overtimeEJB.retrieveAllOTPayPeriod();
		synchronized(this.otPayPeriodMap) {
			this.otPayPeriodMap.clear();
			for (OTPayPeriod otPayPeriod : list) {
				List<OTPayPeriod> yearList = otPayPeriodMap.get(otPayPeriod.getYear());
				if ( yearList == null) {
					yearList = new ArrayList<OTPayPeriod>();
					this.otPayPeriodMap.put(otPayPeriod.getYear(), yearList);
				}
				yearList.add(otPayPeriod);					
			}				
		}
	}
	
	private void buildLimitedSelectItems() throws AlohaServerException{ 
		this.buildLimitedOTPayPeriodMap();

		GregorianCalendar today = new GregorianCalendar();
		today.setTime(new Date());
		int currentYear = today.get(Calendar.YEAR);
		int sevenYearsAgo = currentYear - 7;
		
		List<Integer> keyList = new ArrayList<Integer>();
		for ( Integer key : this.otLimitedPayPeriodMap.keySet()) {
			if ( key.intValue() > sevenYearsAgo) {
				keyList.add(key);				
			}
		}
		Collections.reverse(keyList);
		
		synchronized(this.otLimitedPayPeriods) {
		    this.otLimitedPayPeriods = null;
			this.otLimitedPayPeriods = new SelectItem[keyList.size()];
			int index = 0;
			for ( Integer year : keyList ) {
				List<OTPayPeriod> otLimitedPPs = this.otLimitedPayPeriodMap.get(year);
				SelectItem[] selectItemsForYear = this.buildSelectItem(year, otLimitedPPs);
				SelectItemGroup selectItemGroupForYear = new SelectItemGroup(year.toString() + ":", year.toString() 
						+ " Pay Periods", false, selectItemsForYear);
				this.otLimitedPayPeriods[index++] = selectItemGroupForYear;
			}
		}
	}	
	
	private void buildLimitedOTPayPeriodMap() throws AlohaServerException {
		Date dateStop = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
        	dateStop = sdf.parse("2018-05-27");
		} catch (ParseException e) {
			e.printStackTrace();
			throw new AlohaServerException(e);
		}
        
		List<OTPayPeriod> list = this.overtimeEJB.retrieveAllOTPayPeriod();
		synchronized(this.otLimitedPayPeriodMap) {
			this.otLimitedPayPeriodMap.clear();
			for (OTPayPeriod otPayPeriod : list) {
				if (otPayPeriod.getEndDate().compareTo(dateStop) < 0) {
					List<OTPayPeriod> yearList = otLimitedPayPeriodMap.get(otPayPeriod.getYear());
					if ( yearList == null) {
						yearList = new ArrayList<OTPayPeriod>();
						this.otLimitedPayPeriodMap.put(otPayPeriod.getYear(), yearList);
					}
					yearList.add(otPayPeriod);					
				}
			}				
		}
	}	
	
	private synchronized SelectItem[] buildSelectItem(Integer year, List<OTPayPeriod> otPayPeriods) {
		SelectItem[] selectItems = new SelectItem[otPayPeriods.size()];
		int index = 0;
		for ( OTPayPeriod otPayPeriod : otPayPeriods) {
			selectItems[index++] = new SelectItem(otPayPeriod.getKey(), otPayPeriod.getLabel());
		}
		return selectItems;
	}
	public SelectItem[] getOtPayPeriods() {
		if ( (this.otPayPeriods == null) || (this.otPayPeriods.length == 0) ) {
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
		return this.otPayPeriods;
	}
	
	public SelectItem[] getOtLimitedPayPeriods() {
		if ( (this.otLimitedPayPeriods == null) || (this.otLimitedPayPeriods.length == 0) ) {
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
		return this.otLimitedPayPeriods;
	}	
	public OTPayPeriod getPayPeriodForStartDate(String val) throws ParseException {
		for (Entry<Integer, List<OTPayPeriod>> entry : this.otPayPeriodMap.entrySet()) {
			List<OTPayPeriod> payPeriodList = entry.getValue();
			for (OTPayPeriod payPeriod : payPeriodList) {
				if (payPeriod.getKey() == Long.parseLong(val)) {
					return payPeriod;
				}
			}
		}
		return null;
	}	
}