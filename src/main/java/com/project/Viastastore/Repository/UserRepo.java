package com.project.Viastastore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Model.Users.UserRole;
import com.project.Viastastore.Model.Users.UserStatus;

public interface UserRepo extends JpaRepository<Users, Long> {

	boolean existsByEmail(String email);

	Users findByEmail(String email);

	List<Users> findAllByRole(UserRole user);

	List<Users> findAllByRoleAndStatus(UserRole user, UserStatus status);


}
