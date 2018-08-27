package gov.gsa.ocfo.aloha.web.mb.ot.list.mgr;

import gov.gsa.ocfo.aloha.ejb.overtime.GroupOvertimeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OTIndivListEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTIndivStatus;
import gov.gsa.ocfo.aloha.model.overtime.OTListRow;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;
import gov.gsa.ocfo.aloha.web.util.OTGroupStatusChangeHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.icesoft.faces.facelets.component.table.autosort.AutoSortTableBean;

@ManagedBean(name=OTMgrListMB.MANAGED_BEAN_NAME)
@ViewScoped
public class OTMgrListMB implements Serializable {
	private static final long serialVersionUID = 4371959855077569288L;
	public  static final String MANAGED_BEAN_NAME = "otMgrListMB";
	private static final String ID = "id";
	
	@EJB
	protected OTIndivListEJB otIndivListEJB;	
	
	@EJB
	private GroupOvertimeEJB groupOvertimeEJB;	
	
	// JSF Managed Bean
	@ManagedProperty(value = "#{userMB}")
	private UserMB userMB;
	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}
	
	// MANAGER'S USER_ID
	private long mgrUserId;
	
    // INCOMING INDIVIDUAL REQUESTS
    private AutoSortTableBean incomingIndivRequests;
       
    // RECEIVED INDIVIDUAL REQUESTS
    private AutoSortTableBean receivedIndivRequests;

    // APPROVED INDIVIDUAL REQUESTS
    private AutoSortTableBean approvedIndivRequests;
    
    // DENIED INDIVIDUAL REQUESTS
    private AutoSortTableBean deniedIndivRequests;
    
    // CANCELLED INDIVIDUAL REQUESTS
    private AutoSortTableBean cancelledIndivRequests;
      
 	  // INCOMING GROUP REQUESTS
    private List<OTGroup> incomingGroupRequests;	
	
	  // PENDING GROUP REQUESTS
    private List<OTGroup> pendingGroupRequests;	
    
	  // SUBMITTED GROUP REQUESTS
    private List<OTGroup> submittedGroupRequests;	  
    
	  // FINAL GROUP REQUESTS
    private List<OTGroup> finalGroupRequests;	   
	
	  // CANCELLED GROUP REQUESTS
    private List<OTGroup> cancelledGroupRequests;	  
    
	@PostConstruct
	public void init() {
		try {
			this.checkAccessRights();
			this.mgrUserId = this.userMB.getUserId();
			this.buildGroupLists();
			this.buildIndividualLists();
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch (IOException ignore) {};
		} catch (AlohaServerException ase) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch( Throwable t) {
			t.printStackTrace();
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {};
		}
	}
	private void buildGroupLists() throws AlohaServerException {
		StopWatch stopWatch = new StopWatch();
		
		stopWatch.start();
		List<OTGroup> unfilteredGroupList = this.groupOvertimeEJB.retrieveOTGroupsForManager(this.mgrUserId);
		stopWatch.stop();
		System.out.println("ELAPSED TIME [OTMgrListMB.buildGroupLists.retrieveOTGroupsForManager]: " + stopWatch.getElapsedTime() + " ms");
		System.out.println("groupList.size(): " + unfilteredGroupList.size());				

		
		//SAK 20130129 added sort on PP
		Collections.sort(unfilteredGroupList, new Comparator<OTGroup>() {
			@Override
			public int compare(OTGroup otg1, OTGroup otg2) {
				//return otg1.getPayPeriodDateRange().compareTo(otg2.getPayPeriodDateRange());
				return ((otg2.getPayPeriod().getYear() + otg2.getPayPeriod().getNumber() - otg1.getPayPeriod().getYear() + otg1.getPayPeriod().getNumber()));  
			}
		});

		OTGroupStatusChangeHelper.determineModifiablityOfAllIndividualRequestsInGroupList(this.userMB.getUser(), unfilteredGroupList);	
		OTGroupStatusChangeHelper.determineCancelablityOfAllIndividualRequestsInGroupList(this.userMB.getUser(), unfilteredGroupList);		
		OTGroupStatusChangeHelper.determineCancellablityOfAllGroupsInList(this.userMB.getUser(), unfilteredGroupList);		
		OTGroupStatusChangeHelper.determineFinalizablityOfAllGroupsInList(this.userMB.getUser(), unfilteredGroupList);		
	
		// INCOMING GROUP REQUESTS
		this.incomingGroupRequests = this.buildIncomingGroupRequests(unfilteredGroupList);
		
		// PENDING GROUP REQUESTS
		this.pendingGroupRequests = this.buildPendingGroupRequests(unfilteredGroupList);
		
		// SUBMITTED GROUP REQUESTS
		this.submittedGroupRequests = this.buildSubmittedGroupRequests(unfilteredGroupList);
		
		// FINAL GROUP REQUESTS
		this.finalGroupRequests = this.buildFinalGroupRequests(unfilteredGroupList);
		
		// CANCELLED GROUP REQUESTS
		this.cancelledGroupRequests = this.buildCancelledGroupRequests(unfilteredGroupList);	
		
		stopWatch = null;
	}
	
