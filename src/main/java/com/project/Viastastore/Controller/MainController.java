package com.project.Viastastore.Controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.Viastastore.Dto.EnquiryDto;
import com.project.Viastastore.Dto.SavedAddressDto;
import com.project.Viastastore.Dto.UserDto;
import com.project.Viastastore.MailService.CartService;
import com.project.Viastastore.MailService.PaymentService;
import com.project.Viastastore.MailService.SendMailService;
import com.project.Viastastore.Model.Cart;
import com.project.Viastastore.Model.Category;
import com.project.Viastastore.Model.Enquiry;
import com.project.Viastastore.Model.Orders;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.SavedAddress;
import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Model.Users.UserRole;
import com.project.Viastastore.Model.Users.UserStatus;
import com.project.Viastastore.Repository.CartRepo;
import com.project.Viastastore.Repository.CategoryRepo;
import com.project.Viastastore.Repository.EnquiryRepo;
import com.project.Viastastore.Repository.OrderRepo;
import com.project.Viastastore.Repository.ProductRepo;
import com.project.Viastastore.Repository.SavedAddressRepo;
import com.project.Viastastore.Repository.UserRepo;

import jakarta.mail.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	@Autowired
	private EnquiryRepo enquiryRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private SendMailService sendMailService;
	@Autowired
	private CategoryRepo categoryRepo;
	@Autowired
	private ProductRepo productRepo;
	@Autowired
	private HttpSession session;
	@Autowired
	private CartRepo cartRepo;
	@Autowired
	private SavedAddressRepo addressRepo;
	@Autowired
	private CartService cartService;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private OrderRepo orderRepo;
	
	@GetMapping("/")
	public String showIndex(Model model) {
		List<Category> categories=categoryRepo.findAll();
		model.addAttribute("categories", categories);
		return "index";
	}
	
	@GetMapping("/about")
	public String showAbout() {
		return "about";
	}
	
	@GetMapping("/Register")
	public String ShowRegister(Model model) {
		UserDto dto =new UserDto();
		model.addAttribute("dto", dto);
		return "register";
	}
	
	//User Registration
	@PostMapping("/Register")
	public String Register(@ModelAttribute UserDto dto,RedirectAttributes attributes,HttpSession session) {
		try {
			if(userRepo.existsByEmail(dto.getEmail())) {
				attributes.addFlashAttribute("msg", "User already exists");
				return "redirect:/login";
			}
			Users user=new Users();
			user.setName(dto.getName());
			user.setContactNo(dto.getContactNo());
			user.setEmail(dto.getEmail());
			user.setGender(dto.getGender());
			user.setPassword(dto.getPassword());
			
			user.setRole(UserRole.User);
			user.setStatus(UserStatus.Unverified);
			user.setRegisterAt(LocalDateTime.now());
			
			
			SecureRandom secureRandom=new SecureRandom();
			
			String otp=100000+secureRandom.nextInt(900000)+"";
			user.setOtp(otp);
			user.setGeneratedAt(LocalDateTime.now());
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			
			userRepo.save(user);
			
			System.err.println(dto.getEmail()+" OTP: "+otp);
			attributes.addFlashAttribute("msg", "Registration Successfull.OTP is sended to you by gmail,please veriy it");
			session.setAttribute("email", user.getEmail());
			
			sendMailService.sendOtpMail(user);
			
			return "redirect:/verify-otp";
			
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Register";
	}
	
	@GetMapping("/verify-otp")
	public String showVerifyOtp(HttpSession session) {
		if(session.getAttribute("email")==null) {
			return "redirect:/Register";
		}
		return "verify-otp";
	}
	
	@PostMapping("/verify-otp")
	public String VerifyOtp(@RequestParam("otp") String otp, RedirectAttributes attributes, HttpSession session) {
		try {
			String email=(String) session.getAttribute("email");
			Users user= userRepo.findByEmail(email);
			if(!otp.equals(user.getOtp())) {
				attributes.addFlashAttribute("msg", "invalid otp");
				return "redirect:/verify-otp";
			}
			long minutes=ChronoUnit.MINUTES.between(user.getGeneratedAt(),LocalDateTime.now());
			if(minutes>5) {
				attributes.addFlashAttribute("msg","Otp expired");
				return "redirect:/verify-otp";
			}
			user.setStatus(UserStatus.Verified);
			userRepo.save(user);
			attributes.addFlashAttribute("msg", "Registration Completed!!!");
			return "redirect:/login";
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/verify-otp";
	}
	
	@GetMapping("/resend-otp")
	public String resendOtp(RedirectAttributes attributes,HttpSession session) {
		try {
			SecureRandom secureRandom=new SecureRandom();
			String otp=100000+secureRandom.nextInt(900000)+"";
			
			String email=(String) session.getAttribute("email");
			Users user=userRepo.findByEmail(email);
			user.setOtp(otp);
			user.setGeneratedAt(LocalDateTime.now());
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			userRepo.save(user);
			
			System.err.println("Resend OTP: "+otp);
			attributes.addFlashAttribute("msg", "OTP is resent to you by gmail");
			sendMailService.sendOtpMail(user);
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong");
		}
		return "redirect:/verify-otp";
	}
	
	@GetMapping("/ContactUs")
	public String showContactUs(Model model) {
		EnquiryDto dto=new EnquiryDto();
		model.addAttribute("dto", dto);
		return "contact";
	}
	
	@PostMapping("/SubmitEnquiry")
	public String SubmitEnquiry(@ModelAttribute EnquiryDto dto,RedirectAttributes attributes) {
		try {
			Enquiry enquiry =new Enquiry();
			enquiry.setName(dto.getName());
			enquiry.setEmail(dto.getEmail());
			enquiry.setContactNo(dto.getContactNo());
			enquiry.setAddress(dto.getAddress());
			enquiry.setEnquiryType(dto.getEnquiryType());
			enquiry.setMessage(dto.getMessage());
			enquiry.setEnquiryAt(LocalDateTime.now());
			
			enquiryRepo.save(enquiry);
			
			attributes.addFlashAttribute("msg", "Enquiry Successfully Submitted. We will contact you soon.");
			
			
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong");
			System.err.println("Error : "+e.getMessage());
		}
		return "redirect:/ContactUs";
	}
	@GetMapping("/login")
	public String ShowLogin() {
		return "login";
	}
	
	@PostMapping("/login")
	public String Login(HttpServletRequest request,RedirectAttributes attributes,HttpSession session) {
		try {
			String email=request.getParameter("email");
			String password=request.getParameter("password");
			
			if(!userRepo.existsByEmail(email)) {
				attributes.addFlashAttribute("msg", "User does not exist.");
				return "redirect:/login";
			}
			
			Users user=userRepo.findByEmail(email);
			if((user.getEmail().equals(email)) && (user.getPassword().equals(password))) {
				if(user.getRole().equals(UserRole.User)) {
					if(user.getStatus().equals(UserStatus.Unverified)) {
						sendMailService.sendOtpMail(user);
						session.setAttribute(email, user.getEmail());
						return "redirect:/verify-otp";
					}
					else if(user.getStatus().equals(UserStatus.Disabled)){
						attributes.addFlashAttribute("msg", "Account disabled, Please contact administrator!!!");
						return "redirect:/login";
					}
					else {
						session.setAttribute("loggedInUser", user);
						return "redirect:/";
					}
					
				}
				else if(user.getRole().equals(UserRole.Admin)){
					session.setAttribute("loggedInAdmin", user);
					return "redirect:/Admin/Dashboard";
				}
			}
			else {
				attributes.addFlashAttribute("msg", "Invalid User");
			}
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg",e.getMessage() );
		}
		return "redirect:/login";
	}
	
	@GetMapping("/shop")
	public String showShop(
	        @RequestParam(value = "id", required = false) List<Long> ids,
	        @RequestParam(value = "price", required = false) Double price,
	        @RequestParam(value = "stock", required = false) Boolean stock,
	        @RequestParam(value = "sort", required = false) String sort,
	        Model model) {

	    model.addAttribute("categories", categoryRepo.findAllByIsVisible(true));

	    List<Products> products = productRepo.findAll();

	    // Category Filter
	    if (ids != null && !ids.isEmpty()) {
	        products = products.stream()
	                .filter(p -> ids.contains(p.getCategory().getId()))
	                .toList();
	    }

	    // Price Filter
	    if (price != null) {
	        products = products.stream()
	                .filter(p -> p.getFinalPrice() <= price)
	                .toList();
	    }

	    // Stock Filter
	    if (Boolean.TRUE.equals(stock)) {
	        products = products.stream()
	                .filter(p -> p.getQuantity() > 0)
	                .toList();
	    }

	    // Sorting
	    if ("low".equals(sort)) {
	        products = products.stream()
	                .sorted(Comparator.comparingDouble(Products::getFinalPrice))
	                .toList();
	    } else if ("high".equals(sort)) {
	        products = products.stream()
	                .sorted(Comparator.comparingDouble(Products::getFinalPrice).reversed())
	                .toList();
	    } else if ("new".equals(sort)) {
	        products = products.stream()
	                .sorted(Comparator.comparing(Products::getAddedAt).reversed())
	                .toList();
	    }

	    model.addAttribute("products", products);

	    return "Shop";
	}
	
	@GetMapping("/find/{value}")
	public String search(@PathVariable("value") String value,Model model) {
		List<Products> products=productRepo.findAllByProductNameContainingOrBrandNameContainingOrProductDescriptionContainingAllIgnoreCase(value,value,value);
		model.addAttribute("products",products);
		return "shop";
	}
	
	@GetMapping("/product/{id}")
	public String ShowProduct(@PathVariable("id") Long id,Model model) {
		Products product=productRepo.findById(id).get();
		model.addAttribute("product", product);
		return "product";
	}
	
	@PostMapping("/addToCart")
	public String addToCart(HttpServletRequest request,
	                        HttpSession session,
	                        RedirectAttributes redirectAttributes) {

		String color = request.getParameter("color");
		String size = request.getParameter("size");
		long productId = Long.parseLong(request.getParameter("productId"));
		
	    Users user = (Users) session.getAttribute("loggedInUser");

	    
	    if (user == null) {
	        return "redirect:/login";
	    }

	    try {

	        cartService.addToCart(color,size,1, user, productId);

	        redirectAttributes.addFlashAttribute(
	                "success",
	                "Product added to cart successfully.");

	    } catch (RuntimeException e) {

	        redirectAttributes.addFlashAttribute(
	                "error",
	                e.getMessage());

	    }

	    return "redirect:/product/" + productId;

	}
	
	
	@PostMapping("/cart/increase/{id}")
	public String increase(@PathVariable Long id) {

	    cartService.increaseQuantity(id);

	    return "redirect:/cart";
	}
	
	@PostMapping("/cart/decrease/{id}")
	public String decrease(@PathVariable Long id) {

	    cartService.decreaseQuantity(id);

	    return "redirect:/cart";
	}
	
	@GetMapping("/cart/remove/{id}")
	public String remove(@PathVariable Long id){

	    cartRepo.deleteById(id);

	    return "redirect:/cart";

	}
	
	@GetMapping("/savedAddress")
	public String ShowSavedAddress(Model model) {
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:/login";
		}
		SavedAddressDto dto=new SavedAddressDto();
		model.addAttribute("dto", dto);
		
		
		Users user =(Users) session.getAttribute("loggedInUser");
		List<SavedAddress> addresses=addressRepo.findAllByUser(user);
		model.addAttribute("addresses", addresses);
		return "SavedAddress";
	}
	
	@PostMapping("/savedAddress")
	public String SaveAddress(@ModelAttribute SavedAddressDto dto,RedirectAttributes attributes) {
		try {
				SavedAddress address=new SavedAddress();
				address.setAddress(dto.getAddress());
				address.setContactNo(dto.getContactNo());
				address.setCustomerName(dto.getCustomerName());
				address.setPincode(dto.getAddress());
				
				Users user=(Users) session.getAttribute("loggedInUser");
				address.setUser(user);
			
				List<SavedAddress> addresses=addressRepo.findAllByActiveAndUser(true,user);
				for(SavedAddress address2:addresses) {
					address2.setActive(false);
					addressRepo.save(address2);
				}
				address.setActive(true);
				addressRepo.save(address);
				attributes.addFlashAttribute("msg", "Address add successfully");
			
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/savedAddress";
	}
	
	@GetMapping("/savedAddress/update/{id}")
	public String updateAddress(@PathVariable("id") long id) {
		Users user=(Users) session.getAttribute("loggedInUser");
		List<SavedAddress> addresses=addressRepo.findAllByUser(user);
		for(SavedAddress address:addresses) {
			if(address.getId()==id) {
				address.setActive(true);
				addressRepo.save(address);
			}
			else {
				address.setActive(false);
				addressRepo.save(address);
			}
		}
		return "redirect:/savedAddress";
	}
	
	@GetMapping("/blog")
	public String showBlog() {
		return "blog";
	}
	
	@GetMapping("/cart")
	public String showBag(Model model) {
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:/login";
		}
		Users user=(Users) session.getAttribute("loggedInUser");
		List<Cart> cartItem =cartRepo.findAllByUser(user);
		
		model.addAttribute("cartItem", cartItem);
		
		
		double subtotal = 0;
		double discount = 0;
		double shipping = 0;

		for (Cart cart : cartItem) {
		    subtotal += cart.getProduct().getSellingPrice() * cart.getQuantity();

		    discount += (cart.getProduct().getSellingPrice()
		            - cart.getProduct().getFinalPrice()) * cart.getQuantity();
		}

		double grandTotal = subtotal - discount + shipping;

		model.addAttribute("subtotal", subtotal);
		model.addAttribute("discount", discount);
		model.addAttribute("shipping", shipping);
		model.addAttribute("grandTotal", grandTotal);
		
		return "cart";
	}
	@GetMapping("/payment/create")
	@ResponseBody
	public Map<String, Object> createPayment(HttpSession session) throws Exception {
		

		return paymentService.createPayment(session);

	}

	@PostMapping("/payment/verify")
	@ResponseBody
	public String verifyPayment(@RequestParam String paymentId, @RequestParam String orderId,
			@RequestParam String signature, HttpSession session) throws Exception {
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:/login";
		}

		return paymentService.verifyPayment(paymentId, orderId, signature, session);

	}
	@PostMapping("/placeOrder")
	@ResponseBody
	public String placeOrder(@RequestParam Long addressId,
	                         HttpSession session) {
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:/login";
		}

	    return paymentService.saveOrder(session, addressId, "COD");
	}
	
	@GetMapping("/wishlist")
	public String showWishlist(Model model) {
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:/login";
		}
		
		
		return "wishlist";
	}
	@GetMapping("/MyOrders")
	public String ShowOrders(Model model) {
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:/login";
		}
		
		Users user=(Users) session.getAttribute("loggedInUser");
		List<Orders> orders=orderRepo.findAllByUser(user);
		model.addAttribute("orders", orders);
		return "Order";
	}
	
	@GetMapping("/checkout")
	public String checkout(HttpSession session, Model model) {
		
	    Users user = (Users) session.getAttribute("loggedInUser");

	    List<SavedAddress> addresses =
	            addressRepo.findAllByUser(user);
	    
	    List<Cart> cartItem=cartRepo.findAllByUser(user);

	    model.addAttribute("addresses", addresses);
	    
	    double subtotal=0;
	    double discount=0;
	    double shipping=0;
	    
	    for (Cart cart : cartItem) {
		    subtotal += cart.getProduct().getSellingPrice() * cart.getQuantity();
		    

		    discount += (cart.getProduct().getSellingPrice()
		            - cart.getProduct().getFinalPrice()) * cart.getQuantity();
		}

		double grandTotal = subtotal - discount + shipping;
	    

	    model.addAttribute("subtotal",grandTotal);
	    model.addAttribute("items", cartItem.size());

	    return "checkout";
	}
	
	@GetMapping("/order/{id}")
	public String viewOrder(@PathVariable("id") long id, Model model) {

	    Orders order = orderRepo.findById(id).orElseThrow();

	    model.addAttribute("order", order);

	    return "viewOrder";
	}
	
	@PostMapping("/cancel-order/{id}")
	public String cancelOrder(@PathVariable Long id) {

	    Orders order = orderRepo.findById(id).orElseThrow();

	    // Don't cancel if already shipped
	    if (order.getOrderStatus() == Orders.OrderStatus.Processing ||
	        order.getOrderStatus() == Orders.OrderStatus.Confirmed) {

	        // Send cancellation request to admin
	        order.setOrderStatus(Orders.OrderStatus.Cancelled);
	        order.setCancelledAt(LocalDateTime.now());

	        orderRepo.save(order);
	    }

	    return "redirect:/order/" + id;
	}
	
	@GetMapping("/privacy")
	public String showPrivacy() {
		return "privacy";
	}
	
	@GetMapping("/faq")
	public String showFAQ() {
		return "faq";
	}
	
	@GetMapping("/terms-and-condition")
	public String showTAC() {
		return "tc";
	}
	
	@GetMapping("/shipping")
	public String showShipping() {
		return "shipping";
	}
	
	@GetMapping("/return-policy")
	public String showReturn() {
		return "return-policy";
	}
	@GetMapping("/logout")
	public String Logout() {
		session.removeAttribute("loggedInUser");
		return "redirect:/login";
	}

}
