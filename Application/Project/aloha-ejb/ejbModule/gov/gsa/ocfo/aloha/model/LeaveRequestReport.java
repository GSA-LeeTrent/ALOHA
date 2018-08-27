package gov.gsa.ocfo.aloha.model;

import java.io.Serializable;
import java.util.Date;

public class LeaveRequestReport implements Serializable {

	private static final long serialVersionUID = -7296983731832220132L;

	private int empId;
	private String empFName;
	private String empLName;
	private String empMName;
	private int year;
	private int payPeriod;
	private Date leaveDt;
	private String primaryLeaveType;
	private String secondaryLeaveType;
	private float leaveHrsAloha;
	private String leaveStatus;
	private Long lrDetailId;
	private Long lrHeaderId;
	private String fatCode; 
	private Long lrdSeq;
	private Date leaveStartTime;
	
//	public String getFacilityCd() {
//		return facilityCd;
//	}
//	public void setFacilityCd(String facilityCd) {
//		this.facilityCd = facilityCd;
//	}
//	public String getFacilityDesc() {
//		return facilityDesc;
//	}
//	public void setFacilityDesc(String facilityDesc) {
//		this.facilityDesc = facilityDesc;
//	}
//	public String getAreaCd() {
//		return areaCd;
//	}
//	public void setAreaCd(String areaCd) {
//		this.areaCd = areaCd;
//	}
//	public String getAreaDesc() {
//		return areaDesc;
//	}
//	public void setAreaDesc(String areaDesc) {
//		this.areaDesc = areaDesc;
//	}
//	public String getTeamCd() {
//		return teamCd;
//	}
//	public void setTeamCd(String teamCd) {
//		this.teamCd = teamCd;
//	}
//	public String getTeamDesc() {
//		return teamDesc;
//	}
//	public void setTeamDesc(String teamDesc) {
//		this.teamDesc = teamDesc;
//	}
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

	public void setPrimaryLeaveType(String primaryLeaveType) {
		this.primaryLeaveType = primaryLeaveType;
	}
	public String getPrimaryLeaveType() {
		return primaryLeaveType;
	}
	public void setSecondaryLeaveType(String secondaryLeaveType) {
		this.secondaryLeaveType = secondaryLeaveType;
	}
	public String getSecondaryLeaveType() {
		return secondaryLeaveType;
	}
	public float getLeaveHrsAloha() {
		return leaveHrsAloha;
	}
	public void setLeaveHrsAloha(float leaveHrsAloha) {
		this.leaveHrsAloha = leaveHrsAloha;
	}
//	public float getLeaveHrsEtams() {
//		return leaveHrsEtams;
//	}
//	public void setLeaveHrsEtams(float leaveHrsEtams) {
//		this.leaveHrsEtams = leaveHrsEtams;
//	}
	public void setLeaveStatus(String leaveStatus) {
		this.leaveStatus = leaveStatus;
	}
	public String getLeaveStatus() {
		return leaveStatus;
	}
	public void setLrDetailId(Long lrDetailId) {
		this.lrDetailId = lrDetailId;
	}
	public Long getLrDetailId() {
		return lrDetailId;
	}
	public void setLrHeaderId(Long lrHeaderId) {
		this.lrHeaderId = lrHeaderId;
	}
	public Long getLrHeaderId() {
		return lrHeaderId;
	}
	public String getFatCode() {
		return fatCode;
	}
	public void setFatCode(String fatCode) {
		this.fatCode = fatCode;
	}
	public void setLrdSeq(Long lrdSeq) {
		this.lrdSeq = lrdSeq;
	}
	public Long getLrdSeq() {
		return lrdSeq;
	}
	public Date getLeaveStartTime() {
		return leaveStartTime;
	}
	public void setLeaveStartTime(Date leaveStartTime) {
		this.leaveStartTime = leaveStartTime;
	}
}