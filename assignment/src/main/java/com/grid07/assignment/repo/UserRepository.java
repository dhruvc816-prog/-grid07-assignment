package com.grid07.assignment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grid07.assignment.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
