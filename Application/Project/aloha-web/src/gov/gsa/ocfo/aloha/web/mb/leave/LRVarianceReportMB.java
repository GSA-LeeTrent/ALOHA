package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.LRVarianceReportEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.LeaveRequestReconciliation;
import gov.gsa.ocfo.aloha.model.LeaveRequestVariance;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.mb.FacilityCoordinatorMB;
import gov.gsa.ocfo.aloha.web.mb.PayPeriodMB;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@ManagedBean(name="lrVarianceReportMB")
@ViewScoped
public class LRVarianceReportMB implements Serializable {

	private static final long serialVersionUID = 2287831755492527926L;

	@EJB
	private LRVarianceReportEJB lrVarianceReportEJB;
		
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;	
	
	@ManagedProperty(value="#{payPeriodMB}")
	private PayPeriodMB payPeriodMB;	
	
	@ManagedProperty(value="#{facilityCoordinatorMB}")
	private FacilityCoordinatorMB facilityCoordinatorMB;
	public void setFacilityCoordinatorMB(FacilityCoordinatorMB facilityCoordinatorMB) {
		this.facilityCoordinatorMB = facilityCoordinatorMB;
	}
	private String selectedFromPayPeriod;
	private String selectedToPayPeriod;
	
	private SelectItem[] facilities;
	private String[] selectedFacilities;
	
	private List<LeaveRequestVariance> rptLR;
	private List<LeaveRequestVariance> rptLRFiltered;
	
	private boolean noDataFound = false; //displays no data found msg if rpt selection parms result in empty report 
	
	private boolean sortLastNameAscending = true; 
	private boolean sortDateAscending = true;
	private boolean sortLeaveTypeAscending; 
	private boolean sortFATCodeAscending;
	private boolean sortOrgLocCodeAscending;
	private boolean sortLRIdAscending;
	private String sortBy = null;

