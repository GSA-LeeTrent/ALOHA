package gov.gsa.ocfo.aloha.web.mb.leave;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveRequestReconEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveStatusEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LeaveTypeEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.model.LeaveRequestReconciliation;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveType;
import gov.gsa.ocfo.aloha.util.StopWatch;
import gov.gsa.ocfo.aloha.web.mb.PayPeriodMB;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.security.NavigationOutcomes;
import gov.gsa.ocfo.aloha.web.util.AlohaURIs;
import gov.gsa.ocfo.aloha.web.util.ErrorMessages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

@ManagedBean(name="reconMB")
@ViewScoped
public class ReconMB implements Serializable {

	private static final long serialVersionUID = 3770301470070099040L;
	
	@EJB private LeaveRequestReconEJB leaveRequestReconEJB;
	
	@EJB private LeaveTypeEJB leaveTypeEJB;
	
	@EJB private LeaveStatusEJB leaveStatusEJB;
	
	@EJB private EmployeeEJB employeeEJB;
	
	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;	
	
	@ManagedProperty(value="#{payPeriodMB}")
	private PayPeriodMB payPeriodMB;	

	private String selectedFromPayPeriod;
	private String selectedToPayPeriod;
	
	private SelectItem[] priLeaveTypes;
	private String[] selectedPriLeaveTypes;

	private SelectItem[] secLeaveTypes;
	private String[] selectedSecLeaveTypes;
	
	private SelectItem[] employees;
	private String[] selectedEmployees;
	
	private List<LeaveRequestReconciliation> rptLR;
	private List<LeaveRequestReconciliation> rptLRFiltered;
	private boolean showVariancesOnly = true;
	
	private boolean noDataFound = false; //displays no data found msg if rpt selection parms result in empty report 
	
	private boolean sortLastNameAscending = true; 
	private boolean sortDateAscending = true;
	private boolean sortLeaveTypeAscending; 
	private boolean sortFATCodeAscending;
	private boolean sortLRIdAscending;
	private String sortBy = null;

