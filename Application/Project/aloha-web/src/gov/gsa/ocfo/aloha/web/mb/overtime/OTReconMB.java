package gov.gsa.ocfo.aloha.web.mb.overtime;

import gov.gsa.ocfo.aloha.ejb.leave.EmployeeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeRequestReconEJB;
//import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeTypeEJB;
import gov.gsa.ocfo.aloha.ejb.overtime.OvertimeEJB;
import gov.gsa.ocfo.aloha.model.OvertimeRequestReconciliation;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTPayPeriod;
//import gov.gsa.ocfo.aloha.model.entity.overtime.OvertimeType;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.exception.AuthorizationException;
import gov.gsa.ocfo.aloha.web.mb.UserMB;
import gov.gsa.ocfo.aloha.web.mb.overtime.OTUtilMB;
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
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

@ManagedBean(name="otReconMB")
@ViewScoped
public class OTReconMB implements Serializable {

	private static final long serialVersionUID = 3770301470070099040L;

	@EJB private OvertimeRequestReconEJB overtimeRequestReconEJB;

	//@EJB private OvertimeTypeEJB leaveTypeEJB;

	@EJB private EmployeeEJB employeeEJB;

	@EJB
	protected OvertimeEJB overtimeEJB;

	@ManagedProperty(value="#{otUtilMB}")
	private OTUtilMB otUtilMB;

	@ManagedProperty(value="#{userMB}")
	private UserMB userMB;

	@ManagedProperty(value="#{otPayPeriodMB}")
	private OTPayPeriodMB otPayPeriodMB;

	private String selectedFromPayPeriod;
	private String selectedToPayPeriod;

	private String[] selectedOvertimeTypes;
	private SelectItem[] planGrade;

	private String[] selectedOtPlanGrade;
	private SelectItem[] employees;
	private String[] selectedEmployees;

	private List<OvertimeRequestReconciliation> rptOT;
	private List<OvertimeRequestReconciliation> rptOTFiltered;
	private boolean showVariancesOnly = true;

	private boolean noDataFound = false; //displays no data found msg if rpt selection parms result in empty report

	private boolean sortLastNameAscending = true;
	private boolean sortDateAscending = true;
	private boolean sortOvertimeTypeAscending;
	private boolean sortFATCodeAscending;
	private String sortBy = null;

