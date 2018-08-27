package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

@ManagedBean(name="userPrefMB")
@ViewScoped
public class UserPrefMB implements Serializable {

	private static final long serialVersionUID = -41639586928470152L;
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;
	
	@EJB private UserEJB userEJB;
	@EJB private EmployeeEJB employeeEJB;
	
	//these 3 fields are used for current user's default approver
	private AlohaUserPref userPref;
	private SelectItem[] approvers;
	private long selectedApprover;
	
	//these 3 fields are used for current user's default timekeeper
	private SelectItem[] timekeepers;
	private long selectedTimekeeper;
	
	//these 3 fields are used for Team's user preferences
	private List<AlohaUser> managedStaff; //userPrefs.xhtml datatable value 
	private Map<String, AlohaUserPref> userPrefs; //idx=loginName, value=UserPref; selectOneMenu value i.e. selected value i.e. value="#{userPrefMB.userPrefs[upItem.loginName].defaultApproverUserId}"
	private Map<Long, SelectItem[]> siTeamApprovers; //idx=user, value=all approvers for user; selectItems value="#{userPrefMB.siTeamApprovers[upItem.teamId]}"
	
	//these 2 fields are used for Team's default timekeeper
	private Map<Long, SelectItem[]> siTeamTimekeepers;
		
	@PostConstruct
	public void init() throws IOException {
		if (userMB.isLoggedIn()) {
//			if ( (this.userMB != null) && (this.userMB.getUser()) != null) {
//				System.out.println("UserPrefMB.init() for: " + this.userMB.getUser().getLoginName());
//			}
			try {
				this.getApprovers();
				this.getTimekeepers();
				this.getUserPref();
			} catch( Throwable t) {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			}			
		}

		if (FacesContext.getCurrentInstance().getViewRoot().getViewId().contains("userPrefs.xhtml") ) {
			try {
				this.doSecurityCheck();
				managedStaff = this.employeeEJB.getManagedStaffForUser(userMB.getUserId());
				if (managedStaff != null) {
					//user has some staff, get approvers by team
					this.fillApproverMap();
					getAllUserPrefs();
				}
			} catch (AuthorizationException ae) {
				try {
					//Navigation will not work so use redirect
					//FacesContext.getCurrentInstance().getExternalContext().redirect("/aloha/faces/pages/public/unauthorized.xhtml");
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
				} catch( IOException ioe) {
					ioe.printStackTrace();
					FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
				}		
			} catch( Throwable t) {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			}				
		}
	}
	
