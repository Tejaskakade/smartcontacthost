package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String message, String to) {
		
		boolean f=false;
		// variable for gmail host
		
		        String from= "tejaskakade4545@gmail.com";
				String host="smtp.gmail.com";
				
				//get the system properties
				
				Properties properties = System.getProperties();
				System.out.println("PROPERTIES "+properties);
				
				//setting imp info to properties object
				
				//host set
				properties.put("mail.smtp.host", host);
				properties.put("mail.smtp.port", "465");
				properties.put("mail.smtp.ssl.enable", "true");
				properties.put("mail.smtp.auth","true");
				
				
				// Step 1: to get the Sesson object
				
				Session session=Session.getInstance(properties, new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						// TODO Auto-generated method stub
						return new PasswordAuthentication("tejaskakade4545@gmail.com", "ljwjhfgvzyatxczq");
					}
								
				});
				
				session.setDebug(true);
				
				//step 2:   Compose the message[text, multi media]
				
				MimeMessage m = new MimeMessage(session);
					
				try {
					//from email
					m.setFrom(from);
					
					//adding recipient to message
					m.addRecipient(Message.RecipientType.TO	, new InternetAddress(to));
					
					//adding subject to messaage
					
					m.setSubject(subject);
					
					//adding teext to message
//					m.setText(message);
					m.setContent(message,"text/html");    //it resolve html as it is
					
					//step 3:  send message using Transport class
					
					Transport.send(m);
					System.out.println(" Send Successfully.....");
					f=true;
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}	
				
				return f;
		
		
	}

}
