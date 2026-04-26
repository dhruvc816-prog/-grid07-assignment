package com.grid07.assignment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grid07.assignment.entity.Comment;
import com.grid07.assignment.entity.Post;
import com.grid07.assignment.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postservice;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Map<String, Object> body) {
        Long authorId = Long.valueOf(body.get("authorId").toString());
        String authorType = body.get("authorType").toString();
        String content = body.get("content").toString();

        return ResponseEntity.ok(postservice.createPost(authorId, authorType, content));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Post> likePost(@PathVariable Long postId) {
        return ResponseEntity.ok(postservice.likePost(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody Map<String, Object> body) {
        Long authorId = Long.valueOf(body.get("authorId").toString());
        String authorType = body.get("authorType").toString();
        String content = body.get("content").toString();
        int depthLevel = Integer.valueOf(body.get("depthLevel").toString());
        Long humanOwnerId = Long.valueOf(body.get("humanOwnerId").toString());

        return ResponseEntity
                .ok(postservice.addComment(postId, authorId, authorType, content, depthLevel, humanOwnerId));
    }
}
