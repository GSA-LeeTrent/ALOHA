package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.EmailNotificationEJB;
import gov.gsa.ocfo.aloha.ejb.SendMailEJB;
import gov.gsa.ocfo.aloha.ejb.UserEJB;
import gov.gsa.ocfo.aloha.exception.AlohaServerException;
import gov.gsa.ocfo.aloha.model.PayPeriod;
import gov.gsa.ocfo.aloha.model.entity.AlohaUserPref;
import gov.gsa.ocfo.aloha.model.entity.EtamsUser;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveDetail;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveHeader;
import gov.gsa.ocfo.aloha.model.entity.leave.LeaveItem;
import gov.gsa.ocfo.aloha.model.leave.LeaveRemark;
import gov.gsa.ocfo.aloha.util.Labels;
import gov.gsa.ocfo.aloha.util.Punctuation;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EmailNotificationEJBImpl implements EmailNotificationEJB{
	public static final String FROM_VALUE = "aloha-do-not-reply@gsa.gov";
	public static final String LINK_TO_ALOHA = "https://apps.ocfo.gsa.gov/aloha/";
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
	private AlohaUserPref userPref;
	
	private EtamsUser user;
	
	@EJB
	SendMailEJB emailEJB;
	
	@EJB
	UserEJB userEJB;

	@Override
	public void sendEmailNotifications(LeaveHeader leaveHeader) {
		this.sendEmailNotifications(leaveHeader.getLatestDetail());
	}

	@Override
	public void sendEmailNotifications(LeaveDetail leaveDetail) {
		//System.out.println("LREmailNotificationEJBImpl.sendEmailNotifications() : BEGIN");
		//StopWatch stopWatch = new StopWatch();
		//stopWatch.start();
		
		this.notifySubmitter(leaveDetail);

		if ( leaveDetail.getSubmitter().getUserId() != leaveDetail.getEmployee().getUserId()) {
			this.notifyEmployee(leaveDetail);
		}

		this.notifyApprover(leaveDetail);
		try {
			this.notifyTimekeeper(leaveDetail);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//stopWatch.stop();
		//System.out.println("ELAPSED TIME (sendEmailNotifications()[LR]): " + stopWatch.getElapsedTime() + " ms");
		//System.out.println("LREmailNotificationEJBImpl.sendEmailNotifications() : END");
		//stopWatch = null;
	}
	
	private void notifySubmitter(LeaveDetail leaveDetail) {
		String to = leaveDetail.getSubmitter().getEmailAddress();
		String subject = this.createSubjectForSubmitter(leaveDetail);
		String body = this.createBody(leaveDetail);
		this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
	}

	private void notifyEmployee(LeaveDetail leaveDetail) {
		String to = leaveDetail.getEmployee().getEmailAddress();
		String subject = this.createSubjectForEmployee(leaveDetail);
		String body = this.createBody(leaveDetail);
		this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
	}
	private void notifyApprover(LeaveDetail leaveDetail) {
		String to = leaveDetail.getApprover().getEmailAddress();
		String subject = this.createSubjectForApprover(leaveDetail);
		String body = this.createBody(leaveDetail);
		this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
	}
	private void notifyTimekeeper(LeaveDetail leaveDetail) throws SQLException {
		if ( (leaveDetail.isApproved()) || (leaveDetail.isWithdrawn()) ) {
	  	   try {
		      userPref = userEJB.getUserPref(leaveDetail.getEmployee().getUserId());
		      
		      if (userPref != null) {
			      if (userPref.getDefaultTimekeeperUserId() != null) {
			    	  if (leaveDetail.getSubmitter().getUserId() != userPref.getDefaultTimekeeperUserId()){
	  	                 user = userEJB.getEtamsUser(userPref.getDefaultTimekeeperUserId());
						
						 if (user.getEmailAddress() != null) {
						     String to = user.getEmailAddress();
						     String subject = this.createSubjectForTimekeeper(leaveDetail);
						     String body = this.createBody(leaveDetail);
						     this.emailEJB.sendMail(FROM_VALUE, to, subject, body);
						}
			         }
    		      }
			   }
		   } catch (AlohaServerException e) {
	 		   e.printStackTrace();
		   }		   
		}
	}
	private String createSubjectForSubmitter(LeaveDetail leaveDetail) {
		StringBuilder sb = new StringBuilder();
		if ( (leaveDetail.isApproved()) || (leaveDetail.isDenied()) ) {
			sb.append(Labels.NOTIFICATION);
		} else {
			sb.append(Labels.CONFIRMATION);
		}
		sb.append(this.completeSubject(leaveDetail));
		return sb.toString();
	}
	private String createSubjectForEmployee(LeaveDetail leaveDetail) {
		StringBuilder sb = new StringBuilder();
		sb.append(Labels.NOTIFICATION);
		sb.append(this.completeSubject(leaveDetail));
		return sb.toString();
	}
	private String createSubjectForApprover(LeaveDetail leaveDetail) {
		StringBuilder sb = new StringBuilder();
		if ( (leaveDetail.isApproved()) || (leaveDetail.isDenied()) ) {
			sb.append(Labels.CONFIRMATION);
		} else {
			sb.append(Labels.NOTIFICATION);
		}
		sb.append(this.completeSubject(leaveDetail));
		return sb.toString();
	}
	private String createSubjectForTimekeeper(LeaveDetail leaveDetail) {
		StringBuilder sb = new StringBuilder();
	    sb.append(Labels.NOTIFICATION);
		sb.append(this.completeSubject(leaveDetail));
		return sb.toString();
	}
	private String completeSubject(LeaveDetail leaveDetail) {
		StringBuilder sb = new StringBuilder();

		String lname = leaveDetail.getLeaveHeader().getEmployee().getLastName();
		String fname = leaveDetail.getLeaveHeader().getEmployee().getFirstName();

		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(Labels.LEAVE_REQUEST);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getLeaveStatus().getLabel());

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
	private String createBody(LeaveDetail leaveDetail) {
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

		sb.append(Labels.DESCRIPTION);
		sb.append(Punctuation.COLON);
		sb.append(Punctuation.SPACE);
		sb.append(leaveDetail.getLeaveStatus().getDescription());
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

		sb.append(Labels.APPROVER);
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

		/*
		if ( (leaveDetail.isApproved()) || (leaveDetail.isDenied()) ) {
			sb.append(Labels.APPROVER_REMARKS);
			sb.append(Punctuation.COLON);
			sb.append(Punctuation.NEW_LINE);
			for (LeaveApproverComment lac : leaveDetail.getApproverComments())
			{
			   sb.append(lac.getComment());
			   sb.append(Punctuation.NEW_LINE);
			   sb.append(Punctuation.NEW_LINE);
			}
		}  else {
			sb.append(Labels.SUBMITTER_REMARKS);
		    sb.append(Punctuation.COLON);
		    sb.append(Punctuation.NEW_LINE);
			for (LeaveSubmitterComment lsc : leaveDetail.getSubmitterComments())
			{
			   sb.append(lsc.getComment());
			   sb.append(Punctuation.NEW_LINE);
			   sb.append(Punctuation.NEW_LINE);
			}
		}
		*/
		
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