package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveStatusEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatusTransition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="leaveStatusMB", eager=false)
@SessionScoped
public class LeaveStatusMB implements Serializable {
	private static final long serialVersionUID = 559090077942681155L;

	@EJB
	private LeaveStatusEJB ejb;
	
	private List<LeaveStatus> leaveStatusList = new ArrayList<LeaveStatus>();
	private List<LeaveStatusTransition> leaveStatusTransitionList = new ArrayList<LeaveStatusTransition>();
	
	private Map<String, LeaveStatus> leaveStatusMap = new HashMap<String, LeaveStatus>();
	private Map<String, LeaveStatusTransition> leaveStatusTransitionMap = new HashMap<String, LeaveStatusTransition>();

	@PostConstruct
	public void refresh() {
		try {
			this.initLeaveStatuses();
			this.initLeaveStatusTransitions();
		} catch (AlohaServerException ase) {
			ase.printStackTrace();
			this.clearState();
		} catch (Exception e) {
			e.printStackTrace();
			this.clearState();
		}
	}
	private void clearState() {
		synchronized(this.leaveStatusList) {
			this.leaveStatusList.clear();
		}
		synchronized(this.leaveStatusMap) {
			this.leaveStatusMap.clear();
		}
		synchronized(this.leaveStatusTransitionList) {
			this.leaveStatusTransitionList.clear();
		}
		synchronized(this.leaveStatusTransitionMap) {
			this.leaveStatusTransitionMap.clear();
		}
	}
	private void initLeaveStatuses() throws AlohaServerException{
		synchronized(this.leaveStatusList) {
			this.leaveStatusList.clear();
			this.leaveStatusList = this.ejb.getAllLeaveStatuses();
		}
		synchronized(this.leaveStatusMap) {
			this.leaveStatusMap.clear();
			for ( LeaveStatus model: this.leaveStatusList) {
				this.leaveStatusMap.put(model.getCode(), model);
			}
		}
	}
	private void initLeaveStatusTransitions() throws AlohaServerException {
		synchronized(this.leaveStatusTransitionList) {
			this.leaveStatusTransitionList.clear();
			this.leaveStatusTransitionList = this.ejb.getAllLeaveStatusTransitions();			
		}
		synchronized(this.leaveStatusTransitionMap) {
			this.leaveStatusTransitionMap.clear();
			for ( LeaveStatusTransition model: this.leaveStatusTransitionList) {
				this.leaveStatusTransitionMap.put(model.getActionCode(), model);
			}
		}
	}
	public LeaveStatus getLeaveStatus(String code) throws AlohaServerException{
		if ( this.leaveStatusMap.isEmpty() ) {
			this.initLeaveStatuses();
		}
		return this.leaveStatusMap.get(code);
	}
	public LeaveStatusTransition getLeaveStatusTransition(String actionCode) throws AlohaServerException {
		if ( this.leaveStatusTransitionMap.isEmpty() ) {
			this.initLeaveStatusTransitions();
		}
		return this.leaveStatusTransitionMap.get(actionCode);
	}	
	public List<LeaveStatus> getAllLeaveStatuses() throws AlohaServerException {
		if ( this.leaveStatusList.isEmpty() ) {
			this.initLeaveStatuses();
		}
		return this.leaveStatusList;
	}	
	public List<LeaveStatusTransition> getAllLeaveStatusTransitions() throws AlohaServerException {
		if ( this.leaveStatusTransitionList.isEmpty() ) {
			this.initLeaveStatuses();
		}
		return this.leaveStatusTransitionList;
	}
}