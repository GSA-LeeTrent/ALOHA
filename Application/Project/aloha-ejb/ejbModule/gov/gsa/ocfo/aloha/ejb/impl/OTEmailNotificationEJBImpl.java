package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.OTEmailNotificationEJB;
import gov.gsa.ocfo.aloha.ejb.SendMailEJB;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetail;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSubmitterRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTDetailSupervisorRemark;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroup;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTGroupStatus;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTHeader;
import gov.gsa.ocfo.aloha.model.entity.overtime.OTItem;
import gov.gsa.ocfo.aloha.util.Labels;
import gov.gsa.ocfo.aloha.util.Punctuation;

import java.text.SimpleDateFormat;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OTEmailNotificationEJBImpl implements OTEmailNotificationEJB{
	public static final String FROM_VALUE = "aloha-do-not-reply@gsa.gov";
	public static final String LINK_TO_ALOHA = "https://apps.ocfo.gsa.gov/aloha/";

	@EJB
	SendMailEJB emailEJB;

	@Override
	public void sendEmailNotifications(OTGroup otGroup) {
		//System.out.println("Group Status :" + otGroup.getStatusCode());
		//send email to receiver
		if (otGroup.getStatus().getCode().equals(OTGroupStatus.CodeValues.SUBMITTED)) {
			//System.out.println("sending email to receiver " + otGroup.getReceiver().getFullName());
			String to = otGroup.getReceiver().getEmailAddress();
			String subject = "ALOHA: OT Group Request #" + otGroup.getGroupId() + " SUBMITTED. From " + otGroup.getSubmitterName() + " for PP: " + otGroup.getPayPeriodDateRange();
			//System.out.println("Subject = " + subject);
			String body = "OT Group Id: " + otGroup.getGroupId() + "\n";
			body = body + "Pay Period: " + otGroup.getPayPeriodDateRange() + "\n";
			body = body + "Status: " + otGroup.getStatusCode() + "\n";
			body = body + "Submitter: " + otGroup.getSubmitterName() + "\n";
			body = body + "No. of Requests: " + otGroup.getEmployeeCount() + "\n";
			body = body + "Est. No. of Hrs: " + otGroup.getEstNbrOfHrs();
			//System.out.println("Body = " + body);
			this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
			
		}
		//send email to group submitters & employees on final
		if (otGroup.getStatus().getCode().equals(OTGroupStatus.CodeValues.FINAL) || 
				otGroup.getStatus().getCode().equals(OTGroupStatus.CodeValues.CANCELLED)) {
			//System.out.println("sending email to submitter " + otGroup.getSubmitter().getFullName());
			//System.out.println("Group status : " + otGroup.getStatus().getName());
			
			emailGroupSubmitter(otGroup);
			
			//check child groups and notify group submitters
			notifyChildGroups(otGroup);
			
			for (OTHeader oth : otGroup.getAllEmployees()) {
				notifyEmployee(oth.getLatestDetail());
			}
		}
		
		//send email to group submitter on receive
		if (otGroup.getStatus().getCode().equals(OTGroupStatus.CodeValues.RECEIVED) ) {
			emailGroupSubmitter(otGroup);
		}		
	}
	
	private void notifyChildGroups(OTGroup otGroup) {
		for (OTGroup otg : otGroup.getChildren()) {
			//System.out.println("sending email to submitter " + otg.getSubmitter().getFullName());
			//System.out.println("Group status : " + otg.getStatus().getName());
			
			emailGroupSubmitter(otg);
			//check child groups and notify group submitters
			notifyChildGroups(otg);			
		}		
	}
	
	private void emailGroupSubmitter(OTGroup otGroup){
		String to = otGroup.getSubmitter().getEmailAddress();
		
		String subject = "ALOHA: OT Group Request #" + otGroup.getGroupId() + " " + otGroup.getStatusCode() + ". PP: " + otGroup.getPayPeriodDateRange();
		//System.out.println("Subject = " + subject);
		
		String body = "OT Group Id: " + otGroup.getGroupId() + "\n";
		body = body + "Pay Period: " + otGroup.getPayPeriodDateRange() + "\n";
		body = body + "Status: " + otGroup.getStatusCode() + "\n";
		body = body + "No. of Requests: " + otGroup.getEmployeeCount() + "\n";
		body = body + "Est. No. of Hrs: " + otGroup.getEstNbrOfHrs();
		//System.out.println("Body = " + body);
		this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
	}
	
	@Override
	public void sendEmailNotifications(OTHeader otHeader) {
		this.sendEmailNotifications(otHeader.getLatestDetail());
	}

	@Override
	public void sendEmailNotifications(OTDetail otDetail) {
		//System.out.println("OTEmailNotificationEJBImpl.sendEmailNotifications() : BEGIN");
		//StopWatch stopWatch = new StopWatch();
		//stopWatch.start();

		this.notifySubmitter(otDetail);

		if ( otDetail.getSubmitter().getUserId() != otDetail.getEmployee().getUserId()) {
			this.notifyEmployee(otDetail);
		}

		this.notifyApprover(otDetail);

		//stopWatch.stop();
		//System.out.println("ELAPSED TIME (sendEmailNotifications()[OT]): " + stopWatch.getElapsedTime() + " ms");
		//System.out.println("OTEmailNotificationEJBImpl.sendEmailNotifications() : END");		
	}
	private void notifySubmitter(OTDetail otDetail) {
		String to = otDetail.getSubmitter().getEmailAddress();
		String subject = this.createSubjectForSubmitter(otDetail);
		String body = this.createBody(otDetail);
		if ( !(otDetail.isReceived()) ) {
		   this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
		}
	}

	private void notifyEmployee(OTDetail otDetail) {
		String to = otDetail.getEmployee().getEmailAddress();
		String subject = this.createSubjectForEmployee(otDetail);
		String body = this.createBody(otDetail);
		if ( !(otDetail.isReceived()) ) {
		   this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
		}
	}
	private void notifyApprover(OTDetail otDetail) {
		String to = otDetail.getSupervisor().getEmailAddress();
		String subject = this.createSubjectForApprover(otDetail);
		String body = this.createBody(otDetail);
		if ( !(otDetail.isReceived()) ) {
	    	this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
		}	
	}
	private String createSubjectForSubmitter(OTDetail otDetail) {
		StringBuilder sb = new StringBuilder();
		if ( (otDetail.isApproved()) || (otDetail.isDenied()) ) {
			sb.append(Labels.NOTIFICATION);
		} else {
			sb.append(Labels.CONFIRMATION);
		}
		sb.append(this.completeSubject(otDetail));
		return sb.toString();
	}
	private String createSubjectForEmployee(OTDetail otDetail) {
		StringBuilder sb = new StringBuilder();
		sb.append(Labels.NOTIFICATION);
		sb.append(this.completeSubject(otDetail));
		return sb.toString();
	}
	private String createSubjectForApprover(OTDetail otDetail) {
		StringBuilder sb = new StringBuilder();
		if ( (otDetail.isApproved()) || (otDetail.isDenied()) ) {
			sb.append(Labels.CONFIRMATION);
		} else {
			sb.append(Labels.NOTIFICATION);
		}
		sb.append(this.completeSubject(otDetail));
		return sb.toString();
	}
	private String completeSubject(OTDetail otDetail) {
		StringBuilder sb = new StringBuilder();

		String lname = otDetail.getHeader().getEmployee().getLastName();
		String fname = otDetail.getHeader().getEmployee().getFirstName();

		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(Labels.OT_REQUEST);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getStatus().getName());

		sb.append(Punctuation.SPACE);
		sb.append(Punctuation.OPENING_PARENTHESIS);
		sb.append(lname);
		sb.append(Punctuation.COMMA);
		sb.append(fname);
		sb.append(Punctuation.SPACE);
		sb.append(Punctuation.POUND_SIGN);
		sb.append(otDetail.getRequestId());
		sb.append(Punctuation.CLOSING_PARENTHESIS);


		return sb.toString();
	}
	private String createBody(OTDetail otDetail) {
		StringBuilder sb = new StringBuilder();
		sb.append(Labels.OT_REQUEST_ID);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getRequestId());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.PAY_PERIOD);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		SimpleDateFormat formatSDate = new SimpleDateFormat("MM/dd/yyyy");
		String startDate = formatSDate.format(otDetail.getPayPeriod().getStartDate());
		sb.append(startDate);
		sb.append(Punctuation.SPACE);
		sb.append(Punctuation.DASH);
		SimpleDateFormat formatEDate = new SimpleDateFormat("MM/dd/yyyy");
		String endDate = formatEDate.format(otDetail.getPayPeriod().getEndDate());
		sb.append(endDate);
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.STATUS);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getStatus().getName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.DESCRIPTION);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getStatus().getName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.EMPLOYEE);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getEmployee().getFullName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.SUBMITTER);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getSubmitter().getFullName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Labels.APPROVER);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(otDetail.getSupervisor().getFullName());
		sb.append(Punctuation.NEW_LINE);

		sb.append(Punctuation.NEW_LINE);
		sb.append(Labels.DETAILS);
		sb.append(Punctuation.COLON);

		for ( OTItem otItem : otDetail.getItems()) {
			sb.append(Punctuation.NEW_LINE);
			sb.append(Labels.OT_TYPE);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.SPACE);
			sb.append(otDetail.getType().getCode());
			sb.append(Punctuation.SPACE);
			sb.append(Punctuation.DASH);
			sb.append(Punctuation.SPACE);
			sb.append(otDetail.getType().getName());
			sb.append(Punctuation.NEW_LINE);

			sb.append("Task");
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.SPACE);
			sb.append(otItem.getTaskDescription());
			sb.append(Punctuation.NEW_LINE);

			sb.append(Labels.NUMBER_OF_HOURS);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.SPACE);
			sb.append(otItem.getEstimatedHours());
			sb.append(Punctuation.NEW_LINE);
			sb.append(Punctuation.NEW_LINE);
		}
