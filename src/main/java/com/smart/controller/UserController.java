package com.smart.controller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import java.util.*;

import com.razorpay.*;
@Controller
@RequestMapping("/user")

public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userrepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository MyOrderRepository;
	
	//method for adding common data for response
@ModelAttribute
public void addCommonData(Model model, Principal principal) {
	String userName = principal.getName();
	User user = userrepository.getUserByUserName(userName);
	System.out.println(user);
	model.addAttribute("user",user);
	System.out.println(userName);
	
	
}
//dashboard home
@RequestMapping("/index")
public String dashboard(Model model, Principal principal) {
//	String userName = principal.getName();
//	User user = userrepository.getUserByUserName(userName);
//	System.out.println(user);
//	model.addAttribute("user",user);
//	System.out.println(userName);

// get the username using username(email)
model.addAttribute("title","User Dashboard");
return "normal/user_dashboard";
}

//open add form handler
@GetMapping("/add-contact")
public  String openAddContactForm(Model model) {
	model.addAttribute("ttile","Add Contact");
model.addAttribute("contact",new Contact());
return "normal/add_contact_form";
}

// process add contact form
@PostMapping("/process-contact")
public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
try {
String name = principal.getName();
User user = this.userrepository.getUserByUserName(name);

//processing and uploading files

if(file.isEmpty() ) {
	// if file is emply
System.out.println("File is emplyt");
contact.setImage("contact.png");
}
else {
	// upload the file to folder and update the name to contact
contact.setImage(file.getOriginalFilename());
File saveFile = new ClassPathResource("static/img").getFile();
Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
System.out.println("Image is uploaded");
}
contact.setUser(user);
user.getContacts().add(contact);
user.getContacts().add(contact);
this.userrepository.save(user);
System.out.println(contact);
System.out.println("added to data base");
//message success .....
session.setAttribute("message", new Message("Your contact is added || add more...", "success"));
}catch (Exception e) {
	// TODO: handle exception
System.out.println("Error " + e.getMessage());
e.printStackTrace();
//error message ......

session.setAttribute("message", new Message("Some thing went wrong", "danger"));
}
return "normal/add_contact_form";

}


// show contact handler
@GetMapping("/show-contacts/{page}")
public String ShowContact(@PathVariable("page") Integer page, Model m,Principal principal) {
	// per page contact = 5
	// current page contact = 0 [page]
	m.addAttribute("title","show User Contacts");

	String userName =principal.getName(); 
	User user =	this.userrepository.getUserByUserName(userName);
Pageable pageble = PageRequest.of(page, 5);
	//List<Contact> contacts=user.getContacts(); we can user 
	Page<Contact> contacts= this.contactRepository.findContactByUser(user.getId(),pageble);
	m.addAttribute("contacts",contacts);
	m.addAttribute("currentPage",page);
	m.addAttribute("totalPages",contacts.getTotalPages());
	return "normal/show_contacts";
}

//showing particular contact details
@RequestMapping("/{cId}/contact")
public String showContactDetails(@PathVariable("cId") Integer cId,Model model,Principal principal ) {
	System.out.println(cId);
	Optional<Contact> contactoptional =this.contactRepository.findById(cId);
	Contact contact = contactoptional.get();
	//for restricitonf useis
	 String userName = principal.getName();
	 User user =  this.userrepository.getUserByUserName(userName);
	 if(user.getId() == contact.getUser().getId()) 
	 {
		 model.addAttribute("contact",contact);
		 model.addAttribute("title",contact.getName());
	 }
	 
	 //model.addAttribute("contact",contact);
	return "normal/contact_detail";
}
//deleting contact handler
@GetMapping("/delete/{cid}")
public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal) {
	Optional<Contact> contactoptional= this.contactRepository.findById(cId);
	Contact contact = contactoptional.get();
	//contact.setUser(null); // see in video 26 10 sec
	//check.. for bug
	// remove image also
	// contact.getImage()
	//this.contactRepository.delete(contact);
	 User user=this.userrepository.getUserByUserName(principal.getName());
	user.getContacts().remove(contact);
	this.userrepository.save(user);
	 
	session.setAttribute("message", new Message("Contact deletesuccess fully...", "success"));
	return "redirect:/user/show-contacts/0";
}

