package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgetController {
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	Random random =	new Random(1000);
	// email id for open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session  )
	{
		System.out.println("Email" + "  " +email);
		// genetaring otp 4 digit 
		
	Random random =	new Random(1000);
	int otp =  random.nextInt(9999999);
	System.out.println(otp);	
	
	// here we have to write code for otp to Email
	String subject = "OTP from SMC";
	String message = "<div>"+ "<h1>"
					+"opt is "
					+otp+
					"</h1>"
					+"</div>";
	String to = email;
	
	boolean flag =  this.emailService.sendEmail(subject, message, to);
	if(flag) {
		session.setAttribute("myotp",otp);
		session.setAttribute("email",email);
		return "verify_otp";
	
	}
	else {
		session.setAttribute("message","check your emqail");
		return "forgot_email_form";
	}
	
	}
	
	//verify otp
	
	@PostMapping("verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
	{
		int myotp=(int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		if(myotp == otp) {
			
			User user= this.userRepository.getUserByUserName(email);
			// password is change
			if(user==null) {
				// send error message
				session.setAttribute("message","user does not exist");
				return "forgot_email_form";
			}else {
				// change password
			}
			
			return "password_change_form";
		}else {
			session.setAttribute("message","you have rnter wrong message");
			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newPassword,HttpSession session) {
		String email = (String) session.getAttribute("email");
		User user=this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully";
		
	}
}
