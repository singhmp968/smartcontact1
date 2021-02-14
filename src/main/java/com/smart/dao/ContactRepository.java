package com.smart.dao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

import java.util.*;

public interface ContactRepository extends JpaRepository<Contact,Integer> {

	//Pagination...
	//current page
	// num contact per page
	@Query("From Contact as c where c.user.id=:userId")
	public Page<Contact> findContactByUser(@Param("userId") int userId,Pageable pePageable);
	// searching 
	public List<Contact> findByNameContainingAndUser(String name,User user) ;// name== Name
	
}