//	private void debug(List<OTGroup> list) {
//		for ( OTGroup model: list) {
//			System.out.println(model);
//		}
//	}
	
	private void buildIndividualLists() throws AlohaServerException {
	
		// ALL INDIVIDUAL REQUESTS
		List<OTListRow> unfilteredIndivList = this.otIndivListEJB.retrieveOTIndivListForSupervisor(this.mgrUserId);
		
		 // INCOMING INDIVIDUAL REQUESTS
		this.incomingIndivRequests = new AutoSortTableBean(this.buildIncomingIndivRequests(unfilteredIndivList), ID);			
		
		// RECEIVED INDIVIDUAL REQUESTS
		this.receivedIndivRequests = new AutoSortTableBean(this.buildReceivedIndivRequests(unfilteredIndivList), ID);
		
		  // APPROVED INDIVIDUAL REQUESTS  
		this.approvedIndivRequests = new AutoSortTableBean(this.buildApprovedIndivRequests(unfilteredIndivList), ID);
		
		// DENIED INDIVIDUAL REQUESTS
		this.deniedIndivRequests = new AutoSortTableBean(this.buildDeniedIndivRequests(unfilteredIndivList), ID);
		
		// CANCELLED INDIVIDUAL REQUESTS		
		this.cancelledIndivRequests = new AutoSortTableBean(this.buildCancelledIndivRequests(unfilteredIndivList), ID);		
	}
	
	private void checkAccessRights() throws AuthorizationException {
		if ( !this.userMB.isApprover() ) {
			Object[] params = { this.userMB.getFullName() };
			String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.OT_LIST_UNAUTHORIZED_MGR, params);
			FacesContextUtil.getHttpSession().setAttribute(AlohaConstants.UNAUTHORIZED_MESSAGE, errMsg);
			throw new AuthorizationException(errMsg);
		}
	}
	// INCOMING INDIVIDUAL REQUESTS
	private List<OTListRow> buildIncomingIndivRequests(List<OTListRow> unfilteredList) throws AlohaServerException {
		return this.filterIndivList(unfilteredList, new String[] {OTIndivStatus.CodeValues.SUBMITTED, OTIndivStatus.CodeValues.RESUBMITTED, OTIndivStatus.CodeValues.MODIFIED});
	} 
	// RECEIVED INDIVIDUAL REQUESTS
	private List<OTListRow> buildReceivedIndivRequests(List<OTListRow> unfilteredList) throws AlohaServerException {
		return this.filterIndivList(unfilteredList, new String[] {OTIndivStatus.CodeValues.RECEIVED, OTIndivStatus.CodeValues.RECEIVED_WITH_MODS});
	} 
	// APPROVED INDIVIDUAL REQUESTS
	private List<OTListRow> buildApprovedIndivRequests(List<OTListRow> unfilteredList) throws AlohaServerException {
		return this.filterIndivList(unfilteredList, OTIndivStatus.CodeValues.APPROVED);
	} 
	// DENIED INDIVIDUAL REQUESTS
	private List<OTListRow> buildDeniedIndivRequests(List<OTListRow> unfilteredList) throws AlohaServerException {
		return this.filterIndivList(unfilteredList, OTIndivStatus.CodeValues.DENIED);
	} 
	// CANCELLED INDIVIDUAL REQUESTS
	private List<OTListRow> buildCancelledIndivRequests(List<OTListRow> unfilteredList) throws AlohaServerException {
		return this.filterIndivList(unfilteredList, OTIndivStatus.CodeValues.CANCELLED);
	} 	
	private List<OTListRow> filterIndivList(List<OTListRow> unfilteredList, String statusCodeValueToFilter) {
		List<OTListRow> filteredList = new ArrayList<OTListRow>();
		
		for ( OTListRow otListRow : unfilteredList ) {
			if (otListRow.getStatusCode().equals(statusCodeValueToFilter))  {
				filteredList.add(otListRow);
			}				
		}
		return filteredList;
	}	
	private List<OTListRow> filterIndivList(List<OTListRow> unfilteredList, String[] statusCodesToFilter) {
		List<OTListRow> filteredList = new ArrayList<OTListRow>();
		
		for ( OTListRow otListRow : unfilteredList ) {
			for ( String statusCodeToFilter : statusCodesToFilter) {
				if ( statusCodeToFilter.equals(otListRow.getStatusCode() )) {
					filteredList.add(otListRow);		
				}
			}			
		}
		return filteredList;
	}	
	
	// INCOMING GROUP REQUESTS
	private List<OTGroup> buildIncomingGroupRequests(List<OTGroup> unfilteredList) throws AlohaServerException {
		List<OTGroup> filteredList = new ArrayList<OTGroup>();

		for ( OTGroup otGroup : unfilteredList ) {
			if ( 	(otGroup.getReceiverUserId() != null )
					&& (otGroup.getReceiverUserId().longValue() == this.userMB.getUserId() )
					&& ( otGroup.getStatusCode().equals(OTGroupStatus.CodeValues.SUBMITTED) ) )  {
				filteredList.add(otGroup);
			}				
		}	
		return filteredList;		
	}	
	
	
	// PENDING GROUP REQUESTS
	private List<OTGroup> buildPendingGroupRequests(List<OTGroup> unfilteredList) throws AlohaServerException {
		List<OTGroup> filteredList = new ArrayList<OTGroup>();

		for ( OTGroup otGroup : unfilteredList ) {
			if ( (otGroup.getSubmitterUserId() == this.userMB.getUserId() )
					&& ( otGroup.getStatusCode().equals(OTGroupStatus.CodeValues.PENDING) ) )  {
				filteredList.add(otGroup);
			}				
		}	
		return filteredList;		
	}		
	
	
	// SUBMITTED GROUP REQUESTS
	private List<OTGroup> buildSubmittedGroupRequests(List<OTGroup> unfilteredList) throws AlohaServerException {
		List<OTGroup> filteredList = new ArrayList<OTGroup>();

		for ( OTGroup otGroup : unfilteredList ) {
			if	( (otGroup.getSubmitterUserId() == this.userMB.getUserId())
					&& 	( otGroup.getStatusCode().equals(OTGroupStatus.CodeValues.SUBMITTED)
							|| otGroup.getStatusCode().equals(OTGroupStatus.CodeValues.RECEIVED)
						)
				) {
				filteredList.add(otGroup);
			}				
		}	
		return filteredList;		
	}		
	
	//FINAL GROUP REQUESTS
	private List<OTGroup> buildFinalGroupRequests(List<OTGroup> unfilteredList) throws AlohaServerException {
		List<OTGroup> finalList = new ArrayList<OTGroup>();

		for ( OTGroup otGroup : unfilteredList ) {
			if 	( (otGroup.getStatusCode().equals(OTGroupStatus.CodeValues.FINAL))
					&& 	( (otGroup.getSubmitter().getUserId() == this.userMB.getUserId())
							|| ( (otGroup.getReceiver().getUserId() == this.userMB.getUserId()) 
									&& !otGroup.hasParent())
						)
				) {
				finalList.add(otGroup);
			}				
		}	
		return finalList;
	}
	
	// CANCELLED GROUP REQUESTS
	private List<OTGroup> buildCancelledGroupRequests(List<OTGroup> unfilteredList) throws AlohaServerException {
		return this.filterGroupList(unfilteredList, OTGroupStatus.CodeValues.CANCELLED);
	}	
	
	private  List<OTGroup> filterGroupList(List<OTGroup> unfilteredList, String statusCodeValueToFilter) {
		List<OTGroup> filteredList = new ArrayList<OTGroup>();

		for ( OTGroup otGroup : unfilteredList ) {
			if (otGroup.getStatusCode().equals(statusCodeValueToFilter))  {
				filteredList.add(otGroup);
			}				
		}	
		
		return filteredList;
	}
	
	public AutoSortTableBean getIncomingIndivRequests() {
		return incomingIndivRequests;
	}

	public AutoSortTableBean getReceivedIndivRequests() {
		return receivedIndivRequests;
	}

	public AutoSortTableBean getApprovedIndivRequests() {
		return approvedIndivRequests;
	}

	public AutoSortTableBean getDeniedIndivRequests() {
		return deniedIndivRequests;
	}

	public AutoSortTableBean getCancelledIndivRequests() {
		return cancelledIndivRequests;
	}

	public List<OTGroup> getIncomingGroupRequests() {
		return incomingGroupRequests;
	}

	public List<OTGroup> getPendingGroupRequests() {
		return pendingGroupRequests;
	}

	public List<OTGroup> getSubmittedGroupRequests() {
		return submittedGroupRequests;
	}

	public List<OTGroup> getFinalGroupRequests() {
		return finalGroupRequests;
	}

	public List<OTGroup> getCancelledGroupRequests() {
		return cancelledGroupRequests;
	}	
}