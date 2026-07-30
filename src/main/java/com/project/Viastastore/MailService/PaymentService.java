package com.project.Viastastore.MailService;

import java.time.LocalDateTime; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Viastastore.Model.Cart;
import com.project.Viastastore.Model.Orders;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.SavedAddress;
import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Repository.CartRepo;
import com.project.Viastastore.Repository.OrderRepo;
import com.project.Viastastore.Repository.ProductRepo;
import com.project.Viastastore.Repository.SavedAddressRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class PaymentService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private OrderRepo ordersRepo;

	@Autowired
	private SavedAddressRepo savedAddressRepo;

	
	private RazorpayClient razorpayClient;

	public PaymentService() throws Exception {
		this.razorpayClient = new RazorpayClient("rzp_live_Io1s9ctQtD0G1b", "HO60ThPu65xyvH7ewH5eVcWp");
	}

	public Order createRazorpayOrder(int amount) throws Exception {
		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", String.valueOf(amount * 100));
		orderRequest.put("currency", "INR");
		orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
		return razorpayClient.orders.create(orderRequest);
	}

	public Map<String, Object> createPayment(HttpSession session) throws Exception {

		Users user = (Users) session.getAttribute("loggedInUser");

		List<Cart> carts = cartRepo.findAllByUser(user);

		double total = 0;

		for (Cart cart : carts) {

			total += cart.getFinalPrice();

			total += cart.getProduct().getShippingCharge();

		}

		Order order = createRazorpayOrder((int) total);

		Map<String, Object> map = new HashMap<>();

		map.put("key", "rzp_live_Io1s9ctQtD0G1b");

		map.put("orderId", order.get("id"));

		map.put("amount", order.get("amount"));

		map.put("name", user.getName());

		map.put("email", user.getEmail());

		map.put("contact", user.getContactNo());

		return map;

	}

	@Transactional
	public String verifyPayment(String paymentId, String razorOrderId, String signature, HttpSession session)
			throws Exception {

		try {
			JSONObject json = new JSONObject();

			json.put("razorpay_order_id", razorOrderId);

			json.put("razorpay_payment_id", paymentId);

			json.put("razorpay_signature", signature);
			
			System.out.println("Payment Id : " + paymentId);
			System.out.println("Order Id : " + razorOrderId);
			System.out.println("Signature : " + signature);

			boolean valid = Utils.verifyPaymentSignature(json, "HO60ThPu65xyvH7ewH5eVcWp");
			
			System.out.println("Signature Valid : " + valid);

			if (!valid) {

				return "failed";

			}

			Users user = (Users) session.getAttribute("loggedInUser");

			SavedAddress address = savedAddressRepo.findByUserAndActiveTrue(user);
			System.out.println(address);

			List<Cart> carts = cartRepo.findAllByUser(user);

			for (Cart cart : carts) {

				Products product = cart.getProduct();

				if (product.getQuantity() < cart.getQuantity()) {

					return "Stock Not Available";

				}

				Orders order = new Orders();

				order.setProduct(product);

				order.setUser(user);

				order.setProductName(product.getProductName());

				order.setDescription(product.getProductDescription());

				order.setProductPrice(product.getSellingPrice());

				order.setDiscount(product.getDiscount());

				order.setFinalPrice(product.getFinalPrice());

				order.setShippingCharge(product.getShippingCharge());

				order.setQuantity(cart.getQuantity());

				order.setColor(cart.getColor());

				order.setSize(cart.getSize());

				order.setTotalAmount(cart.getFinalPrice() + product.getShippingCharge());

				order.setCustomerName(address.getCustomerName());

				order.setContactNo(address.getContactNo());

				order.setShippingAddress(address.getAddress());

				order.setPincode(address.getPincode());

				order.setOrderId(razorOrderId);

				order.setPaymentId(paymentId);

				order.setPaymentSignature(signature);

				order.setOrderStatus(com.project.Viastastore.Model.Orders.OrderStatus.Confirmed);

				order.setPaymentStatus(com.project.Viastastore.Model.Orders.PaymentStatus.Success);

				order.setOrderAt(LocalDateTime.now());

				ordersRepo.save(order);

				product.setQuantity(product.getQuantity() - cart.getQuantity());

				productRepo.save(product);

			}

			cartRepo.deleteAll(carts);

			return "success";

		} catch (Exception e) {
			e.printStackTrace();
			return "failed";
		}
	}

	@Transactional
	public String saveOrder(HttpSession session, Long addressId, String paymentMode) {

	    try {

	        Users user = (Users) session.getAttribute("loggedInUser");

	        SavedAddress address = savedAddressRepo.findById(addressId).orElse(null);

	        List<Cart> carts = cartRepo.findAllByUser(user);

	        for (Cart cart : carts) {

	            Products product = cart.getProduct();

	            if (product.getQuantity() < cart.getQuantity()) {
	                return "Stock Not Available";
	            }

	            Orders order = new Orders();
	            
	            

	            order.setProduct(product);
	            order.setUser(user);

	            order.setProductName(product.getProductName());
	            order.setDescription(product.getProductDescription());

	            order.setProductPrice(product.getSellingPrice());
	            order.setDiscount(product.getDiscount());
	            order.setFinalPrice(product.getFinalPrice());

	            order.setShippingCharge(product.getShippingCharge());

	            order.setQuantity(cart.getQuantity());
	            order.setColor(cart.getColor());
	            order.setSize(cart.getSize());

	            order.setTotalAmount(cart.getFinalPrice() + product.getShippingCharge());

	            order.setCustomerName(address.getCustomerName());
	            order.setContactNo(address.getContactNo());
	            order.setShippingAddress(address.getAddress());
	            order.setPincode(address.getPincode());

	            order.setOrderStatus(Orders.OrderStatus.Confirmed);
	            order.setOrderAt(LocalDateTime.now());

	            if(paymentMode.equals("COD")){

	            	order.setOrderId("COD" + System.currentTimeMillis());
		            order.setPaymentStatus(Orders.PaymentStatus.Pending);

	            }else{

	                order.setPaymentStatus(Orders.PaymentStatus.Success);

	            }

	            ordersRepo.save(order);

	            product.setQuantity(product.getQuantity() - cart.getQuantity());
	            productRepo.save(product);

	        }

	        cartRepo.deleteAll(carts);

	        return "success";

	    } catch (Exception e) {

	        e.printStackTrace();
	        return e.getMessage();

	    }

	}
}
