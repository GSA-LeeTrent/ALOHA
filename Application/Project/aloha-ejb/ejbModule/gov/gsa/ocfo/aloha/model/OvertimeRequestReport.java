package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;

public class OvertimeRequestReport implements Serializable {

	private static final long serialVersionUID = -7296983731832220132L;

	private int empId;
	private String empFName;
	private String empLName;
	private String empMName;
	private String appFName;
	private String appLName;
	private String appMName;
	private String planGrade;
	private String taskDesc;
	private int year;
	private int payPeriod;
	private String otDt;
	private String overtimeType;
	private float overtimeHrsAloha;
	private String overtimeStatus;
	private Long otDetailId;
	private Long otHeaderId;
//	private Long otdSeq;
	private String FATCode;
	
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
	
	public String getAppFName() {
		return appFName;
	}
	public void setAppFName(String appFName) {
		this.appFName = appFName;
	}
	public String getAppLName() {
		return appLName;
	}
	public void setAppLName(String appLName) {
		this.appLName = appLName;
	}
	public String getAppMName() {
		return appMName;
	}
	public void setAppMName(String appMName) {
		this.appMName = appMName;
	}
	
	public String getPlanGrade() {
		return planGrade;
	}
	public void setPlanGrade(String planGrade) {
		this.planGrade = planGrade;
	}
	
	public String getTaskDesc() {
		return taskDesc;
	}
	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
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
	public void setOvertimeDt(String otDt) {
		this.otDt = otDt;
	}
	
//	public void setOtdSeq(Long otdSeq) {
//		this.otdSeq = otdSeq;
//	}
//	public Long getOtdSeq() {
//		return otdSeq;
//	}

	public void setOvertimeType(String overtimeType) {
		this.overtimeType = overtimeType;
	}
	public String getOvertimeType() {
		return overtimeType;
	}
	public float getOvertimeHrsAloha() {
		return overtimeHrsAloha;
	}
	public void setOvertimeHrsAloha(float overtimeHrsAloha) {
		this.overtimeHrsAloha = overtimeHrsAloha;
	}

	public void setOvertimeStatus(String overtimeStatus) {
		this.overtimeStatus = overtimeStatus;
	}
	public String getOvertimeStatus() {
		return overtimeStatus;
	}
	public void setOtDetailId(Long otDetailId) {
		this.otDetailId = otDetailId;
	}
	public Long getOtDetailId() {
		return otDetailId;
	}
	public void setOtHeaderId(Long otHeaderId) {
		this.otHeaderId = otHeaderId;
	}
	public Long getOtHeaderId() {
		return otHeaderId;
	}
	public void setFATCode(String fATCode) {
		FATCode = fATCode;
	}
	public String getFATCode() {
		return FATCode;
	}
	
}