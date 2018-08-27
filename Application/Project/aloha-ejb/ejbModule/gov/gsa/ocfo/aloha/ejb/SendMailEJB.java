package gov.gsa.ocfo.aloha.ejb;

import javax.ejb.Local;


@Local
public interface SendMailEJB {

	public void sendMail(
			String from,
			String to, 
			String subject,
			String body);

	public void sendMail(
					String from,
					String to, 
					String cc,
					String bcc,
					String subject,
					String body);
}