// open update from handler

@PostMapping("/update-contact/{cid}")
public String updateForm(@PathVariable("cid") Integer cid,Model m) {
	m.addAttribute("title","Update COntact");
	
	Contact contact =this.contactRepository.findById(cid).get();
	m.addAttribute("contact",contact);
	
	return "normal/update_form";
}
// update contact handler
@RequestMapping(value = "/process-update",method = RequestMethod.POST)
public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
	
	try {
		
		// image..
		// get old contact details
		Contact oldcontactdetails = this.contactRepository.findById(contact.getcId()).get();
		if(!file.isEmpty()) {
			// file work
				//trewrite
			//delete old photo
			File deleteFile = new ClassPathResource("static/img").getFile();
			File file1 = new File(deleteFile,oldcontactdetails.getImage());
			file1.delete();
			
			//update new photo
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			contact.setImage(file.getOriginalFilename());
			System.out.println("Image is uploaded");
		}else {
			contact.setImage(oldcontactdetails.getImage());
		}
		User user = this.userrepository.getUserByUserName(principal.getName());
		contact.setUser(user);
		this.contactRepository.save(contact);
		session.setAttribute("message", new Message("Your contact is updated...","success"));
		}catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
	}
	
	
	System.out.println("Contact " + contact.getName());
	System.out.println(contact.getcId());
	this.contactRepository.save(contact);
	return "redirect:/user/"+contact.getcId()+"/contact";
}

//your profile handler
@GetMapping("/profile")
public String yourProfile(Model model) {
	model.addAttribute("title","profile section");
	return "normal/profile";
}

// open setting handler

@GetMapping("/settings")
public String openSetting() {
	
	return "normal/settings";
}


//change password

@PostMapping("/change-password")
public String changepassword(@RequestParam("oldPassword") String oldPassword , @RequestParam("newPassword") String newPassword , Principal principal,HttpSession session) {
	System.out.println("oldPassword" + oldPassword);
	System.out.println("newPassword" + newPassword);
    String userName = 	principal.getName();
	User currentUser =  this.userrepository.getUserByUserName(userName);
	System.out.println(currentUser.getPassword());
	
	// checking op == np
	
	if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
		
		// change the password
		currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userrepository.save(currentUser);
		session.setAttribute("message", new Message("your password is changed..","success"));
		
	}else {
		// error

		session.setAttribute("message", new Message("your password is wrong..","danger"));
		return "redirect:/user/settings";
	}
	return "redirect:/user/index";

}

	// creating order ofr payment
@PostMapping("/create_order")
@ResponseBody
public String createOrder(@RequestBody  Map<String, Object>data,Principal principal ) throws Exception {
	//System.out.println("order is executed");
	
	System.out.println(data);// confirming data is reaching on the server
	int amt=Integer.parseInt(data.get("amount").toString());
	var client = new RazorpayClient("rzp_test_0dXwwWQU0bQ1iv","HguTMSajGUko4jnSMPda0Iea");
	JSONObject options = new JSONObject();
	options.put("amount", amt*100);
	options.put("currency", "INR");
	options.put("receipt", "txn_123456");
	//creeating new order
	Order order = client.Orders.create(options);
	System.out.println(order);
	// if you want u can save to ur db
	
	MyOrder myOrder= new MyOrder();
	myOrder.setAmount(order.get("amount")+"");
	myOrder.setOrderId(order.get("id"));
	myOrder.setPaymentId(null);
	myOrder.setStatus("created");
	myOrder.setUser(this.userrepository.getUserByUserName(principal.getName()));  //  getting current user
	myOrder.setRecipt(order.get("receipt"));
	this.MyOrderRepository.save(myOrder);
	
	//return "done";
	return order.toString();
}
	
@PostMapping("/update_order")
public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {
	
	MyOrder myorder = this.MyOrderRepository.findByorderId(data.get("order_id").toString());
	myorder.setPaymentId(data.get("payment_id").toString());
	myorder.setStatus(data.get("status").toString());
	this.MyOrderRepository.save(myorder);
	System.out.println(data);
	return ResponseEntity.ok(Map.of("msg","updated"));
}
}
