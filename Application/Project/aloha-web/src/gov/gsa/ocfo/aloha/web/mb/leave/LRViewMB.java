package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.KeyValuePair;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;
import gov.gsa.ocfo.aloha.web.util.FacesContextUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name="lrViewMB")
@ViewScoped
public class LRViewMB extends LRReadOnlyMB  {
	private static final long serialVersionUID = -5168730793482081644L;
	
	@EJB
	private LeaveRequestEJB leaveRequestEJB;

	@EJB
	private EmployeeEJB employeeEJB;

	private AlohaUser currentUser;
	private LeaveDetail leaveDetail;
	private List<KeyValuePair> leaveBalances = new ArrayList<KeyValuePair>();	
	
	@PreDestroy
	public void cleanup() {
		this.leaveDetail = null;
		this.leaveBalances = null;
		this.lrMode = null;
		this.currentUser = null;
	}
	
	@PostConstruct
	public void initView() {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			this.currentUser = this.userMB.getUser();
			
			// GET REQUEST PARAMETER
			String paramVal = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.LR_MODE);

			// IF THE REQUEST PARAMETER IS EMPTY, WE CAN'T CONTINUE
			if (StringUtil.isNullOrEmpty(paramVal)) {
				System.out.println("HTTP Request Parameter \"" + AlohaConstants.LR_MODE + "\" is NULL in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}	
			
			// GET LEAVE REQUEST MODE
			this.lrMode = LRMode.fromString(paramVal);

			// IF LEAVE REQUEST MODE CANNOT BE FOUND, WE CAN'T CONTINUE
			if (this.lrMode == null ) {
				System.out.println("LRMode NOT FOUND for \"" + paramVal + "\" in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}		
			
			// GET REQUEST PARAMETER
			String leaveDetailId = FacesContextUtil.getHttpServletRequest().getParameter(AlohaConstants.LEAVE_DETAIL_ID);		

			// IF THE REQUEST PARAMETER IS EMPTY, WE CAN'T CONTINUE
			if ( StringUtil.isNullOrEmpty(leaveDetailId)) {
				System.out.println("HTTP Parameter \"leaveDetailId\" is NULL in call to " 
						+ this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();
			}
			// RETRIEVE LEAVE DETAIL OBJECT
			this.leaveDetail = this.leaveRequestEJB.getLeaveDetail(Long.parseLong(leaveDetailId));		

			// IF THE LEAVE DETAIL OBJECT IS NULL, WE CAN'T CONTINUE
			if ( this.leaveDetail == null) {
				System.out.println("Retrieval of LeaveDetail object, using HTTP Parameter named \"leaveDetailId\", having a value of " 
									+ leaveDetailId + ", is NULL in call to " + this.getClass().getName() + ".init() method. Cannot continue.");
				throw new IllegalStateException();				
			}
			
			this.checkAccessRights();
			this.leaveBalances = this.employeeEJB.getLeaveBalances(this.leaveDetail.getLeaveHeader().getEmployee().getUserId());
			
			stopWatch.stop();
			System.out.println("ELAPSED TIME (View Leave Request): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;
		} catch (NumberFormatException nfe) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		}catch (IllegalStateException ise) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.ILLEGAL_OPERATION);
			} catch (IOException ignore) {}
		} catch (AlohaServerException ase) {
			try {
				ase.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}
		} catch ( Throwable t) {
			t.printStackTrace();
			try {
				t.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			} catch (IOException ignore) {}		
		}
	}

	private void checkAccessRights() throws AuthorizationException {
		
		if ( (!this.leaveDetail.isEmployee(this.userMB.getUserId()))
				&& (!this.leaveDetail.isSubmitter(this.userMB.getUserId()))
				&& (!this.leaveDetail.isApprover(this.userMB.getUserId()))
			) {
				Object[] params = { userMB.getFullName(), Long.valueOf(this.leaveDetail.getLeaveHeaderId()) };
				String errMsg = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_VIEW_UNAUTHORIZED, params);
				FacesContextUtil.getHttpSession().setAttribute("unauthMsg", errMsg);
				throw new AuthorizationException(errMsg);
		}
	}

	public LeaveDetail getLeaveDetail() {
		return leaveDetail;
	}

	public List<KeyValuePair> getLeaveBalances() {
		return leaveBalances;
	}
	public int getLeaveBalanceCount() {
		return (this.leaveBalances != null) ? (this.leaveBalances.size()) : (0);
	}
	
	public boolean isLeaveRequestActionable() {
		return	( this.isLeaveRequestCancelable()
					|| this.isChangeOfSupervisorAllowedForThisLeaveRequest()
					|| this.isLeaveRequestApproveable()
					|| this.isLeaveRequestAmendable()
					|| this.isLeaveRequestWithdrawable()
				);
	}		
	
	public boolean isLeaveRequestCancelable() {
		return	( this.leaveDetail != null
					&& this.leaveDetail.isCancelableByThisUser(this.currentUser)
					&& this.isInCancelMode()				
				);
	}	

	public boolean isChangeOfSupervisorAllowedForThisLeaveRequest() {
		return 	( this.leaveDetail != null
					&& this.leaveDetail.isChangeOfSupervisorAllowedByThisUser(this.currentUser)
					&& this.isInChangeSupervisorMode()
				);
	}
	
	public boolean isLeaveRequestApproveable() {
		return 	( this.leaveDetail != null
					&& this.leaveDetail.isApprovableByThisUser(this.currentUser)
					&& this.isInApproverMode()
				);
	}	

	public boolean isLeaveRequestAmendable() {
		return 	( this.leaveDetail != null
					&& this.leaveDetail.isAmendableByThisUser(this.currentUser)
					&& this.isInAmendMode()
				);
	}
	
	public boolean isLeaveRequestWithdrawable() {
		return 	( this.leaveDetail != null
					&& this.leaveDetail.isWithdrawableByThisUser(this.currentUser)
					&& this.isInWithdrawMode()
				);
	}
}