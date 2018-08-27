package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveTypeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

@ManagedBean(name="leaveTypeMB", eager=false)
@SessionScoped
public class LeaveTypeMB implements Serializable {
	private static final long serialVersionUID = -8544857480365450023L;

	@EJB
	private LeaveTypeEJB leaveTypeEJB;

	private List<LeaveType> leaveTypeList = new ArrayList<LeaveType>();

	private SelectItem[] leaveTypes = new SelectItem[0];
	public void setLeaveTypes(SelectItem[] leaveTypes) {
		this.leaveTypes = leaveTypes;
	}
	public SelectItem[] getLeaveTypes() {
		return this.leaveTypes;
	}	
	
	
	@PostConstruct
//	public void refresh(){
////		2012-10-02 JJM 48969: Commented out since replaced be getLeaveTypesEff and getLeaveTypesEffOBO
////		try {
////			this.buildLeaveTypes();
////		} catch (AlohaServerException ase) {
////			ase.printStackTrace();
////			this.clearState();
////		} catch (Exception e) {
////			e.printStackTrace();
////			this.clearState();
////		}
//	}			
////	private void clearState() {
////		synchronized(this.leaveTypeList) {
////			this.leaveTypeList.clear();	
////		}
////		synchronized(this.leaveTypes) {
////			this.leaveTypes = new SelectItem[0];	
////		}
////	}	
	private void buildLeaveTypes() throws AlohaServerException {
		synchronized(this.leaveTypeList) {
			this.leaveTypeList.clear();
			this.leaveTypeList = this.leaveTypeEJB.getAllLeaveTypes();				
		}
		
		int size = leaveTypeList.size();
		
		synchronized(this.leaveTypes) {
			this.leaveTypes = null;
			this.leaveTypes = new SelectItem[size];
			for (int ii = 0; ii < size; ii++) {
				LeaveType leaveType = leaveTypeList.get(ii);
				leaveTypes[ii] = new SelectItem(leaveType.getValue(), leaveType.getLabel());
			}
		}
	}
	
	//2012-10-02 JJM 48969: Add buildLeaveTypesEff()
	private void buildLeaveTypesEff(Date ppEffDate) throws AlohaServerException {
		synchronized(this.leaveTypeList) {
			this.leaveTypeList.clear();
			this.leaveTypeList = this.leaveTypeEJB.getAllLeaveTypesEff(ppEffDate);				
		}
		
		int size = leaveTypeList.size();
		
		synchronized(this.leaveTypes) {
			this.leaveTypes = null;
			this.leaveTypes = new SelectItem[size];
			for (int ii = 0; ii < size; ii++) {
				LeaveType leaveType = leaveTypeList.get(ii);
				leaveTypes[ii] = new SelectItem(leaveType.getValue(), leaveType.getLabel());
			}
		}
	}

	//2012-10-02 JJM 48969: Add buildLeaveTypesEffOBO()
	private void buildLeaveTypesEffOBO(Date ppEffDate) throws AlohaServerException {
		synchronized(this.leaveTypeList) {
			this.leaveTypeList.clear();
			this.leaveTypeList = this.leaveTypeEJB.getAllLeaveTypesEffOBO(ppEffDate);				
		}
		
		int size = leaveTypeList.size();
		
		synchronized(this.leaveTypes) {
			this.leaveTypes = null;
			this.leaveTypes = new SelectItem[size];
			for (int ii = 0; ii < size; ii++) {
				LeaveType leaveType = leaveTypeList.get(ii);
				leaveTypes[ii] = new SelectItem(leaveType.getValue(), leaveType.getLabel());
			}
		}
	}

	public LeaveType getLeaveType(String primaryCode) throws AlohaServerException {
		if ( this.leaveTypeList.isEmpty()) {
			this.buildLeaveTypes();
		}
		for ( LeaveType leaveType : this.leaveTypeList) {
			if ( (leaveType.getPrimaryCode().equals(primaryCode)) 
					&& (leaveType.getSecondaryCode() == null) ) {
				return leaveType;
			}
		}
		return null;
	}
	public LeaveType getLeaveType(String primaryCode, String secondaryCode) throws AlohaServerException {
		if ( this.leaveTypeList.isEmpty()) {
			this.buildLeaveTypes();
		}
		for ( LeaveType leaveType : this.leaveTypeList) {
			if ( (leaveType.getPrimaryCode().equals(primaryCode)) 
					&& (leaveType.hasSecondaryCode())
					&& (leaveType.getSecondaryCode().equals(secondaryCode)) ) {
				return leaveType;
			}
		}
		return null;
	}	

	//2012-10-02 JJM 48969: Added to get Effective Leave Types for Employees
	public SelectItem[] getLeaveTypesEff(Date ppEffDate) {
//		if ( (this.leaveTypes == null) || (this.leaveTypes.length == 0) ) {
			try {
				this.buildLeaveTypesEff(ppEffDate);
			} catch (AlohaServerException e) {
				e.printStackTrace();
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
				} catch( IOException ioe) {
					ioe.printStackTrace();
				}
			}
//		}
		return this.leaveTypes;
	}
	//2012-10-02 JJM 48969: Added to get Effective Leave Types for Timekeepers / Supervisors
	public SelectItem[] getLeaveTypesEffOBO(Date ppEffDate) {
//		if ( (this.leaveTypes == null) || (this.leaveTypes.length == 0) ) {
			try {
				this.buildLeaveTypesEffOBO(ppEffDate);
			} catch (AlohaServerException e) {
				e.printStackTrace();
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);				
				} catch( IOException ioe) {
					ioe.printStackTrace();
				}
			}
//		}
		return this.leaveTypes;
	}

}