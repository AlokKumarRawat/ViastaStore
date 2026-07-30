package com.project.Viastastore.Controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.Viastastore.Dto.CategoryDto;
import com.project.Viastastore.Dto.ProductDto;
import com.project.Viastastore.Model.Category;
import com.project.Viastastore.Model.Enquiry;
import com.project.Viastastore.Model.Orders;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.Products.ProductStatus;
import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Model.Users.UserRole;
import com.project.Viastastore.Model.Users.UserStatus;
import com.project.Viastastore.Repository.CategoryRepo;
import com.project.Viastastore.Repository.EnquiryRepo;
import com.project.Viastastore.Repository.OrderRepo;
import com.project.Viastastore.Repository.ProductRepo;
import com.project.Viastastore.Repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {
	
	@Autowired
	private HttpSession session;
	@Autowired
	private EnquiryRepo enquiryRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private CategoryRepo categoryRepo;
	@Autowired
	private ProductRepo productRepo;
	@Autowired
	private OrderRepo orderRepo;
	
	@GetMapping("/Dashboard")
	public String ShowDashboard(Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		model.addAttribute("totalProducts", productRepo.count());
	    model.addAttribute("totalOrders", orderRepo.count());
	    model.addAttribute("totalUsers", userRepo.count());

	    model.addAttribute("recentOrders",
	            orderRepo.findTop5ByOrderByOrderAtDesc());
		
		return "Admin/Dashboard";
	}
	
	@GetMapping("/ManageUsers")
	public String ShowManageUsers(@RequestParam(value="status",required=false) UserStatus status,Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		if(status==null) {
			List<Users> users=userRepo.findAllByRole(UserRole.User);
			model.addAttribute("users", users);
		}
		else {
			List<Users> users=userRepo.findAllByRoleAndStatus(UserRole.User,status);
			model.addAttribute("users", users);
		}
		return "Admin/ManageUsers";
	}
	
	@GetMapping("/UpdateUserStatus/{id}")
	public String UpdateUserStatus(@PathVariable("id") long id,RedirectAttributes attributes,HttpServletRequest request) {
		Users user=userRepo.findById(id).get();
		if(user.getStatus().equals(UserStatus.Verified)) {
			user.setStatus(UserStatus.Disabled);
		}
		else if(user.getStatus().equals(UserStatus.Disabled)) {
			user.setStatus(UserStatus.Verified);
		}
		userRepo.save(user);
		attributes.addFlashAttribute("msg", "User Status updated successfully");
		return "redirect:"+request.getHeader("referer");
	}
	
	@GetMapping("/ManageOrders")
	public String ShowManageOrders(Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		List<Orders> orders=orderRepo.findAll();
		model.addAttribute("orders", orders);
		
		return "Admin/ManageOrders";
	}
	
	@GetMapping("/AddCategory")
	public String ShoeAddCategory(Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		CategoryDto dto= new CategoryDto();
		model.addAttribute("dto", dto);
				
		List<Category> categories=categoryRepo.findAll();
		model.addAttribute("categories",categories);
		return "Admin/AddCategory";
	}
	
	@PostMapping("/AddCategory")
	public String AddCategoryValue(@ModelAttribute CategoryDto dto,RedirectAttributes attributes) {
		Category category=new Category();
		if(categoryRepo.existsByCategoryName(dto.getCategoryName())) {
			attributes.addFlashAttribute("msg", "Category already exist");
			return "redirect:/Admin/AddCategory";
		}
		category.setCategoryName(dto.getCategoryName());
		category.setCategoryIcon(dto.getCategoryIcon());
		category.setVisible(true);
		categoryRepo.save(category);
		attributes.addFlashAttribute("msg", "Category added successfully");
		return"redirect:/Admin/AddCategory";
	}
	
	@GetMapping("/AddProduct")
	public String ShowAddProduct(Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		ProductDto dto=new ProductDto();
		model.addAttribute("dto", dto);
		List<Category> categories=categoryRepo.findAllByIsVisible(true);
		model.addAttribute("categories", categories);
		return "Admin/AddProduct";
	}
	
	@PostMapping("/AddProduct")
	public String AddProduct(@ModelAttribute ProductDto dto,@RequestParam("images") MultipartFile images[],RedirectAttributes attributes) {
		if(session.getAttribute("loggedInAdmin")==null){
		    return "redirect:/login";
		}
		try {
			if(images.length<2) {
				attributes.addFlashAttribute("msg", "Please upload at least 2 images");
			}
			if(images.length>5) {
				attributes.addFlashAttribute("msg", "Please upload at max 5 images");
			}
			//Product image file uploading
			
			String uploadDir="Public/ProductImages/";
			File folder=new File(uploadDir);
			if(!folder.exists()) {
				folder.mkdirs();
			}
			List<String> productImages=new ArrayList<>();
			for(MultipartFile image : images) {
				String storageFileName= UUID.randomUUID()+"_"+image.getOriginalFilename();
				Path uploadPath=Paths.get(uploadDir,storageFileName);
				InputStream inputStream=image.getInputStream();
				Files.copy(inputStream, uploadPath, StandardCopyOption.REPLACE_EXISTING);
				productImages.add(storageFileName);
			}
			
			
			//Product data uploading
			
			Products products=new Products();
			products.setProductName(dto.getProductName());
			products.setProductDescription(dto.getProductDescription());
			products.setBrandName(dto.getBrandName());
			products.setCategory(dto.getCategory());
			products.setGender(dto.getGender());
			
			
			products.setCostPrice(dto.getCostPrice());
			products.setDiscount(dto.getDiscount());
			products.setSellingPrice(dto.getSellingPrice());
			
			double finalPrice= dto.getSellingPrice()-(dto.getSellingPrice()*(dto.getDiscount()/100.0));
			
			products.setFinalPrice(finalPrice);
			
			products.setColors(dto.getColors());
			products.setSizes(dto.getSizes());
			
			products.setQuantity(dto.getQuantity());
			products.setShippingCharge(dto.getShippingCharge());
			products.setDeliveryTime(dto.getDeliveryTime());
			products.setReturnPolicy(dto.isReturnPolicy());
			products.setVisiblity(true);
			products.setAddedAt(LocalDateTime.now());
			products.setStatus(ProductStatus.Available);
			products.setProductImages(productImages);
			
			productRepo.save(products);
			attributes.addFlashAttribute("msg","Product add successfully");
			
			
			
			
			
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Admin/AddProduct";
	}
	
	@GetMapping("/ManageProduct")
	public String ShowManageProduct(Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		List<Products> products=productRepo.findAll();
		model.addAttribute("products", products);
		
		return "Admin/ManageProduct";
	}
	
	@GetMapping("/ViewEnquiry")
	public String ShowViewEnquiry(Model model) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		List<Enquiry> enquiries = enquiryRepo.findAll();
		model.addAttribute("enquiries", enquiries);
		return "Admin/ViewEnquiry";
	}
	
	@GetMapping("/deleteEnquiry/{id}")
	public String DeleteEnquiry(@PathVariable long id) {
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/login";
		}
		enquiryRepo.deleteById(id);
		return "redirect:/Admin/ViewEnquiry";
	}
	
	@PostMapping("/Admin/update-order-status")
	public String updateOrderStatus(Long id,
	                                Orders.OrderStatus status) {

	    Orders order = orderRepo.findById(id).orElseThrow();

	    order.setOrderStatus(status);

	    if(status == Orders.OrderStatus.Delivered) {
	        order.setDeliveredAt(LocalDateTime.now());
	    }

	    if(status == Orders.OrderStatus.Cancelled) {
	        order.setCancelledAt(LocalDateTime.now());
	    }

	    orderRepo.save(order);

	    return "redirect:/Admin/manage-orders";
	}
	@GetMapping("/Admin/view-product/{id}")
	public String viewProduct(@PathVariable Long id, Model model) {

	    Products product = productRepo.findById(id).orElseThrow();
	    model.addAttribute("product", product);

	    return "Admin/viewProduct";
	}
	@GetMapping("/Admin/edit-product/{id}")
	public String editProduct(@PathVariable Long id, Model model) {

	    Products product = productRepo.findById(id).orElseThrow();
	    model.addAttribute("product", product);

	    return "Admin/editProduct";
	}
	@PostMapping("/Admin/delete-product/{id}")
	public String deleteProduct(@PathVariable Long id) {

	    productRepo.deleteById(id);

	    return "redirect:/Admin/manage-products";
	}
	
	@GetMapping("/logout")
	public String Logout() {
		session.removeAttribute("loggedInAdmin") ;
		return "redirect:/login";
	}
}
