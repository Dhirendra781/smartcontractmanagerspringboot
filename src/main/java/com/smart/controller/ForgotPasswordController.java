package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserDao;
import com.smart.entity.User;
import com.smart.service.EmailService;

@Controller
public class ForgotPasswordController {
	
	Random random = new Random(10000);
	
	@Autowired
		private EmailService emailService;
	
	@Autowired
		private UserDao userDao;
	
	@Autowired
		private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	//open email id form handler
	@RequestMapping("/forgot")
	public String emailFormHandler() {
		return "forgot_email_form";
	}
	
	
	@PostMapping("/sent-opt")
	public String sendOpt(@RequestParam("email") String email, HttpSession session) {
		
		//generating opt of 4 digit
		int opt = random.nextInt(9999);
		System.out.println("Opt "+opt);
		
		String message ="OPT From SCM";
		String subject=""
						+"<div style='border: 1px solid #e2e2e2; padding: 20px;'>"
						+"<h1>"
						+"OPT is "
						+"<b>"+opt
						+""
						+"</h1>"
						+"</div>";
		String to=email;
		
		boolean flag = this.emailService.sendEmail(message, subject, to);
		
		if(flag) {
			session.setAttribute("myopt", opt);
			session.setAttribute("email", email);
			return"verify-opt";
		}
		
		else {
			session.setAttribute("message", "check your email id...");
			return "forgot_email_form";

		}
		
	}
	
	//verify opt
	@PostMapping("/verify-opt")
	public String verifyOpt(@RequestParam("opt") int opt, HttpSession session) {
		
		int myOpt = (int) session.getAttribute("myopt");
		String email = (String) session.getAttribute("email");
		
		if(myOpt==opt) {
			//password change form
			User user = this.userDao.getUserByUserNmae(email);
			
			if(user == null) {
				//send error message
				session.setAttribute("message", "User does not exits with this email !!");
				return "forgot_email_form";
				
			}
			else {
				//send change password form
				
			}
			return"password_change_form";
		}
		else {
			session.setAttribute("message", "You entered wrong opt!!");
			return "verify-opt";

		}
		
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		
		String email = (String) session.getAttribute("email");
	    User user =	this.userDao.getUserByUserNmae(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
	    this.userDao.save(user);
		return "redirect:/signin/?change=password change successfully!!";
	}

}



