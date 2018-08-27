package gov.gsa.ocfo.aloha.ejb.impl;

import gov.gsa.ocfo.aloha.ejb.SendMailEJB;
import gov.gsa.ocfo.aloha.util.StringUtil;

import java.util.Date;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Session Bean implementation class SendMailEJBImpl
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SendMailEJBImpl implements SendMailEJB {
	private static final String SMPT_HOST_KEY = "mail.smtp.host";
	private static final String SMPT_HOST_VALUE = "smtp.gsa.gov"; 
	
	public void sendMail(String from, String to, String subject, String body) {
		this.sendMail(from, to, null, null, subject, body);
	}
	
	public void sendMail(String from, String to, String cc, String bcc, String subject,	String body) {
		try	{
			//System.out.println("SendMailEJBImpl.sendMail() : BEGIN");
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			
			Message message = this.createMessage();
			message.setFrom(new InternetAddress(from));
			this.setToCcBccRecipients(message, to, cc, bcc);
			
			message.setSentDate(new Date());
			message.setSubject(subject);
			message.setText(body);
			this.sendMessage(message);

			//stopWatch.stop();
			//System.out.println("ELAPSED TIME (sendMail()): " + stopWatch.getElapsedTime() + " ms");
			//System.out.println("SendMailEJBImpl.sendMail() : END");
			//stopWatch = null;
			
	    } catch (AddressException ae) {
	    	ae.printStackTrace();
	    } catch (MessagingException me) {
	    	me.printStackTrace();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(Message message) throws MessagingException {
		Transport.send(message);

	}
	
	private Message createMessage() {
	    Properties properties = new Properties();
	    properties.put(SMPT_HOST_KEY, SMPT_HOST_VALUE);
	    Session session = Session.getDefaultInstance(properties, null);
	    return new MimeMessage(session);
	  }

	private void setToCcBccRecipients(Message message, String to, String cc, String bcc) 
		throws AddressException, MessagingException {
	    setMessageRecipients(message, to, Message.RecipientType.TO);

	    if ( !StringUtil.isNullOrEmpty(cc)) {
	    	setMessageRecipients(message, cc, Message.RecipientType.CC);
	    }
	    if ( !StringUtil.isNullOrEmpty(bcc)) {
		    setMessageRecipients(message, bcc, Message.RecipientType.BCC);	    	
	    }
	  } 

	  private void setMessageRecipients(Message message, String recipient, Message.RecipientType recipientType)
	  throws AddressException, MessagingException {
	    InternetAddress[] addressArray = buildInternetAddressArray(recipient);
	    if ((addressArray != null) && (addressArray.length > 0)) {
	      message.setRecipients(recipientType, addressArray);
	    }
	  }

	  /**
	   * The address can be a comma-separated sequence of email addresses.
	   * @see mail.internet.InternetAddress.parse() for details.
	   *
	   */
	  private InternetAddress[] buildInternetAddressArray(String address) throws AddressException {
		  // could test for a null or blank String but I'm letting parse just throw an exception
		  return InternetAddress.parse(address);
	  }
}