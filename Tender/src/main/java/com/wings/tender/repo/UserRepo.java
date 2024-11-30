package com.wings.tender.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wings.tender.model.User;

@Repository
public interface UserRepo extends JpaRepository<User,Integer>{
	User findByEmail(String email);
}
