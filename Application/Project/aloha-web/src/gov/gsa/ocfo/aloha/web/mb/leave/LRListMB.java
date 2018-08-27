package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveStatus;
import gov.gsa.ocfo.aloha.model.leave.LeaveDetailRow;
import gov.gsa.ocfo.aloha.model.leave.LeaveHeaderRow;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.util.StringUtil;
import gov.gsa.ocfo.aloha.web.enums.LRMode;
import gov.gsa.ocfo.aloha.web.util.AlohaConstants;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
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

import com.icesoft.faces.facelets.component.table.autosort.AutoSortTableBean;

@ManagedBean(name="lrListMB")
@ViewScoped
public class LRListMB extends LRReadOnlyMB {
	private static final long serialVersionUID = 6643843623631608451L;
	
	private AutoSortTableBean tableBean;
    private boolean showDataPager = true;
    private boolean showRowControls = true;

	@EJB
	private LeaveRequestEJB leaveRequestEJB;

	private List<LeaveDetailRow> leaveDetails = new ArrayList<LeaveDetailRow>();
	
	@PreDestroy
	public void cleanup() {
		this.tableBean = null;
		this.leaveDetails = null;
		this.lrMode = null;
		this.showDataPager = false;
		this.showRowControls = false;
	}
	
	@PostConstruct
	public void initList() {
		try {
			StopWatch stopWatch = new StopWatch();
			
			this.leaveDetails = new ArrayList<LeaveDetailRow>();
			
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
			
			switch(this.lrMode) {
				case SUBMIT_OWN:
					stopWatch.start();
					this.leaveDetails = convert(this.leaveRequestEJB.getSubmitOwnList(this.userMB.getUser()));	
					stopWatch.stop();
					System.out.println("ELAPSED TIME (Leave Request List - My Requests): " + stopWatch.getElapsedTime() + " ms");
					break;
				case APPROVER:
					stopWatch.start();
					List<LeaveDetailRow> detailWorkList = this.convert(this.leaveRequestEJB.getApproverList(this.userMB.getUser()));
					for ( String codeValue : LeaveStatus.ApproverCodeSortOrder) {
						//System.out.println(codeValue);
						for ( LeaveDetailRow detailRow: detailWorkList) {
							if ( detailRow.getStatusCode().equals(codeValue)) {
								this.leaveDetails.add(detailRow);
							}
						}
					}
					stopWatch.stop();
					System.out.println("ELAPSED TIME (Leave Request List - Management): " + stopWatch.getElapsedTime() + " ms");
					break;					
				case ON_BEHALF_OF:
					stopWatch.start();
					this.leaveDetails = convert(this.leaveRequestEJB.getOnBehalfOfList(this.userMB.getUser()));
					stopWatch.stop();
					System.out.println("ELAPSED TIME (Leave Request List - My Team): " + stopWatch.getElapsedTime() + " ms");					
					break;
				default:
					System.out.println("LRMode value of  \"" + this.lrMode.getText() + "\" in not allowed in " 
							+ this.getClass().getName() + ". Cannot continue.");
					throw new IllegalStateException();
			}
			this.tableBean = new AutoSortTableBean(this.leaveDetails, "requestId");
			
			stopWatch = null;
			
		}  catch (IllegalStateException ise) {
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
//	private void debug() {
//		for ( LeaveDetailRow ldr : this.leaveDetails) {
//			System.out.println(ldr.getStatusCode() + " / " + ldr.getStatusLabel());
//		}
//	}
	private List<LeaveDetailRow> convert(List<LeaveHeaderRow> headerRows) {
		List<LeaveDetailRow> leaveDetailRows = new ArrayList<LeaveDetailRow>();
		for ( LeaveHeaderRow headerRow : headerRows) {
			leaveDetailRows.add(headerRow.getViewableLeaveDetail());
		}
		return leaveDetailRows;
	}

	public List<LeaveDetailRow> getLeaveDetails() {
		return leaveDetails;
	}
	public AutoSortTableBean getTableBean() {
		return tableBean;
	}
	public boolean isShowDataPager() {
		return showDataPager;
	}
	public boolean isShowRowControls() {
		return showRowControls;
	}
	public void setShowDataPager(boolean showDataPager) {
		this.showDataPager = showDataPager;
	}
	public void setShowRowControls(boolean showRowControls) {
		this.showRowControls = showRowControls;
	}	
}