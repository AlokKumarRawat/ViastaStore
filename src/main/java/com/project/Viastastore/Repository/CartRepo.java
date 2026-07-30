 package com.project.Viastastore.Repository;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Viastastore.Model.Cart;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.Users;

public interface CartRepo extends JpaRepository<Cart, Long> {

	boolean existsByUserAndProduct(Users user, Products product);

	Optional<Cart> findByUserAndProductAndColorAndSize(Users user, Products product, String color, String size);

	List<Cart> findAllByUser(Users user);

}
