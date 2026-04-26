package com.grid07.assignment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grid07.assignment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
