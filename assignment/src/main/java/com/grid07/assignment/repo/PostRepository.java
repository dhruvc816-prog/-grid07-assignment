package com.grid07.assignment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grid07.assignment.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
