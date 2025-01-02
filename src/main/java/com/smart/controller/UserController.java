package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
//	method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println( "Username" +userName);
		
		User user = this.userRepository.getUserByUserName(userName);
		
		System.out.println(user);
		
		model.addAttribute("user",user);
		
		
	}
	
	
//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		
		
		return "normal/user_dashboard";
	}
	
//	open add form handler
	
	@GetMapping("/add-contact")
	public String openContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact",new Contact());
		
		
		return "normal/add_contact_form";
	}
	
	
//	processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage")MultipartFile file, 
			Principal principal, HttpSession session 
			) {
		
		
		try {
			
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading image file
	
		
		if(file.isEmpty()) {
			System.out.println(" File is Empty");
			contact.setImage("contact.png");
		}
		
		else {
			//save the file to folder and update the name
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/image").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DATA"+contact);
		
		System.out.println("added to data base");
		
		//success message(notification)
		session.setAttribute("message" ,new Message("your Contact is addded ! ! Add More", "success" ));
		
		
		
		
		}
		
		catch(Exception e) {
			System.out.println("Error" +e.getMessage());
			e.printStackTrace();
			
			//error message(notification)
			session.setAttribute("message" ,new Message("Something went wrong ! ! try again", "danger" ));

			
		}
		
		
		return "normal/add_contact_form";
	}
	
	
	//view contact handler
	//per page 5 contact=5[n]
	//current page= 0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "show contact");
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
//		List<Contact> contacts = user.getContacts();
		
	     Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		
		
		return "normal/show_contact";
	}
	
	
//	showing particular contact details
	
	@RequestMapping("/contact/{cId}")
	public String showContactDetails( @PathVariable("cId") Integer cId ,Model model,Principal principal) {
		
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());

		}
		
		return "normal/contact_details";
	}
	
	
	//delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session , HttpServletRequest request) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		
		
		this.contactRepository.delete(contact);
		
		session.setAttribute("message", new Message("contact delete successfully...", "success"));
		
		 String pageParam = request.getParameter("currentPage");
		    int currentPage = pageParam != null ? Integer.parseInt(pageParam) : 0;
		    
		    System.out.println("Current Page: " + currentPage);
		    
		
		return "redirect:/user/show-contacts/"+ currentPage;
	}
	
	
//	update contact
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm( @PathVariable("cid") Integer cid, Model m) {
		
		m.addAttribute("title", "update contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact",contact);
		
		
		return "normal/update_form";
	}
	
	
//	update  contact form handler
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage")MultipartFile file,
			Model m, HttpSession session,
			Principal principal) {
		
		
		try {
			
			//old contact details
			
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image
			if(!file.isEmpty()) {
				
//				delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
               File file1= new File(deleteFile, oldContactDetail.getImage());
               file1.delete();
				
				
				
				
				//update new photo
				
				File saveFile = new ClassPathResource("static/image").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
				
				
			}
			else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println("Contact Name : "+ contact.getName());
		System.out.println("Contact ID : "+ contact.getcId());

		return "redirect:/user/contact/"+contact.getcId();
	}
	
	
	
	//your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		
		return "normal/profile" ;
		
	}
	
	@GetMapping("/setting")
	public String setting(Model model) {
		
		model.addAttribute("title","Settings");	
		
		return "normal/setting";
	}
	
	
//	change password handler
	@PostMapping("/change-password")
	public String changePassword( @RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword,
			@RequestParam("confirmNewPassword") String confirmNewPassword,
			Principal principal,
			HttpSession session) {
		
		System.out.println("OLD PASSWORD: "+oldPassword);
		System.out.println("NEW PASSWORD: "+newPassword);
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		String pass=user.getPassword();
		System.out.println(pass);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, pass) && newPassword.equalsIgnoreCase(confirmNewPassword)  ) {
			
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Password change Successfilly... plesae Login Again..", "success"));	

		}
		else {
			
			session.setAttribute("message", new Message("Password Not Matching...", "danger"));
			
			return "redirect:/user/setting";
			
		}
		
		return "redirect:/user/index";	
			
	}
	
	//create order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data) {
		System.out.println("Hey order executed ");
		System.out.println(data);
		return "done";
	}
	
	
}
