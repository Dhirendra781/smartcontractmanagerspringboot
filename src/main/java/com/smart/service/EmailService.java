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
		
		boolean f = false;
		
		String from = "dhiruyadav661@gmail.com";
		
		
		//variable for email
		String host="smtp.gmail.com";
				
		//get system properties
		Properties properties = System.getProperties();
		System.out.println("Properties"+properties);
				
		//setting important information to properties object
				
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
				
		//get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("dhiruyadav661@gmail.com", "Dhiru@123");
				}
					
		});
				
		session.setDebug(true);
		
		//Step 2 compose the message [text, multi media]
		MimeMessage mimeMessage = new MimeMessage(session);
				
		try {	
			mimeMessage.setFrom(from);
					
			//adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
					
			//adding subject to message
			mimeMessage.setSubject(subject);
					
			//adding text to message
			//mimeMessage.setText(message);
			mimeMessage.setContent(message,"text/html");
					
			//Step 3 send message using transport class
			Transport.send(mimeMessage);
			System.out.println("Email Sent successfully.");
			f = true;
			
			}catch(Exception e) {
				e.printStackTrace();
			}
		
			return f;
		
		}

}
