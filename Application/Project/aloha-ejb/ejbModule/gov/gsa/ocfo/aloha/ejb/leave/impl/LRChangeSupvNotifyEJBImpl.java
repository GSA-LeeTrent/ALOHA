package gov.gsa.ocfo.aloha.ejb.leave.impl;

import gov.gsa.ocfo.aloha.ejb.SendMailEJB;
import gov.gsa.ocfo.aloha.ejb.leave.LRChangeSupvNotifyEJB;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.leave.LeaveRemark;
import gov.gsa.ocfo.aloha.util.Labels;
import gov.gsa.ocfo.aloha.util.Punctuation;

import java.text.SimpleDateFormat;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class LRChangeSupvNotifyEJBImpl implements LRChangeSupvNotifyEJB {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	@EJB
	SendMailEJB emailEJB;
	
	public void sendEmailNotifications(LeaveDetail leaveDetail, AlohaUser oldSupvervisor) {
		this.notifyOldSupvervisor(leaveDetail, oldSupvervisor);
		this.notifyNewSupvervisor(leaveDetail, oldSupvervisor);
	}
	
	private void notifyOldSupvervisor(LeaveDetail leaveDetail, AlohaUser oldSupvervisor) {
		String to = oldSupvervisor.getEmailAddress();
		String subject = this.createSubject(leaveDetail);
		String body = this.createBody(leaveDetail, oldSupvervisor);
		this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
	}
	
	private void notifyNewSupvervisor(LeaveDetail leaveDetail, AlohaUser oldSupvervisor) {
		String to = leaveDetail.getApprover().getEmailAddress();
		String subject = this.createSubject(leaveDetail);
		String body = this.createBody(leaveDetail, oldSupvervisor);
		this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
	}
	private String createSubject(LeaveDetail leaveDetail) {
		StringBuilder sb = new StringBuilder();
		sb.append(Labels.NOTIFICATION);

		String lname = leaveDetail.getLeaveHeader().getEmployee().getLastName();
		String fname = leaveDetail.getLeaveHeader().getEmployee().getFirstName();

		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getLeaveStatus().getLabel());
		sb.append(Punctuation.SPACE);
		sb.append(Labels.LEAVE_REQUEST);
		sb.append(Punctuation.SPACE);
		sb.append(Labels.REASSIGNED);
		sb.append(Punctuation.SPACE);
		sb.append(Punctuation.OPENING_PARENTHESIS);
		sb.append(lname);
		sb.append(Punctuation.COMMA);
		sb.append(fname);
		sb.append(Punctuation.SPACE);
		sb.append(Punctuation.POUND_SIGN);
		sb.append(leaveDetail.getLeaveHeaderId());
		sb.append(Punctuation.CLOSING_PARENTHESIS);
		
		return sb.toString();
	}
	
	private String createBody(LeaveDetail leaveDetail, AlohaUser oldSupervisor) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(Labels.LEAVE_REQUEST_ID);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getLeaveHeaderId());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.PAY_PERIOD);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getPayPeriodDateRange());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.STATUS);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getLeaveStatus().getLabel());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.EMPLOYEE);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getEmployee().getFullName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.SUBMITTER);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getSubmitter().getFullName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Punctuation.NEW_LINE);
		sb.append(Labels.DESCRIPTION);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(Labels.DESC_CHANGED_SUPV);
		sb.append(Punctuation.NEW_LINE);
		
		sb.append(Labels.OLD_SUPV_MGR);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(oldSupervisor.getFullName());
		sb.append(Punctuation.NEW_LINE);
		
		sb.append(Labels.NEW_SUPV_MGR);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getApprover().getFullName());
		sb.append(Punctuation.NEW_LINE);
	
		sb.append(Punctuation.NEW_LINE);
		sb.append(Labels.DETAILS);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.NEW_LINE);
		sb.append(Punctuation.NEW_LINE);

		for ( LeaveItem leaveItem : leaveDetail.getLeaveItems()) {
			sb.append(Punctuation.NEW_LINE);
			sb.append(Labels.DATE_OF_LEAVE);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.SPACE);
			SimpleDateFormat sdf = new SimpleDateFormat(PayPeriod.LABEL_FORMAT);
			sb.append(sdf.format(leaveItem.getLeaveDate()));

			sb.append(Punctuation.NEW_LINE);
			sb.append(Labels.LEAVE_TYPE);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.SPACE);
			sb.append(leaveItem.getLeaveType().getLabel());
			sb.append(Punctuation.NEW_LINE);

			sb.append(Labels.NUMBER_OF_HOURS);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.SPACE);
			sb.append(leaveItem.getLeaveHours());
			sb.append(Punctuation.NEW_LINE);
		}

		sb.append(Punctuation.NEW_LINE);
		sb.append(Labels.PREVIOUS_REMARKS);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.NEW_LINE);
		sb.append(Punctuation.NEW_LINE);
			
		for ( LeaveRemark leaveRemark : leaveDetail.getLeaveHeader().getAllRemarks()) {
			sb.append(leaveRemark.getActor().getFullName());
			sb.append(Punctuation.SPACE);
			sb.append(Punctuation.OPENING_PARENTHESIS);
			sb.append(Labels.ON);
			sb.append(Punctuation.SPACE);
			sb.append(dateFormat.format(leaveRemark.getRemarkDate()));
			sb.append(Punctuation.CLOSING_PARENTHESIS);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.NEW_LINE);
			sb.append(leaveRemark.getRemark());
			sb.append(Punctuation.NEW_LINE);
			sb.append(Punctuation.NEW_LINE);
		}

		sb.append(Punctuation.NEW_LINE);
		sb.append(Labels.LINK);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(LINK_TO_ALOHA);

		return sb.toString();
	}
}