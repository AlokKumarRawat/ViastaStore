package com.project.Viastastore.Repository;

import org.springframework.data.jpa.repository.JpaRepository; 

import com.project.Viastastore.Model.Enquiry;

public interface EnquiryRepo extends JpaRepository<Enquiry, Long> {

}
