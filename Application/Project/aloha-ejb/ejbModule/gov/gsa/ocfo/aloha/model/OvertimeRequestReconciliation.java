package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;

public class OvertimeRequestReconciliation implements Serializable {
//sak
	private static final long serialVersionUID = -4641702203735197213L;
	
	private String facilityCd;
	private String facilityDesc;
	private String areaCd;
	private String areaDesc;
	private String teamCd;
	private String teamDesc;
	private int empId;
	private String empFName;
	private String empLName;
	private String empMName;
	private String planGrade;
	private int year;
	private int payPeriod;
	private String otDt;
	private float overtimeHrsAloha;
	private float overtimeHrsEtams;
	private String overtimeType;
	private String otDetailId; //SAK 20120123 removed from use
	private String otHeaderId; //SAK 20120123 report changed to use OTH 
	private String FATCode; //sak 20120613 added F-A-T to report
	private String AppFName;
	private String AppMName;
	private String AppLName;
	
	public String getFacilityCd() {
		return facilityCd;
	}
	public void setFacilityCd(String facilityCd) {
		this.facilityCd = facilityCd;
	}
	public String getFacilityDesc() {
		return facilityDesc;
	}
	public void setFacilityDesc(String facilityDesc) {
		this.facilityDesc = facilityDesc;
	}
	public String getAreaCd() {
		return areaCd;
	}
	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}
	public String getAreaDesc() {
		return areaDesc;
	}
	public void setAreaDesc(String areaDesc) {
		this.areaDesc = areaDesc;
	}
	public String getTeamCd() {
		return teamCd;
	}
	public void setTeamCd(String teamCd) {
		this.teamCd = teamCd;
	}
	public String getTeamDesc() {
		return teamDesc;
	}
	public void setTeamDesc(String teamDesc) {
		this.teamDesc = teamDesc;
	}
	public int getEmpId() {
		return empId;
	}
	public void setEmpId(int empId) {
		this.empId = empId;
	}
	public String getEmpFName() {
		return empFName;
	}
	public void setEmpFName(String empFName) {
		this.empFName = empFName;
	}
	public String getEmpLName() {
		return empLName;
	}
	public void setEmpLName(String empLName) {
		this.empLName = empLName;
	}
	public String getEmpMName() {
		return empMName;
	}
	public void setEmpMName(String empMName) {
		this.empMName = empMName;
	}
	public String getPlanGrade() {
		return planGrade;
	}
	public void setPlanGrade(String planGrade) {
		this.planGrade = planGrade;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getPayPeriod() {
		return payPeriod;
	}
	public void setPayPeriod(int payPeriod) {
		this.payPeriod = payPeriod;
	}
	public String getOvertimeDt() {
		return otDt;
	}
	public void setOvertimeDt(String string) {
		this.otDt = string;
	}
	public String getOvertimeType() {
		return overtimeType;
	}
	public void setOvertimeType(String overtimeType) {
		this.overtimeType = overtimeType;
	}
	public void setOvertimeHrsAloha(float overtimeHrsAloha) {
		this.overtimeHrsAloha = overtimeHrsAloha;
	}
	public float getOvertimeHrsAloha() {
		return overtimeHrsAloha;
	}
	public void setOvertimeHrsEtams(float overtimeHrsEtams) {
		this.overtimeHrsEtams = overtimeHrsEtams;
	}
	public float getOvertimeHrsEtams() {
		return overtimeHrsEtams;
	}
	public void setOtDetailId(String otDetailId) {
		this.otDetailId = otDetailId;
	}
	public String getOtDetailId() {
		return otDetailId;
	}
	public void setOtHeaderId(String otHeaderId) {
		this.otHeaderId = otHeaderId;
	}
	public String getOtHeaderId() {
		return otHeaderId;
	}
	public void setFATCode(String fATCode) {
		FATCode = fATCode;
	}
	public String getFATCode() {
		return FATCode;
	}
	public void setAppFName(String appFName) {
		AppFName = appFName;
	}
	
	public String getAppFName() {
		return AppFName;
	}
	
	public void setAppMName(String appMName) {
		AppMName = appMName;
	}
	
	public String getAppMName() {
		return AppMName;
	}
	
	public void setAppLName(String appLName) {
		AppLName = appLName;
	}
	
	public String getAppLName() {
		return AppLName;
	}

}
