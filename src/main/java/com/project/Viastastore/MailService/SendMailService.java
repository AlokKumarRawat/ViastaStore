package com.project.Viastastore.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.project.Viastastore.Model.Users;

@Service
public class SendMailService {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	public void sendOtpMail(Users user) throws Exception {
		SimpleMailMessage mailMessage=new SimpleMailMessage();
		
		String subject="Welcome to Viasta Store, Please verify OTP";
		String message="Hello "+user.getName()+"\nWelcome to Viasta Store\n Use the following OTP to complete your verification\n\n OTP: "+user.getOtp()+"\n\n This OTP will expire in 5 minutes. For security reasons, never share this code with anyone.\n\n\n Thankyou \n Team Viasta Store";          
		mailMessage.setTo(user.getEmail());
		mailMessage.setSubject(subject);
		mailMessage.setText(message);
		
		javaMailSender.send(mailMessage);
	}
}
