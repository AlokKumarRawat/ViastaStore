package com.project.Viastastore.MailService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Viastastore.Model.Cart;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Repository.CartRepo;
import com.project.Viastastore.Repository.ProductRepo;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    public void addToCart(String color, String size, int qty, Users user, long productId) {

        Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        if (product.getQuantity() < qty) {
            throw new RuntimeException("Insufficient Stock");
        }

        Optional<Cart> existing = cartRepo.findByUserAndProductAndColorAndSize(user,product,color, size);

        if (existing.isPresent()) {

            Cart cart = existing.get();

            int newQty = cart.getQuantity() + qty;

            if (newQty > product.getQuantity()) {
                throw new RuntimeException("Stock Limit Exceeded");
            }

            cart.setQuantity(newQty);

            cart.setFinalPrice(newQty * product.getFinalPrice());

            cartRepo.save(cart);

        } else {

            Cart cart = new Cart();

            cart.setUser(user);

            cart.setProduct(product);

            cart.setColor(color);

            cart.setSize(size);

            cart.setQuantity(qty);

            cart.setProductPrice(product.getFinalPrice());

            cart.setFinalPrice(qty * product.getFinalPrice());

            cartRepo.save(cart);

        }

    }
    
    public void increaseQuantity(Long cartId) {

        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart Item Not Found"));

        Products product = cart.getProduct();

        // Check stock
        if (cart.getQuantity() >= product.getQuantity()) {
            return;
        }

        cart.setQuantity(cart.getQuantity() + 1);

        cart.setFinalPrice(cart.getQuantity() * cart.getProductPrice());

        cartRepo.save(cart);
    }
    
    public void decreaseQuantity(Long cartId) {

        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart Item Not Found"));

        // Don't allow quantity below 1
        if (cart.getQuantity() > 1) {

            cart.setQuantity(cart.getQuantity() - 1);

            cart.setFinalPrice(cart.getQuantity() * cart.getProductPrice());

            cartRepo.save(cart);

        }

    }

}