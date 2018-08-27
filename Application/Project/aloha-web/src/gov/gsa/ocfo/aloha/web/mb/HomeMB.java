package gov.gsa.ocfo.aloha.web.mb;

import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name="homeMB")
@ViewScoped
public class HomeMB implements Serializable {
	private static final long serialVersionUID = 3304985837976216192L;
	// JSF BEAN
	@ManagedProperty(value="#{userMB}")
	protected UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	// JSF BEAN
	@ManagedProperty(value="#{constantsMB}")
	protected ConstantsMB constantsMB;	
	public void setConstantsMB(ConstantsMB constantsMB) {
		this.constantsMB = constantsMB;
	}
	private String featuredTab;
	private int tabIndexForTab;
	
	@PostConstruct
	public void init() {
		//System.out.println("HomeMB.init() : BEGIN");
		try {
			if ( (this.userMB == null) || (this.userMB.getUser() == null) ) {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.LOGIN);
			} else {
				//System.out.println("alohaUser: " + this.userMB.getUser());
				this.featuredTab = this.determineFeaturedTab();
				//System.out.println("featuredTab: " + this.featuredTab);
				this.tabIndexForTab = 1;
			}
		} catch (FacesException e) {
			e.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		} catch (IOException e) {
			e.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		} catch (Exception e) {
			e.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}
		//System.out.println("HomeMB.init() : END");
	}
	
//	private String determineFeaturedTab() {
//		Cookie tabCookie = CookieUtil.findCookie(this.constantsMB.getLastAlohaHomeTabVisited());
//		if ( tabCookie != null) {
//			return tabCookie.getValue();
//		} else {
//			AlohaUser user = this.userMB.getUser();
//			if ( user.isSubmitOwn()) {
//				return this.constantsMB.getTabMyRequests();
//			} else if ( user.isOnBehalfOf()) {
//				return this.constantsMB.getTabMyTeam();
//			} else if ( user.isApprover()) {
//				return this.constantsMB.getTabManagement();
//			} else if ( user.isAlohaAdmin()) {
//				return this.constantsMB.getTabAdmin();
//			}
//		}
//		// WE SHOULD NEVER GET TO THIS LINE OF CODE
//		return this.constantsMB.getTabReports(); // SubmitOwn, OnBehalfOf and Approver have this tab
//	}
	
	private String determineFeaturedTab() {
		return this.constantsMB.getTabReports(); // SubmitOwn, OnBehalfOf and Approver have this tab
	}	
	
	public String getFeaturedTab() {
		return featuredTab;
	}

	public int getTabIndexForTab() {
		return tabIndexForTab++;
	}

	public String getMyRequestsSectionDisplay() {
		if ( this.featuredTab.equals(this.constantsMB.getTabMyRequests())) {
			return this.constantsMB.getDisplayBlock();
		} else {
			return this.constantsMB.getDisplayNone();
		}
	}		
	public String getMyTeamSectionDisplay() {
		if ( this.featuredTab.equals(this.constantsMB.getTabMyTeam())) {
			return this.constantsMB.getDisplayBlock();
		} else {
			return this.constantsMB.getDisplayNone();
		}
	}		

	public String getReportsSectionDisplay() {
		if ( this.featuredTab.equals(this.constantsMB.getTabReports())) {
			return this.constantsMB.getDisplayBlock();
		} else {
			return this.constantsMB.getDisplayNone();
		}
	}			
	
	public String getManagementSectionDisplay() {
		if ( this.featuredTab.equals(this.constantsMB.getTabManagement())) {
			return this.constantsMB.getDisplayBlock();
		} else {
			return this.constantsMB.getDisplayNone();
		}
	}
	public String getAdminSectionDisplay() {
		if ( this.featuredTab.equals(this.constantsMB.getTabAdmin())) {
			return this.constantsMB.getDisplayBlock();
		} else {
			return this.constantsMB.getDisplayNone();
		}
	}	

	public String getCssClassForMyRequestsTab() {
		if	( (!StringUtil.isNullOrEmpty(this.featuredTab)) 
				&& (this.featuredTab.equals(this.constantsMB.getTabMyRequests()))
			) {
			return this.constantsMB.getCssClassForSelectedHomeTab();
		} else {
			return this.constantsMB.getCssClassForNotSelectedHomeTab();
		}		
	}
	
	public String getCssClassForMyTeamTab() {
		if ( this.featuredTab.equals(this.constantsMB.getTabMyTeam())) {
			return this.constantsMB.getCssClassForSelectedHomeTab();
		} else {
			return this.constantsMB.getCssClassForNotSelectedHomeTab();
		}		
	}
	
	public String getCssClassForReportsTab() {
		if ( this.featuredTab.equals(this.constantsMB.getTabReports())) {
			return this.constantsMB.getCssClassForSelectedHomeTab();
		} else {
			return this.constantsMB.getCssClassForNotSelectedHomeTab();
		}		
	}
	public String getCssClassForManagementTab() {
		if ( this.featuredTab.equals(this.constantsMB.getTabManagement())) {
			return this.constantsMB.getCssClassForSelectedHomeTab();
		} else {
			return this.constantsMB.getCssClassForNotSelectedHomeTab();
		}		
	}	
	public String getCssClassForAdminTab() {
		if ( this.featuredTab.equals(this.constantsMB.getTabAdmin())) {
			return this.constantsMB.getCssClassForSelectedHomeTab();
		} else {
			return this.constantsMB.getCssClassForNotSelectedHomeTab();
		}		
	}		
	public String runLeaveRequestsRecon() {
        return NavigationOutcomes.LEAVE_REQUEST_RECONCILIATION;
	}
	public String runLeaveRequestReport() {
        return NavigationOutcomes.LEAVE_REQUEST_REPORT;
	}
	public String runOvertimeRequestsRecon() {
        return NavigationOutcomes.OVERTIME_REQUEST_RECONCILIATION;
	}
	public String runOvertimeRequestReport() {
        return NavigationOutcomes.OVERTIME_REQUEST_REPORT;
	}	

	/*
	public boolean isShowMyRequestTab() {
		return this.determineFeaturedTab().equals(this.constantsMB.getTabMyRequests());
	}
	public boolean isShowMyTeamTab() {
		return this.determineFeaturedTab().equals(this.constantsMB.getTabMyTeam());
	}
	public boolean isShowReportsTab() {
		return this.determineFeaturedTab().equals(this.constantsMB.getTabReports());
	}
	public boolean isShowManagementTab() {
		return this.determineFeaturedTab().equals(this.constantsMB.getTabManagement());
	}	
	public boolean isShowAdminTab() {
		return this.determineFeaturedTab().equals(this.constantsMB.getTabAdmin());
	}	
*/

}