	@PostConstruct
	public void init() throws IOException {
		try {
			this.doSecurityCheck();
			this.loadEmployees();
			this.otUtilMB.getOtTypes();
//			this.otUtilMB.getOTPayPeriod();
			this.loadPlanGrade();
			this.noDataFound = false;
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

	//called on click Run Report button
	public String onSelectFilters() {
		try{
			//clear previous data
			this.clearOldData();

			doValidation();
			OTPayPeriod fromPP  = otPayPeriodMB.getPayPeriodForStartDate(selectedFromPayPeriod);
			OTPayPeriod toPP  = otPayPeriodMB.getPayPeriodForStartDate(selectedToPayPeriod);

			this.rptOT = this.overtimeRequestReconEJB.runRpt(this.userMB.getUser(), this.getOvertimeTypesSelectedByUser(),
					this.getOtPlanGradeSelectedByUser(),
					this.getEmployeesSelectedByUser(), fromPP, toPP);
			//initial default display -- show variances only
			//above call retrieves all records which are then filtered to show variances only
			this.showVariancesOnly = true;
			rptOTFiltered = new ArrayList<OvertimeRequestReconciliation>();
			if (rptOT == null || rptOT.size() <= 0) {
				this.noDataFound = true;
			} else {
				for (OvertimeRequestReconciliation otr : rptOT) {
					if (otr.getOvertimeHrsAloha() != otr.getOvertimeHrsEtams()) {
						rptOTFiltered.add(otr);
					}
				}
				if (rptOTFiltered == null || rptOTFiltered.size() <= 0) {
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
		rptOTFiltered = new ArrayList<OvertimeRequestReconciliation>();
		if (this.isShowVariancesOnly()) {
			for (OvertimeRequestReconciliation otr : rptOT) {
				if (otr.getOvertimeHrsAloha() != otr.getOvertimeHrsEtams()) {
					rptOTFiltered.add(otr);
				}
			}
		}else {
			rptOTFiltered = rptOT;
		}
		if (rptOTFiltered == null || rptOTFiltered.size() <= 0) {
			this.noDataFound = true;
		} else {
			this.noDataFound = false;
		}
		return null;
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

	private void loadPlanGrade() throws AlohaServerException {
		List<AlohaUser> aus = this.employeeEJB.getManagedStaffForUser(userMB.getUserId());

		//add current logged-in user;
		//jsf barfs if selectitem array is not sized exactly so in order to add current user,
		//first load map, then load into selectitem array
		Map<String, String> mapPG = new TreeMap<String, String>();

		if ( aus.size() == 0){

			String sg = this.overtimeEJB.retrieveSalaryGradeKey(userMB.getUserId());
			mapPG.put(sg.substring(0,2) + sg.substring(6,8),sg.substring(0,2) + sg.substring(6,8));
		}

		for (AlohaUser au : aus) {

			String sg = this.overtimeEJB.retrieveSalaryGradeKey(au.getUserId());

			if (sg != null){
		   	   if (!mapPG.containsKey(sg.substring(0,2) + sg.substring(6,8))) {
			  	   mapPG.put(sg.substring(0,2) + sg.substring(6,8),sg.substring(0,2) + sg.substring(6,8));
		   	   }
	        }

		}

        String sg = this.overtimeEJB.retrieveSalaryGradeKey(userMB.getUserId());

		if (sg != null){
		   	   if (!mapPG.containsKey(sg.substring(0,2) + sg.substring(6,8))) {
			  	   mapPG.put(sg.substring(0,2) + sg.substring(6,8),sg.substring(0,2) + sg.substring(6,8));
		   	   }
	        }

		this.planGrade = new SelectItem[mapPG.size()];
		int index = 0;

		for (Map.Entry<String, String> entry : mapPG.entrySet()) {
			this.planGrade[index++] = new SelectItem(entry.getKey(), entry.getValue());
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
		} else {
			//selectedPP may not be 6 characters e.g. 20121 so pad it to 6 char
			String fromYYYY = this.selectedFromPayPeriod.substring(0, 4);
			String fromPP = "0" + this.selectedFromPayPeriod.substring(4);
			fromPP = fromPP.substring(fromPP.length()-2);
			String toYYYY = this.selectedToPayPeriod.substring(0, 4);
			String toPP = ("0" + this.selectedToPayPeriod.substring(4));
			toPP = toPP.substring(toPP.length()-2);
			//System.out.println("fromY:" + fromYYYY + " fromPP: " + fromPP + " toY: " + toYYYY + " toPP: " + toPP);

			if ( Integer.parseInt(fromYYYY+fromPP) > Integer.parseInt(toYYYY+toPP) ) {
				String errMsgText = ErrorMessages.getInstance().getMessage(ErrorMessages.PPFROM_LE_PPTO);
				FacesMessage facesMsg = new FacesMessage(errMsgText, errMsgText);
				facesMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				FacesContext.getCurrentInstance().addMessage(null, facesMsg);
				errorCount++;
			}
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

		CellStyle csOvertimeHr = wb.createCellStyle();
		csOvertimeHr.setDataFormat(createHelper.createDataFormat().getFormat("0.0"));

		short r = 0; short c = 0;
		Cell cell; Row row;
		for (OvertimeRequestReconciliation otr : rptOTFiltered) {

			if (r == 0) {
				//Create headings
				row = sheet.createRow(r);
				c = 0;
//				cell = row.createCell(c);
//			    cell.setCellValue("Overtime Request ID");
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
			    cell.setCellValue("Date");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Overtime Type");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Pay Plan/Grade");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("Approver");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("ALOHA Hours");
			    ++c;
			    cell = row.createCell(c);
			    cell.setCellValue("ETAMS Hours");
			    ++r;
			}
			row = sheet.createRow(r);
		    // Create cells
			c = 0;
//		    cell = row.createCell(c);
//		    cell.setCellValue(otr.getOtHeaderId());
//		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getEmpLName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getEmpFName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getEmpMName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getFATCode());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getYear());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getPayPeriod());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getOvertimeDt());
		    cell.setCellStyle(csDt);
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getOvertimeType());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getPlanGrade());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getAppFName() + ' '+ otr.getAppMName() + ' ' + otr.getAppLName());
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getOvertimeHrsAloha());
		    cell.setCellStyle(csOvertimeHr); //round to two decimals as XL is showing 9 decimals for some values
		    ++c;
		    cell = row.createCell(c);
		    cell.setCellValue(otr.getOvertimeHrsEtams());
		    cell.setCellStyle(csOvertimeHr); //round to two decimals as XL is showing 9 decimals for some values
		    ++r;
		}
		//adjust column width to fit the content
		for (short i = 0; i < 12; i++) {
			sheet.autoSizeColumn(i);
		}
	    sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J1"));

        //Set the filename
        //filename = fc.getExternalContext().getUserPrincipal().toString() + "-"+ filename;
		String filename = "OvertimeRequestReport_" + selectedFromPayPeriod + "_" + selectedToPayPeriod + ".xlsx";
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
			throw new AuthorizationException(ErrorMessages.getInstance().getMessage(ErrorMessages.OT_REPORT_NOTAUTH));
		}
	}

	private void clearOldData () {
		this.rptOT = null;
		this.sortBy = null;
		this.sortLastNameAscending = true;
		this.sortDateAscending = true;
		this.sortOvertimeTypeAscending = false;
		this.sortFATCodeAscending = false;
		this.noDataFound = false;
	}

	private String[] getOvertimeTypesSelectedByUser() {
		//if user has selected overtime types, returns selectedOvertimeTypes
		//else returns all overtime types
		if (this.selectedOvertimeTypes.length > 0) {
			return this.selectedOvertimeTypes;
		} else {
			int index = 0;
			String[] allOvertimeTypes = new String[this.otUtilMB.getOtTypes().length];
			for (SelectItem si : this.otUtilMB.getOtTypes()) {
				allOvertimeTypes[index++] = si.getValue().toString();
			}
			return allOvertimeTypes;
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

	private String[] getOtPlanGradeSelectedByUser() {
		//if user has selected overtime pay plan/grade, returns selectedOtPlanGrade
		//else returns all overtime pay plan/grade
		if (this.selectedOtPlanGrade.length > 0) {
			return this.selectedOtPlanGrade;
		} else {
			int index = 0;
			String[] allOtPlanGrade = new String[this.planGrade.length];
			for (SelectItem sp : this.planGrade) {
				allOtPlanGrade[index++] = sp.getValue().toString();
			}
			return allOtPlanGrade;
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
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr1.getEmpLName().compareTo(otr2.getEmpLName());
					}
				});
				sortLastNameAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr2.getEmpLName().compareTo(otr1.getEmpLName());
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
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr1.getOvertimeDt().compareTo(otr2.getOvertimeDt());
					}
				});
				sortDateAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr2.getOvertimeDt().compareTo(otr1.getOvertimeDt());
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

	public String sortByOvertimeType() {
		try {
			this.setSortBy("OvertimeType");
			if (sortOvertimeTypeAscending) {
				//sort ascending order
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr1.getOvertimeType().compareTo(otr2.getOvertimeType());
					}
				});
				sortOvertimeTypeAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr2.getOvertimeType().compareTo(otr1.getOvertimeType());
					}
				});
				sortOvertimeTypeAscending = true;
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
			if (this.sortFATCodeAscending) {
				//sort ascending order
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr1.getFATCode().compareTo(otr2.getFATCode());
					}
				});
				sortFATCodeAscending = false;
			} else {
				// sort descending order
				Collections.sort(rptOTFiltered, new Comparator<OvertimeRequestReconciliation>() {
					@Override
					public int compare(OvertimeRequestReconciliation otr1, OvertimeRequestReconciliation otr2) {
						return otr2.getFATCode().compareTo(otr1.getFATCode());
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

	public void setRptOTn(List<OvertimeRequestReconciliation> rptOT) {
		this.rptOT = rptOT;
	}

	public List<OvertimeRequestReconciliation> getRptOT() {

		return rptOT;
	}

	public UserMB getUserMB() {
		return userMB;
	}

	public void setUserMB(UserMB userMB) {
		this.userMB = userMB;
	}

	public OTUtilMB getOtUtilMB() {
		return otUtilMB;
	}

	public void setOtUtilMB(OTUtilMB otUtilMB) {
		this.otUtilMB = otUtilMB;
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

	public void setSortOvertimeTypeAscending(boolean sortOvertimeTypeAscending) {
		this.sortOvertimeTypeAscending = sortOvertimeTypeAscending;
	}

	public boolean isSortOvertimeTypeAscending() {
		return sortOvertimeTypeAscending;
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

	public OvertimeRequestReconEJB getOvertimeRequestReconEJB() {
		return overtimeRequestReconEJB;
	}

	public void setOvertimeRequestReconEJB(OvertimeRequestReconEJB overtimeRequestReconEJB) {
		this.overtimeRequestReconEJB = overtimeRequestReconEJB;
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

	public String[] getSelectedOtPlanGrade() {
		return selectedOtPlanGrade;
	}

	public void setSelectedOtPlanGrade(String[] selectedOtPlanGrade) {
		this.selectedOtPlanGrade = selectedOtPlanGrade;
	}

	public SelectItem[] getPlanGrade() {
		return planGrade;
	}

	public void setPlanGrade(SelectItem[] planGrade) {
		this.planGrade = planGrade;
	}

	public String[] getSelectedOvertimeTypes() {
		return selectedOvertimeTypes;
	}

	public void setSelectedOvertimeTypes(String[] selectedOvertimeTypes) {
		this.selectedOvertimeTypes = selectedOvertimeTypes;
	}

	public void setRptOTFiltered(List<OvertimeRequestReconciliation> rptOTFiltered) {
		this.rptOTFiltered = rptOTFiltered;
	}

	public List<OvertimeRequestReconciliation> getRptOTFiltered() {
		return rptOTFiltered;
	}

	public void setShowVariancesOnly(boolean showVariancesOnly) {
		this.showVariancesOnly = showVariancesOnly;
	}

	public boolean isShowVariancesOnly() {
		return showVariancesOnly;
	}

	public void setSortFATCodeAscending(boolean sortFATCodeAscending) {
		this.sortFATCodeAscending = sortFATCodeAscending;
	}

	public boolean isSortFATCodeAscending() {
		return sortFATCodeAscending;
	}

	public void setOtPayPeriodMB(OTPayPeriodMB otPayPeriodMB) {
		this.otPayPeriodMB = otPayPeriodMB;
	}

	public OTPayPeriodMB getOtPayPeriodMB() {
		return otPayPeriodMB;
	}

}