	@PostConstruct
	public void init() throws IOException {
		try {
			this.doSecurityCheck();
			this.loadEmployees();
			this.loadLeaveTypes();
			this.noDataFound = false;
			this.initPayPeriodSelections();
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
			t.printStackTrace();
			FacesContext.getCurrentInstance().getExternalContext().redirect(AlohaURIs.SERVER_ERROR);
		}
	}		
	private void initPayPeriodSelections() {
		PayPeriod currentPP = this.payPeriodMB.findCurrentPayPeriod();
		if ( currentPP != null) {
			this.selectedFromPayPeriod = currentPP.getValue();
			this.selectedToPayPeriod = currentPP.getValue();
		}	
	}	
	//called on click Run Report button
	public String onSelectFilters() {
		try{
			//clear previous data
			this.clearOldData();
			
			doValidation();
			PayPeriod fromPP  = payPeriodMB.getPayPeriodForStartDate(selectedFromPayPeriod);
			PayPeriod toPP  = payPeriodMB.getPayPeriodForStartDate(selectedToPayPeriod);
			
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			this.rptLR = this.leaveRequestReconEJB.runRpt(this.userMB.getUser(), this.getPriLeaveTypesSelectedByUser(),
					this.getSecLeaveTypesSelectedByUser(),this.getEmployeesSelectedByUser(), 
					fromPP, toPP);
			stopWatch.stop();
			System.out.println("ELAPSED TIME (Run Leave Request Recon Report): " + stopWatch.getElapsedTime() + " ms");
			stopWatch = null;
			
			//initial default display -- show variances only
			//above call retrieves all records which are then filtered to show variances only
			this.showVariancesOnly = true;
			rptLRFiltered = new ArrayList<LeaveRequestReconciliation>();
			if (rptLR == null || rptLR.size() <= 0) {
				this.noDataFound = true;
			} else {
				for (LeaveRequestReconciliation lrr : rptLR) {
					if (lrr.getLeaveHrsAloha() != lrr.getLeaveHrsEtams()) {
						rptLRFiltered.add(lrr);
					}
				}
				if (rptLRFiltered == null || rptLRFiltered.size() <= 0) {
					this.noDataFound = true;
				}
			}
			
			return null;
//		} catch (AuthorizationException ae) {
//			return NavigationOutcomes.UNAUTHORIZED_PAGE;
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
		rptLRFiltered = new ArrayList<LeaveRequestReconciliation>();
		if (this.isShowVariancesOnly()) {
			for (LeaveRequestReconciliation lrr : rptLR) {
				if (lrr.getLeaveHrsAloha() != lrr.getLeaveHrsEtams()) {
					rptLRFiltered.add(lrr);
				}
			}
		}else {
			rptLRFiltered = rptLR;	
		}
		if (rptLRFiltered == null || rptLRFiltered.size() <= 0) {
			this.noDataFound = true;
		} else {
			this.noDataFound = false;
		}
		return null;
	}
	//called on initial page load to get list of leave types
	private void loadLeaveTypes() throws AlohaServerException {
		List<LeaveType> lts = this.leaveTypeEJB.getAllLeaveTypes();
		//restricting filter to primary leave codes only
		Map<String, String> mapPriLTS = new LinkedHashMap<String, String>();
		Map<String, String> mapSecLTS = new LinkedHashMap<String, String>();
		mapPriLTS.put("0", "Select None");
		mapSecLTS.put("0", "Select None");
		for (LeaveType lt : lts) {
			if (!mapPriLTS.containsKey(lt.getPrimaryCode())) {
				mapPriLTS.put(lt.getPrimaryCode(), lt.getPrimaryCode() + "-" + lt.getPrimaryCodeDesc());
			}
			if (lt.getSecondaryCode() != null) {
				if (!mapSecLTS.containsKey(lt.getSecondaryCode())) {
					mapSecLTS.put(lt.getSecondaryCode(), lt.getSecondaryCode() + "-" + lt.getSecondaryCodeDesc());
				}				
			}
		}
		
		priLeaveTypes = new SelectItem[mapPriLTS.size()];
		int index = 0;
		for (Map.Entry<String, String> entry : mapPriLTS.entrySet()) {
			priLeaveTypes[index++] = new SelectItem(entry.getKey(), entry.getValue());
		}
		index = 0;
		secLeaveTypes = new SelectItem[mapSecLTS.size()];
		for (Map.Entry<String, String> entry : mapSecLTS.entrySet()) {
			secLeaveTypes[index++] = new SelectItem(entry.getKey(), entry.getValue());
		}
	}

	//called on initial page load to get list of employees
	private void loadEmployees() throws AlohaServerException {
		List<AlohaUser> aus = this.employeeEJB.getManagedStaffForUser(userMB.getUserId());
		
		//add current logged-in user; 
		//jsf barfs if selectitem array is not sized exactly so in order to add current user, 
		//first load map, then load into selectitem array
		
		Map<String, String> mapAU = new LinkedHashMap<String, String>();
		mapAU.put(userMB.getUserId().toString(), userMB.getFullName());
		for (AlohaUser au : aus) {
			if (!mapAU.containsKey(String.valueOf(au.getUserId()))) {
				mapAU.put(String.valueOf(au.getUserId()), au.getFullName());
			}
		}
		
		this.employees = new SelectItem[mapAU.size()];
		int index = 0;
		
		for (Map.Entry<String, String> entry : mapAU.entrySet()) {
			this.employees[index++] = new SelectItem(entry.getKey(), entry.getValue());
		}
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
		for (LeaveRequestReconciliation lrr : this.rptLRFiltered) {
			
			if (r == 0) {
				//Create headings
				row = sheet.createRow(r);
				c = 0;
//			    cell = row.createCell(c);
//			    cell.setCellValue("Facility");
//			    ++c;
//			    cell = row.createCell(c);
//			    cell.setCellValue("Area");
//			    ++c;
//			    cell = row.createCell(c);
//			    cell.setCellValue("Team");
//			    ++c;
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
//		    cell = row.createCell(c);
//		    cell.setCellValue(lrr.getFacilityCd());
//		    ++c;
//		    cell = row.createCell(c);
//		    cell.setCellValue(lrr.getAreaCd());
//		    ++c;
//		    cell = row.createCell(c);
//		    cell.setCellValue(lrr.getTeamCd());
//		    ++c;
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
		    //SAK 20120123 Changed rpt to use LRH instead of LRD
		    //cell.setCellValue(lrr.getLrDetailId());
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
	}
	private String[] getPriLeaveTypesSelectedByUser() {
		//if user has selected leave types, returns selectedLeaveTypes
		//else returns all leave types
		if (this.selectedPriLeaveTypes.length > 0) {
			return this.selectedPriLeaveTypes;
		} else {
			int index = 0;
			String[] allPriLeaveTypes = new String[this.priLeaveTypes.length];
			for (SelectItem si : this.priLeaveTypes) {
				allPriLeaveTypes[index++] = si.getValue().toString();
			}
			return allPriLeaveTypes;
		}
	}
	private String[] getSecLeaveTypesSelectedByUser() {
		//if user has selected leave types, returns selectedSecLeaveTypes
		//else returns all Secondary leave types
		if (this.selectedSecLeaveTypes.length > 0) {
			return this.selectedSecLeaveTypes;
		} else {
			int index = 0;
			String[] allSecLeaveTypes = new String[this.secLeaveTypes.length];
			for (SelectItem si : this.secLeaveTypes) {
				allSecLeaveTypes[index++] = si.getValue().toString();
			}
			return allSecLeaveTypes;
		}
	}
	
	private String[] getEmployeesSelectedByUser() {
		//if user has selected employees, returns selectedEmployees
		//else returns all employees
		if (this.selectedEmployees.length > 0) {
			return this.selectedEmployees;
		} else {
			int index = 0;
			String[] allEmployees = new String[this.employees.length];
			for (SelectItem si : this.employees) {
				allEmployees[index++] = si.getValue().toString();
			}
			return allEmployees;
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

	public void setRptLRn(List<LeaveRequestReconciliation> rptLR) {
		this.rptLR = rptLR;
	}

	public List<LeaveRequestReconciliation> getRptLR() {
		
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

	public void setLeaveTypeEJB(LeaveTypeEJB leaveTypeEJB) {
		this.leaveTypeEJB = leaveTypeEJB;
	}

	public LeaveTypeEJB getLeaveTypeEJB() {
		return leaveTypeEJB;
	}

	public void setLeaveStatusEJB(LeaveStatusEJB leaveStatusEJB) {
		this.leaveStatusEJB = leaveStatusEJB;
	}

	public LeaveStatusEJB getLeaveStatusEJB() {
		return leaveStatusEJB;
	}

	public LeaveRequestReconEJB getLeaveRequestReconEJB() {
		return leaveRequestReconEJB;
	}

	public void setLeaveRequestReconEJB(LeaveRequestReconEJB leaveRequestReconEJB) {
		this.leaveRequestReconEJB = leaveRequestReconEJB;
	}

	public EmployeeEJB getEmployeeEJB() {
		return employeeEJB;
	}

	public void setEmployeeEJB(EmployeeEJB employeeEJB) {
		this.employeeEJB = employeeEJB;
	}

	public void setEmployees(SelectItem[] employees) {
		this.employees = employees;
	}

	public SelectItem[] getEmployees() {
		return employees;
	}

	public void setSelectedEmployees(String[] selectedEmployees) {
		this.selectedEmployees = selectedEmployees;
	}

	public String[] getSelectedEmployees() {
		return selectedEmployees;
	}

	public void setSecLeaveTypes(SelectItem[] secLeaveTypes) {
		this.secLeaveTypes = secLeaveTypes;
	}

	public SelectItem[] getSecLeaveTypes() {
		return secLeaveTypes;
	}

	public void setSelectedSecLeaveTypes(String[] selectedSecLeaveTypes) {
		this.selectedSecLeaveTypes = selectedSecLeaveTypes;
	}

	public String[] getSelectedSecLeaveTypes() {
		return selectedSecLeaveTypes;
	}

	public SelectItem[] getPriLeaveTypes() {
		return priLeaveTypes;
	}

	public void setPriLeaveTypes(SelectItem[] priLeaveTypes) {
		this.priLeaveTypes = priLeaveTypes;
	}

	public String[] getSelectedPriLeaveTypes() {
		return selectedPriLeaveTypes;
	}

	public void setSelectedPriLeaveTypes(String[] selectedPriLeaveTypes) {
		this.selectedPriLeaveTypes = selectedPriLeaveTypes;
	}

	public void setRptLRFiltered(List<LeaveRequestReconciliation> rptLRFiltered) {
		this.rptLRFiltered = rptLRFiltered;
	}

	public List<LeaveRequestReconciliation> getRptLRFiltered() {
		return rptLRFiltered;
	}

	public void setShowVariancesOnly(boolean showVariancesOnly) {
		this.showVariancesOnly = showVariancesOnly;
	}

	public boolean isShowVariancesOnly() {
		return showVariancesOnly;
	}

	public boolean isSortFATCodeAscending() {
		return sortFATCodeAscending;
	}

	public void setSortFATCodeAscending(boolean sortFATCodeAscending) {
		this.sortFATCodeAscending = sortFATCodeAscending;
	}
	public void setSortLRIdAscending(boolean sortLRIdAscending) {
		this.sortLRIdAscending = sortLRIdAscending;
	}
	public boolean isSortLRIdAscending() {
		return sortLRIdAscending;
	}
}
