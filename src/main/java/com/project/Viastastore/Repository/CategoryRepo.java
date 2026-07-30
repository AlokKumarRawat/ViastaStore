package com.project.Viastastore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Viastastore.Model.Category;

public interface CategoryRepo extends JpaRepository<Category, Long> {

	boolean existsByCategoryName(String categoryName);

	List<Category> findAllByIsVisible(boolean b);

}
