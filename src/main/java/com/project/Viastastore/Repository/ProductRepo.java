package com.project.Viastastore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Viastastore.Model.Category;
import com.project.Viastastore.Model.Products;

public interface ProductRepo extends JpaRepository<Products, Long> {

	List<Products> findAllByCategory(Category category);


	List<Products> findAllByProductNameContainingOrBrandNameContainingOrProductDescriptionContainingAllIgnoreCase(
			String value, String value2, String value3);


	List<Products> findAllByCategoryIdIn(List<Long> ids);

}
