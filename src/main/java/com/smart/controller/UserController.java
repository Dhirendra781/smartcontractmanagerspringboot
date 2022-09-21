package com.smart.controller;


import java.io.File;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.smart.dao.ContactDao;
import com.smart.dao.UserDao;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	
	@Autowired
		private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
		private UserDao userDao;
	
	@Autowired
		private ContactDao contactDao;
	
	@ModelAttribute()
	public void addCommmonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("UserName "+userName);
		
		//get user using by username(email)
		User user = userDao.getUserByUserNmae(userName);
		model.addAttribute("user",user);		
	}
	
	@RequestMapping(value="/index")
	public String userDashboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String addContact(Model model) {
		model.addAttribute("title","Add-Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add-contact-form";
	}
	
	//Processing form contact
	@PostMapping("/sava-contact")
	public String saveContact(@ModelAttribute("contact") Contact contact, BindingResult result, Principal principal,
			HttpSession session,@RequestParam("image") MultipartFile file) {

		
		try {
			String name = principal.getName();
			User user = this.userDao.getUserByUserNmae(name);
			
			//processing and uploding file
			if(file.isEmpty()) {
				//if file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("contact-logo.png");
			}
			
			else {
				//file the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/images").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File is uploaded");
			}
			
			contact.setUser(user);
			
			user.getContact().add(contact);
			this.userDao.save(user);
			//Success message
			session.setAttribute("message", new Message("Your contact is added","success"));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			//Error Message
			session.setAttribute("message", new Message("Something went woring !! Try again..","danger"));
		}
		
		return "normal/add-contact-form";
	}
	
	//Show contact
	@GetMapping("/show-contacts/{page}")
	public String showContact(Model model, Principal principal, @PathVariable("page") Integer page) {
		//Contact ki list ko bhejni hai
		
		String userName = principal.getName();
		User user = this.userDao.getUserByUserNmae(userName);
		
		//Current page
		//Contact per page 5
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactDao.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("title","Show User Contacts");
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPage",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//Showing Particular contact deteails
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID"+cId);
		
		Optional<Contact> contactOptional = this.contactDao.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userDao.getUserByUserNmae(userName);
		
		if(user.getId()== contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_details";
	}
	
	//Delete Contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session,
			Principal principal) {
		
		Contact contact = contactDao.findById(cId).get();
		
		User user = this.userDao.getUserByUserNmae(principal.getName());
		user.getContact().remove(contact);
		
		this.userDao.save(user);
		
		session.setAttribute("message", new Message("Contact Delete Successfully","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	//Open Update Contact form
	@PostMapping("/update-contact/{cId}")
	public String openContact(@PathVariable("cId") Integer cId, Model model) {
		Contact contact = contactDao.findById(cId).get();
		model.addAttribute("contact",contact);
		model.addAttribute("title","Update Contact");
		
		return "normal/update_form";
	}
	
	//Update Contact handler
	@RequestMapping(value="/process-update", method = RequestMethod.POST )
	public String updateContact(@ModelAttribute Contact contact, BindingResult result,
			@RequestParam("image") MultipartFile file, 
			Model model, HttpSession session, Principal principal) {
	
		try {
			//old contact details
			Contact oldContactDetails = this.contactDao.findById(contact.getcId()).get();
			
			//image
			if(!file.isEmpty()) {
				
				//delete old image
				File deleteFile = new ClassPathResource("static/images").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
				
				//update image
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
			}
			else {
				contact.setImage(oldContactDetails.getImage());
			}
			
			
			User user = this.userDao.getUserByUserNmae(principal.getName());
			contact.setUser(user);
			this.contactDao.save(contact);
			session.setAttribute("message", new Message("Your contact is updated..","success"));
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Update name"+contact.getName());
		System.out.println("Update ID"+contact.getcId());
		model.addAttribute("contact", contact);
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile page");
		return "normal/profile";
	}
	
	//open setting form for change password
	@GetMapping("/settings")
	public String settingForm() {
		return "normal/setting-form";
	}

	//Change password save
	@PostMapping("/change-password")
	public String saveChangePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal,HttpSession session) {
	
		String userName = principal.getName();
		User currentUser = this.userDao.getUserByUserNmae(userName);
		
		/*System.out.println("OLD Password"+oldPassword);
		System.out.println("NEW Password"+newPassword);*/
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userDao.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully change","success"));
		}
		
		else {
			session.setAttribute("message", new Message("Please Enter correct old password","danger"));
			return "redirect:/user/settings";
			
		}
		
		return "redirect:/user/index";
	}
	
	
}