	@PostConstruct
	public void init() throws IOException {
		try {
			this.doSecurityCheck();
			this.loadFacilities();
			this.noDataFound = false;
			this.initPayPeriodSelections();
		} catch (AuthorizationException ae) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.UNAUTHORIZED);
			} catch( IOException ioe) {
				ioe.printStackTrace();
				FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
			}		
		} catch( Throwable t) {
			t.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		}
	}		
	private void initPayPeriodSelections() {
		PayPeriod currentPP = this.payPeriodMB.getLatestClosedPayPeriod();
		if ( currentPP != null) {
			this.selectedFromPayPeriod = currentPP.getValue();
			this.selectedToPayPeriod = currentPP.getValue();
		}	
	}
	
	/***************************************************************************************
	* NO LONGER NEEDED - Lee Trent (2015.02.26)	
	****************************************************************************************
	private void validateDateRange(Date beginDate, Date endDate) throws ValidatorException {
	
		GregorianCalendar beginCal = new GregorianCalendar();
		GregorianCalendar endCal =  new GregorianCalendar();
		
		beginCal.setTime(beginDate);
		endCal.setTime(endDate);
		
		beginCal.add(Calendar.WEEK_OF_YEAR, 52);
		
		if ( beginCal.before(endCal) ) {
		
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REPORT_VARIANCE_INVALID_DATE_RANGE);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				throw new ValidatorException(new FacesMessage());
		}
	}
	*****************************************************************************************/
	
	//called on click Run Report button
	public String onSelectFilters() {
		try {
			//clear previous data
			this.clearOldData();
			
			doValidation();
			
			String[] facilitiesIdsForRunReport = this.getFacilitiesIdsForRunReport();
			PayPeriod fromPP  = payPeriodMB.getPayPeriodForStartDate(selectedFromPayPeriod);
			PayPeriod toPP  = payPeriodMB.getPayPeriodForStartDate(selectedToPayPeriod);
			
			/**************************************************************
			* NO LONGER NEEDED - Lee Trent (2015.02.26)
			* this.validateDateRange(fromPP.getToDate(), toPP.getToDate());
			**************************************************************/
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			
			if ( facilitiesIdsForRunReport.length <= 1000 ) {
				this.rptLR = this.lrVarianceReportEJB.runReport(this.userMB.getUser(), facilitiesIdsForRunReport, 
						fromPP.getFromDate(), toPP.getToDate());
			} else {
				int size = facilitiesIdsForRunReport.length;
				int remaining = size;
				int max = 1000;
				int start = 0;
				int end = max;

				this.rptLR = new ArrayList<LeaveRequestVariance>();

				//System.out.println("facilitiesIdsForRunReport: " + facilitiesIdsForRunReport.length);
				
				while ( remaining  > 0  ) {

					//System.out.println("remaining: " + remaining + " / start: " + start + " / end: " + end);

					String[] subset = Arrays.copyOfRange(facilitiesIdsForRunReport, start, end);
					
					//System.out.println("subset: " + subset.length);

					this.rptLR.addAll(this.lrVarianceReportEJB.runReport(this.userMB.getUser(), subset, 
							fromPP.getFromDate(), toPP.getToDate()));
					
					start += max;
					remaining = size - end;
					
					if ( remaining > max ) {
						end   += max;
					} else if (remaining < max) {
						end += remaining;
					} else if (remaining == max) {
						end += max;
					}
				}
			}

			stopWatch.stop();
			System.out.println("ELAPSED TIME (Run Leave Request Recon Report): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;
			
			//initial default display -- show variances only
			//above call retrieves all records which are then filtered to show variances only
			rptLRFiltered = new ArrayList<LeaveRequestVariance>();
			if (rptLR == null || rptLR.size() <= 0) {
				this.noDataFound = true;
			} else {
				for (LeaveRequestVariance lrVariance : rptLR) {
					if (lrVariance.getLeaveHrsAloha() != lrVariance.getLeaveHrsEtams()) {
						rptLRFiltered.add(lrVariance);
					}
				}
				if (rptLRFiltered == null || rptLRFiltered.size() <= 0) {
					this.noDataFound = true;
				} else {
					this.sortLastNameAscending = true;
					this.sortByLastName();
				}
			}
			
			return null;
		} catch (ValidatorException ve) {
			return null;			
		} catch (AlohaServerException se) {
			return NavigationOutcomes.SERVER_ERROR;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}	
	

	public String refreshDisplay() {
		this.resetSortFields();
		rptLRFiltered = new ArrayList<LeaveRequestVariance>();
		for (LeaveRequestVariance lrVariance : rptLR) {
			if (lrVariance.getLeaveHrsAloha() != lrVariance.getLeaveHrsEtams()) {
				rptLRFiltered.add(lrVariance);
			}
		}

		if (rptLRFiltered == null || rptLRFiltered.size() <= 0) {
			this.noDataFound = true;
		} else {
			this.noDataFound = false;
		}
		return null;
	}
	
	private void loadFacilities() throws AlohaServerException {
		this.facilities = this.facilityCoordinatorMB.getFacilities();
	}
	
	//validates that user has selected a team and start, end pay periods
	//called after Run Report button click
	private void doValidation() throws ValidatorException {
		int errorCount = 0;
	
		if ( Integer.parseInt(this.selectedFromPayPeriod) < 1 || 
				Integer.parseInt(this.selectedToPayPeriod) < 1) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PAY_PERIOD_REQUIRED);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}
		if ( Integer.parseInt(this.selectedFromPayPeriod) > Integer.parseInt(this.selectedToPayPeriod) ) {
			String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PPFROM_LE_PPTO);
			FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
			facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage(null, facesMsg);
			errorCount++;
		}
		if ( errorCount > 0) {
			throw new ValidatorException(new FacesMessage());
		}		
	}
	
	//downloads currently displayed table to excel
	public void exportRpt2() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		CreationHelper createHelper = wb.getCreationHelper();
		CellStyle csDt = wb.createCellStyle();
		csDt.setDataFormat(
		        createHelper.createDataFormat().getFormat("mm/dd/yyyy"));
		
		CellStyle csLeaveHr = wb.createCellStyle();
		csLeaveHr.setDataFormat(createHelper.createDataFormat().getFormat("0.0"));
		
		short r = 0; short c = 0;  
		Cell cell; Row row;
		for (LeaveRequestVariance lrr : this.rptLRFiltered) {
			
			if (r == 0) {
				//Create headings
				row = sheet.createRow(r);
				c = 0;
			    cell = row.createCell(c);
			    cell.setCellValue("Last Name");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("First Name");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("M");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Org Loc");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("F/A/T");
			    ++c;					    
			    cell = row.createCell(c);
			    cell.setCellValue("Year");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Pay Period");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Leave Dt");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Leave Type");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("ALOHA Leave Hrs");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("ETAMS Leave Hrs");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("LR Id");
			    ++r;
			}
			row = sheet.createRow(r);
		    // Create cells
			c = 0;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getEmpLName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getEmpFName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getEmpMName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getOrgLocCode());
		    ++c;		    
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getFATCode());
		    ++c;			    
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getYear());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getPayPeriod());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getLeaveDt());
		    cell.setCellStyle(csDt);
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getLeaveType());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getLeaveHrsAloha()); 
		    cell.setCellStyle(csLeaveHr); //round to two decimals as XL is showing 9 decimals for some values
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getLeaveHrsEtams()); 
		    cell.setCellStyle(csLeaveHr); //round to two decimals as XL is showing 9 decimals for some values
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(lrr.getLrHeaderId());
		    ++r;
		}
		//adjust column width to fit the content
		for (short i = 0; i < 12; i++) {
			sheet.autoSizeColumn(i);
		}
	    sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J1"));

        //Set the filename
        //filename = fc.getExternalContext().getUserPrincipal().toString() + "-"+ filename; 
		String filename = "LeaveRequestReport_" + selectedFromPayPeriod + "_" + selectedToPayPeriod + ".xlsx";
		//String contentType = "application/vnd.ms-excel"; // excel 2003 and older
		
		//Download as xlsx
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; //xlsx format
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)fc.getExternalContext().getResponse();
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        response.setContentType(contentType);
        ServletOutputStream out = response.getOutputStream();
        
        wb.write(out);
        
        out.close();
        fc.responseComplete();
        
	}
	
	private void doSecurityCheck() throws AuthorizationException {
		//all roles -- submitown, approvers and submitothers can run reconciliation
		if ( !this.userMB.isSubmitOwn() && !this.userMB.isApprover() && !this.userMB.isOnBehalfOf()) {
			throw new AuthorizationException(ErrorMessages.getInstance().getMessage(ErrorMessages.LR_REPORT_NOTAUTH));
		}
	}
	
	private void clearOldData () {
		this.rptLR = null;
		this.noDataFound = false;
		this.resetSortFields();
	}

	private void resetSortFields () {
		//called from clearold data and when showvariances is toggled
		this.sortBy = null;
		this.sortLastNameAscending = true;
		this.sortDateAscending = true;
		this.sortLeaveTypeAscending = false;
		this.sortFATCodeAscending = false;
		this.sortLRIdAscending = false;
		this.sortOrgLocCodeAscending = false;
	}


	
	private String[] getFacilitiesIdsForRunReport() {

		if (this.selectedFacilities.length > 0) {
			return this.selectedFacilities;
		} else {
			int index = 0;
			String[] allFacilities = new String[this.facilities.length];
			for (SelectItem si : this.facilities) {
				allFacilities[index++] = si.getValue().toString();
			}
			return allFacilities;
		}
	}	
	
	//------------------------------------------------
	//Sorting Methods
	//------------------------------------------------		
	public String sortByLastName() {
		try {
			this.setSortBy("LastName");
			if (sortLastNameAscending) {
				//sort ascending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr1.getEmpLName().compareTo(lrr2.getEmpLName());
					}	
				});	
				sortLastNameAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr2.getEmpLName().compareTo(lrr1.getEmpLName());
					}
				});	
				sortLastNameAscending = true;			
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}
	
	public String sortByDate() {
		try {
			this.setSortBy("Date");
			if (sortDateAscending) {
				//sort ascending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr1.getLeaveDt().compareTo(lrr2.getLeaveDt());
					}	
				});	
				sortDateAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr2.getLeaveDt().compareTo(lrr1.getLeaveDt());
					}
				});	
				sortDateAscending = true;			
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}

	public String sortByLeaveType() {
		try {
			this.setSortBy("LeaveType");
			if (sortLeaveTypeAscending) {
				//sort ascending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr1.getLeaveType().compareTo(lrr2.getLeaveType());
					}	
				});	
				sortLeaveTypeAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr2.getLeaveType().compareTo(lrr1.getLeaveType());
					}
				});	
				sortLeaveTypeAscending = true;			
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}

	public String sortByFATCode() {
		try {
			this.setSortBy("FATCode");
			if (sortFATCodeAscending) {
				//sort ascending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr1.getFATCode().compareTo(lrr2.getFATCode());
					}	
				});	
				sortFATCodeAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr2.getFATCode().compareTo(lrr1.getFATCode());
					}
				});	
				sortFATCodeAscending = true;			
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}		

	public String sortByLRId() {
		try {
			this.setSortBy("LRId");
			if (sortLRIdAscending) {
				//sort ascending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr1.getLrHeaderId().compareTo(lrr2.getLrHeaderId());
					}	
				});	
				sortLRIdAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestReconciliation>() {
					@Override
					public int compare(LeaveRequestReconciliation lrr1, LeaveRequestReconciliation lrr2) {
						return lrr2.getLrHeaderId().compareTo(lrr1.getLrHeaderId());
					}
				});	
				sortLRIdAscending = true;			
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}
	
	public String sortByOrgLocCode() {
		try {
			this.setSortBy("OrgLocCode");
			if (this.sortOrgLocCodeAscending)  {
				//sort ascending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestVariance>() {
					@Override
					public int compare(LeaveRequestVariance lrv1, LeaveRequestVariance lrv2) {
						return ( lrv1.getOrgLocCode().compareTo(lrv2.getOrgLocCode()));
					}	
				});	
				this.sortOrgLocCodeAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptLRFiltered, new Comparator<LeaveRequestVariance>() {
					@Override
					public int compare(LeaveRequestVariance lrv1, LeaveRequestVariance lrv2) {
						return lrv2.getOrgLocCode().compareTo(lrv1.getOrgLocCode());
					}
				});	
				this.sortOrgLocCodeAscending = true;			
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return NavigationOutcomes.SERVER_ERROR;
		}
	}
	//------------------------------------------------
	//GETTERS and SETTERS
	//------------------------------------------------	

	public String getSelectedFromPayPeriod() {
		return selectedFromPayPeriod;
	}
	public void setSelectedFromPayPeriod(String selectedFromPayPeriod) {
		this.selectedFromPayPeriod = selectedFromPayPeriod;
	}
	public String getSelectedToPayPeriod() {
		return selectedToPayPeriod;
	}
	public void setSelectedToPayPeriod(String selectedToPayPeriod) {
		this.selectedToPayPeriod = selectedToPayPeriod;
	}

	public void setRptLRn(List<LeaveRequestVariance> rptLR) {
		this.rptLR = rptLR;
	}

	public List<LeaveRequestVariance> getRptLR() {
		
		return rptLR;
	}

	public UserMB getUserMB() {
		return userMB;
	}

	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}

	public PayPeriodMB getPayPeriodMB() {
		return payPeriodMB;
	}
	

	public void setPayPeriodMB(PayPeriodMB payPeriodMB) {
		this.payPeriodMB = payPeriodMB;
	}

	public void setSortLastNameAscending(boolean sortLastNameAscending) {
		this.sortLastNameAscending = sortLastNameAscending;
	}

	public boolean isSortLastNameAscending() {
		return sortLastNameAscending;
	}

	public void setSortDateAscending(boolean sortDateAscending) {
		this.sortDateAscending = sortDateAscending;
	}

	public boolean isSortDateAscending() {
		return sortDateAscending;
	}

	public void setSortLeaveTypeAscending(boolean sortLeaveTypeAscending) {
		this.sortLeaveTypeAscending = sortLeaveTypeAscending;
	}

	public boolean isSortLeaveTypeAscending() {
		return sortLeaveTypeAscending;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setNoDataFound(boolean noDataFound) {
		this.noDataFound = noDataFound;
	}

	public boolean isNoDataFound() {
		return noDataFound;
	}

	public SelectItem[] getFacilities() {
		return facilities;
	}
	
	public String[] getSelectedFacilities() {
		return selectedFacilities;
	}
	public void setSelectedFacilities(String[] selectedFacilities) {
		this.selectedFacilities = selectedFacilities;
	}

	public void setRptLRFiltered(List<LeaveRequestVariance> rptLRFiltered) {
		this.rptLRFiltered = rptLRFiltered;
	}

	public List<LeaveRequestVariance> getRptLRFiltered() {
		return rptLRFiltered;
	}

	public boolean isSortFATCodeAscending() {
		return sortFATCodeAscending;
	}

	public void setSortFATCodeAscending(boolean sortFATCodeAscending) {
		this.sortFATCodeAscending = sortFATCodeAscending;
	}
	public boolean isSortOrgLocCodeAscending() {
		return sortOrgLocCodeAscending;
	}
	public void setSortOrgLocCodeAscending(boolean sortOrgLocCodeAscending) {
		this.sortOrgLocCodeAscending = sortOrgLocCodeAscending;
	}
	public void setSortLRIdAscending(boolean sortLRIdAscending) {
		this.sortLRIdAscending = sortLRIdAscending;
	}
	public boolean isSortLRIdAscending() {
		return sortLRIdAscending;
	}
}