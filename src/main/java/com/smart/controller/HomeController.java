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

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.dao.UserRepository;

@Controller
public class HomeController {
	/*
	@GetMapping("/test")
	@ResponseBody
	public String test() {
		User user =  new User();
		user.setName("asfa");
		user.setEmail("fasf@ga.com");
		Contact contact = new Contact();
		user.getContacts().add(contact);
		UserRepository.save(user);
		return "working";
		
	}
	*/
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	private UserRepository UserRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - smart Contact home manager");
		return "home";
	}
	
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - smart Contact home manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	// handler for register user
	
	@RequestMapping( value = "/do_register", method=RequestMethod.POST) 
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,

			@RequestParam(value = "agreement",defaultValue = "false") boolean agreement,
			Model model  ,HttpSession session) {
	
		try {
			
			if(!agreement) {
				System.out.println("You Have not agreed with term and conditions ");
				throw new Exception("You Have not agreed with term and conditions ");
			} 
			
			if(result1.hasErrors()) {
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
		
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println(agreement);
			System.out.println(user);
			User result = this.UserRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Successfully register  ","alert-success"));
			return "signup";

			
		}catch(Exception e){
		
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("something went wrong " + e.getMessage(),"alert-danger"));
			return "signup";

		}
		
		
		
	
	
	}

@GetMapping("/signin")
public String customLogin(Model model) {
	model.addAttribute("title","Login page");
	return "login";
	
}

	
}