	private void doSecurityCheck() throws AuthorizationException {
		//only approvers and submitothers can run reconciliation
		if ( !this.userMB.isApprover() && !this.userMB.isOnBehalfOf()) {
			throw new AuthorizationException(ErrorMessages.getInstance().getMessage(ErrorMessages.ADXREF_NOTAUTH));
		}
	}	
	public String save() {
		try {
			//inprocess aup - reqd for exception
			//AlohaUserPref aup_inprocess =  new AlohaUserPref();
			//aup_inprocess.
			//refactor if EJB is deployed remotely - pass collection
			//JPA handles case where db update is not issued if no data changed
			if (managedStaff != null) {
				for (AlohaUser au : managedStaff) {
					StopWatch stopWatch = new StopWatch();
					stopWatch.start();
					AlohaUserPref aup = this.userEJB.saveUserPref(this.userMB.getUser(), userPrefs.get(au.getLoginName()));
					stopWatch.stop();
					System.out.println("ELAPSED TIME (Save User Preferences): " + stopWatch.getElapsedTime() + " ms");
					stopWatch = null;					
					
					//replace in collection - required for version 
					userPrefs.put(au.getLoginName(), aup);
				}				
			} else {
				//users own default approver
				if (selectedApprover > 0) {
					userPref.setDefaultApproverUserId(selectedApprover);
				} else {
					userPref.setDefaultApproverUserId(null);
				}
				
				if (selectedTimekeeper > 0) {
					userPref.setDefaultTimekeeperUserId(selectedTimekeeper);
				} else {
					userPref.setDefaultTimekeeperUserId(null);
				}
				//System.out.println("Before save..." + userPref.getVersion());
				//replace with updated object
				userPref = this.userEJB.saveUserPref(this.userMB.getUser(), userPref);
				//System.out.println("After save..." + userPref.getVersion());
				
			}
			
		} catch (AlohaServerException ase) {
			if ( ase.getExceptionType() == AlohaServerException.ExceptionType.OPTIMISTIC_LOCK) {
				
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.OPTIMISTIC_LOCK_MSG);
				
				FacesContextUtil.getHttpSession().setAttribute("userErrorMessage", errMsgText);
				return NavigationOutcomes.USER_ERROR;				
			} else {
				return NavigationOutcomes.SERVER_ERROR;	
			}
		} catch (Exception e) {
			return NavigationOutcomes.SERVER_ERROR;
		} 
		return null;
	}

	private void fillApproverMap() throws AlohaServerException {
		//this method creates a map with etams.team_info.team_id as index and list of approvers for that team as the value
		siTeamApprovers = new TreeMap<Long, SelectItem[]>();
		for (AlohaUser au : managedStaff) {
			if (!siTeamApprovers.containsKey(au.getTeamId())) {
				List<AlohaUser> approversForUser = this.employeeEJB.getLeaveApprovers(au.getUserId());
				int index = 0;
				SelectItem[] si = new SelectItem[approversForUser.size()];
				for (AlohaUser au2 : approversForUser) {
					si[index++] = new SelectItem(au2.getUserId(),au2.getFullName());
					
				}
				siTeamApprovers.put(au.getTeamId(), si);
			}
		}
		
		siTeamTimekeepers = new TreeMap<Long, SelectItem[]>();
		for (AlohaUser au : managedStaff) {
			if (!siTeamTimekeepers.containsKey(au.getTeamId())) {
				List<AlohaUser> timekeepersForUser = this.employeeEJB.getPrimTimekeeper(au.getUserId());
				int index = 0;
				SelectItem[] si = new SelectItem[timekeepersForUser.size()];
				for (AlohaUser au2 : timekeepersForUser) {
					si[index++] = new SelectItem(au2.getUserId(),au2.getFullName());
					
				}
				siTeamTimekeepers.put(au.getTeamId(), si);
			}
		}
	}
	
	private void getAllUserPrefs() throws AlohaServerException {
			userPrefs = new TreeMap<String, AlohaUserPref>();
			for (AlohaUser au : managedStaff) {
				if (!userPrefs.containsKey(au.getLoginName())) {
					AlohaUserPref aup = this.userEJB.getUserPref(au.getUserId());
					if (aup == null) {
						aup = new AlohaUserPref();
						aup.setUserId(au.getUserId());
					} 
					userPrefs.put(au.getLoginName(), aup);
				}
			}
		
	}	
	
	/****************************
	 * Getters and Setters
	 ****************************/
	public void setApprovers(SelectItem[] approvers) {
		this.approvers = approvers;
	}

	public SelectItem[] getApprovers() throws AlohaServerException {
		if (approvers == null && userMB.isLoggedIn()) {

//			if ( (this.userMB != null) && (this.userMB.getUser()) != null) {
//				System.out.println("UserPrefMB.getApprovers() for: " + this.userMB.getUser().getLoginName());				
//			}
			
			List<AlohaUser> approversForUser = this.employeeEJB.getLeaveApprovers(userMB.getUserId());
			this.approvers = new SelectItem[approversForUser.size()];
			int index = 0;
			for (AlohaUser alohaUser : approversForUser) {
				//method does not return loginname (ent userid)
				//approvers[index++] = new SelectItem(alohaUser.getLoginName(),alohaUser.getFullName());
				approvers[index++] = new SelectItem(alohaUser.getUserId(),alohaUser.getFullName());
			}
		}
		return approvers;
	}

	public void setUserPref(AlohaUserPref userPref) {
		this.userPref = userPref;
	}
	public AlohaUserPref getUserPref() throws AlohaServerException {
		if (userPref == null && userMB.isLoggedIn()) {

//			if ( (this.userMB != null) && (this.userMB.getUser()) != null) {
//				System.out.println("UserPrefMB.getUserPref() for: " + this.userMB.getUser().getLoginName());				
//			}
			
				userPref = this.userEJB.getUserPref(userMB.getUserId());
				if (userPref == null) {
					userPref = new AlohaUserPref();
					userPref.setUserId(userMB.getUserId());
				} else {
					if (userPref.getDefaultApproverUserId() == null) {
						this.selectedApprover = 0;
					} else {
						this.selectedApprover = userPref.getDefaultApproverUserId();	
					}
				}
		}
		return userPref;
	}
	
	public void setTimekeepers(SelectItem[] timekeepers) {
		this.timekeepers = timekeepers;
	}

	public SelectItem[] getTimekeepers() throws AlohaServerException {
		if (timekeepers == null && userMB.isLoggedIn()) {

//			if ( (this.userMB != null) && (this.userMB.getUser()) != null) {
//				System.out.println("UserPrefMB.getTimekeepers() for: " + this.userMB.getUser().getLoginName());
//			}
			
			List<AlohaUser> timekeepersForUser = this.employeeEJB.getPrimTimekeeper(userMB.getUserId());
			this.timekeepers = new SelectItem[timekeepersForUser.size()];
			int index = 0;
			for (AlohaUser alohaUser : timekeepersForUser) {
				//method does not return loginname (ent userid)
				//approvers[index++] = new SelectItem(alohaUser.getLoginName(),alohaUser.getFullName());
				timekeepers[index++] = new SelectItem(alohaUser.getUserId(),alohaUser.getFullName());
			}
		}
		return timekeepers;
	}
	
	public UserMB getUserMB() {
		return userMB;
	}
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	public void setSelectedApprover(long selectedApprover) {
		this.selectedApprover = selectedApprover;
	}
	public long getSelectedApprover() {
		return selectedApprover;
	}

	public void setManagedStaff(List<AlohaUser> managedStaff) {
		this.managedStaff = managedStaff;
	}
	public List<AlohaUser> getManagedStaff() {
		return managedStaff;
	}

	public void setSelectedTimekeeper(long selectedTimekeeper) {
		this.selectedTimekeeper = selectedTimekeeper;
	}
	public long getSelectedTimekeeper() {
		return selectedTimekeeper;
	}

	public void setSiTeamApprovers(Map<Long, SelectItem[]> siTeamApprovers) {
		this.siTeamApprovers = siTeamApprovers;
	}

	public Map<Long, SelectItem[]> getSiTeamApprovers() {
		return siTeamApprovers;
	}
	
	public void setSiTeamTimekeepers(Map<Long, SelectItem[]> siTeamTimekeepers) {
		this.siTeamTimekeepers = siTeamTimekeepers;
	}
	
	public Map<Long, SelectItem[]> getSiTeamTimekeepers() {
		return siTeamTimekeepers;
	}

	public void setUserPrefs(Map<String, AlohaUserPref> userPrefs) {
		this.userPrefs = userPrefs;
	}

	public Map<String, AlohaUserPref> getUserPrefs() {
		return userPrefs;
	}
	
}