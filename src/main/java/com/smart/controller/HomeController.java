package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserDao;
import com.smart.entity.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;


	@RequestMapping(value="/")
	public String home(Model model) {
		model.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping(value="/about")
	public String about(Model model) {
		model.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping(value="/signup")
	public String signup(Model model) {
		model.addAttribute("title","Registration Smart Contact Manger");
		model.addAttribute("user", new User());
		return "registration";
	}

	@RequestMapping(value="/do_registration", method = RequestMethod.POST)
	public String doRegistration(@Valid @ModelAttribute("user") User user, BindingResult result, 
			@RequestParam(value="agreement", defaultValue = "false") boolean agreement, Model model,HttpSession session) {
		
		try {
			
			if(!agreement) {
				//System.out.println(" You have not agree terms and conditions");
				throw new Exception(" You have not agree terms and conditions");
			}
			
			if(result.hasErrors()) {
				System.out.println("result"+result.toString());
				model.addAttribute("user",user);
				return "registration";
			}
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.gpg");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("User Name:"+user);
			System.out.println("User Name:"+agreement);
			
			User saveUser = this.userDao.save(user);
			model.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Successfully Register!!", "alert-success"));
			return "registration";
			
			} catch(Exception e) {
				e.printStackTrace();
				model.addAttribute("user",user);
				session.setAttribute("message", new Message("Something went wrong"+ e.getMessage(), "alert-danger"));
				return "registration";
	
			}
			
		}
	
	//This is custom login handler method
	@GetMapping(value="/signin")
	public String login(Model model) {
		model.addAttribute("title","Login Page");
		return "login";
	}

	
}
