package com.project.Viastastore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Viastastore.Model.SavedAddress;
import com.project.Viastastore.Model.Users;

public interface SavedAddressRepo extends JpaRepository<SavedAddress, Long>  {

	

	List<SavedAddress> findAllByActiveAndUser(boolean b, Users user);

	List<SavedAddress> findAllByUser(Users user);

	SavedAddress findByUserAndActiveTrue(Users user);

}
