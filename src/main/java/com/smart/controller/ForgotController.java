package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	Random random = new Random(1000);

	@Autowired
	private BCryptPasswordEncoder bcrypt;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService;

	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm(Model model) {
		model.addAttribute("title", "Forgot Email Form");

		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String semdOTP(@RequestParam("email") String email, HttpSession session) {

		System.out.println("EMAIL " + email);
		
//		we check email id  of user is exist or not 
		User user = this.userRepository.getUserByUserName(email);
		
		if(user==null) {
			session.setAttribute("message", " User does not exist with this email");
			return "forgot_email_form";
			
		}
		else {
			

		// generating otp of 4 digit

		int otp = random.nextInt(99999);
		System.out.println("OTP " + otp);

//		write code for send otp
		String subject = "OTP from Sart Contact Manager";
		String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<h1>" + "OTP is " + "<b>" + otp
				+ "</b>" + "</h1>" + "</div>";
		String to = email;
		
	

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {

			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);

			return "verify_otp";
		}

		else {

			session.setAttribute("message", "MSG not sent... try again ! ! ");
			return "forgot_email_form";
		}
		}

		
	}

//	verify OTP

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {

		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");

		if (myOtp == otp) {

			// password change form

			User user = this.userRepository.getUserByUserName(email);
			if (user == null) {

				// send error message

				session.setAttribute("message", " User does not exist with this email");
				return "forgot_email_form";

			} else {
				// send change Password form
				return "password_change_form";
			}

		} else {
			session.setAttribute("message", "You entered wrong OTP");
			return "verify_otp";
		}

	}

	// change password

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {

		String email = (String) session.getAttribute("email");

		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		session.setAttribute("message", "Password changed Successfully...Login with new Password");

		return "redirect:/signin?change=password changed successfully...";

	}
}
