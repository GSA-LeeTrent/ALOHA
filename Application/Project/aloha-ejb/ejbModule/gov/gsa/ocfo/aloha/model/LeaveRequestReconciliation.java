package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.util.Date;

public class LeaveRequestReconciliation implements Serializable {
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
	private int year;
	private int payPeriod;
	private Date leaveDt;
	private float leaveHrsAloha;
	private float leaveHrsEtams;
	private String leaveType;
	private String lrDetailId; //SAK 20120123 removed from use
	private String lrHeaderId; //SAK 20120123 report changed to use LRH
	private String FATCode; //SAK 20120612 added back FAT code
	
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
	public Date getLeaveDt() {
		return leaveDt;
	}
	public void setLeaveDt(Date leaveDt) {
		this.leaveDt = leaveDt;
	}
	public String getLeaveType() {
		return leaveType;
	}
	public void setLeaveType(String leaveType) {
		this.leaveType = leaveType;
	}
	public void setLeaveHrsAloha(float leaveHrsAloha) {
		this.leaveHrsAloha = leaveHrsAloha;
	}
	public float getLeaveHrsAloha() {
		return leaveHrsAloha;
	}
	public void setLeaveHrsEtams(float leaveHrsEtams) {
		this.leaveHrsEtams = leaveHrsEtams;
	}
	public float getLeaveHrsEtams() {
		return leaveHrsEtams;
	}
	public void setLrDetailId(String lrDetailId) {
		this.lrDetailId = lrDetailId;
	}
	public String getLrDetailId() {
		return lrDetailId;
	}
	public void setLrHeaderId(String lrHeaderId) {
		this.lrHeaderId = lrHeaderId;
	}
	public String getLrHeaderId() {
		return lrHeaderId;
	}
	public String getFATCode() {
		return FATCode;
	}
	public void setFATCode(String fATCode) {
		FATCode = fATCode;
	}

}
