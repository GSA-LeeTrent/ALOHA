package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.web.model.TimeIntervalGroup;
import gov.gsa.ocfo.aloha.web.model.TimeIntervalItem;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

@ManagedBean(name="timeIntervalMB", eager=true)
@ApplicationScoped
public class TimeIntervalMB implements Serializable {
	private static final long serialVersionUID = -7694604294553690033L;
	private static final int[] HOURS = {6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,0,1,2,3,4,5};

	private SelectItem[] selectItems = new SelectItem[HOURS.length];

	@PostConstruct
	public void init() {
		try {
			this.buildSelectItems();
			//this.debug();
		} catch( Throwable t) {
			t.printStackTrace();
		}
	}	
	private synchronized void buildSelectItems() {
		TimeIntervalGroup[] tiGroups = new TimeIntervalGroup[HOURS.length];
		for ( int ii = 0; ii < HOURS.length; ii++ ) {
			tiGroups[ii] = new TimeIntervalGroup(HOURS[ii]);
			SelectItem[] tiSelectItems = this.buildSelectItem(tiGroups[ii].getItems());
			SelectItemGroup sig = new SelectItemGroup(tiGroups[ii].getLabel(), tiGroups[ii].getDescription(), false, tiSelectItems);	
			this.selectItems[ii] = sig;

		}
	}
	/*
	private void debug() {
		for ( int ii = 0; ii < selectItems.length; ii++ ) {
			System.out.println(selectItems[ii].getValue() + " : " + selectItems[ii].getValue());
		}
	}
	*/
	private synchronized SelectItem[] buildSelectItem(TimeIntervalItem[] tiItems ) {
		SelectItem[] selectItems = new SelectItem[tiItems.length];
		for ( int ii = 0; ii < selectItems.length; ii++) {
			selectItems[ii] = new SelectItem(tiItems[ii].getValue(), tiItems[ii].getLabel());
		}
		return selectItems;
	}
	public SelectItem[] getSelectItems() {
		return selectItems;
	}		
}