//Need to add logic to determine who is actually doing the cancellation so the remarks get on the appropriate email
		if ( (otDetail.isApproved()) || (otDetail.isDenied())) {
			sb.append(Labels.APPROVER_REMARKS);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.NEW_LINE);
			for (OTDetailSupervisorRemark osr : otDetail.getSupervisorRemarks())
			{
			   sb.append(osr.getText());
			   sb.append(Punctuation.NEW_LINE);
			   sb.append(Punctuation.NEW_LINE);
			}
		}else if  (otDetail.isCancelled()) { 
			sb.append(Labels.APPROVER_REMARKS);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.NEW_LINE);
			for (OTDetailSupervisorRemark osr : otDetail.getSupervisorRemarks())
			{
			   sb.append(osr.getText());
			   sb.append(Punctuation.NEW_LINE);
			   sb.append(Punctuation.NEW_LINE);
			}
			sb.append(Punctuation.NEW_LINE);
			sb.append(Labels.SUBMITTER_REMARKS);
		    sb.append(Punctuation.COLON);
		    sb.append(Punctuation.NEW_LINE);
			for (OTDetailSubmitterRemark osur : otDetail.getSubmitterRemarks())
			{
			   sb.append(osur.getText());
			   sb.append(Punctuation.NEW_LINE);
			   sb.append(Punctuation.NEW_LINE);
			}
		}else
		    {sb.append(Labels.SUBMITTER_REMARKS);
		    sb.append(Punctuation.COLON);
		    sb.append(Punctuation.NEW_LINE);
			for (OTDetailSubmitterRemark osur : otDetail.getSubmitterRemarks())
			{
			   sb.append(osur.getText());
			   sb.append(Punctuation.NEW_LINE);
			   sb.append(Punctuation.NEW_LINE);
			}
		}

		sb.append(Punctuation.NEW_LINE);
		sb.append(Labels.LINK);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(LINK_TO_ALOHA);

		return sb.toString();
	}
}