package com.grid07.assignment.service;

import com.grid07.assignment.entity.Comment;
import com.grid07.assignment.entity.Post;
import com.grid07.assignment.repo.CommentRepository;
import com.grid07.assignment.repo.PostRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RedisService redisService;

    @Transactional
    public Post createPost(Long authorId, String authorType, String content) {
        Post post = Post.builder()
                .authorId(authorId)
                .authorType(authorType)
                .content(content)
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public Post likePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        redisService.incrementViralityScore(postId, 20);
        post.setLikeCount(post.getLikeCount() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public Comment addComment(Long postId, Long authorId, String authorType,
            String content, int depthLevel, Long humanOwnerId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (depthLevel > 20) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Comment depth exceeds limit");
        }

        if ("BOT".equalsIgnoreCase(authorType)) {
            boolean allowed = redisService.incrementBotReplyCount(postId);
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many bot comments");
            }

            boolean cooldownOk = redisService.checkAndSetCooldown(authorId, humanOwnerId);
            if (!cooldownOk) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Bot cooldown active — wait 10 minutes");
            }

            boolean directNotif = redisService.checkAndSetNotifCooldown(humanOwnerId);
            if (directNotif) {
                System.out.println("Push Notification Sent to User " + humanOwnerId);
            } else {
                redisService.pushPendingNotif(humanOwnerId, "Bot " + authorId + " replied to your post");
            }

            redisService.incrementViralityScore(postId, 1);
        } else {
            redisService.incrementViralityScore(postId, 50);
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .authorType(authorType)
                .content(content)
                .depthLevel(depthLevel)
                .createdAt(LocalDateTime.now())
                .build();
        return commentRepository.save(comment);
    }
}