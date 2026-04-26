package com.grid07.assignment.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redistemplate;

    // To check virality score
    public void incrementViralityScore(Long postId, int points) {
        redistemplate.opsForValue().increment("virality_score::" + postId, points);
    }

    public String getViralityScore(Long postId) {
        return redistemplate.opsForValue().get("virality_score::" + postId);
    }

    // Horizontal cap- max 100 bot replies per post
    public boolean incrementBotReplyCount(Long postId) {
        Long count = redistemplate.opsForValue().increment("post:" + postId + ":bot_count");
        if (count != null && count > 100) {
            redistemplate.opsForValue().decrement("post:" + postId + ":bot_count");
            return false;// rejected
        }
        return true;// accepted
    }

    // Cooldown cap=- A bot caanot interact with specific human more than 10 minutes
    public boolean checkAndSetCooldown(Long botId, Long humanId) {
        String key = "cooldown:" + botId + ":" + humanId;
        Boolean exists = redistemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }
        redistemplate.opsForValue().set(key, "1", Duration.ofMinutes(10));
        return true; // allowed
    }

    // Notifiaction throttle - 15 min cooldown per user
    public boolean checkAndSetNotifCooldown(Long userId) {
        String key = "notif_cooldown:user_" + userId;
        Boolean exists = redistemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false; // already notified recently
        }
        redistemplate.opsForValue().set(key, "1", Duration.ofMinutes(15));
        return true; // send notification
    }

    // Pending notifications list
    public void pushPendingNotif(Long userId, String message) {
        redistemplate.opsForList().rightPush("user:" + userId + ":pending_notifs", message);
    }

    public java.util.List<String> popAllPendingNotifs(Long userId) {
        String key = "user:" + userId + ":pending_notifs";
        Long size = redistemplate.opsForList().size(key);
        if (size == null || size == 0)
            return java.util.Collections.emptyList();
        java.util.List<String> notifs = redistemplate.opsForList().range(key, 0, size - 1);
        redistemplate.delete(key);
        return notifs;
    }

    public java.util.Set<String> getPendingNotifKeys() {
        return redistemplate.keys("user:*:pending_notifs");
    }

}
