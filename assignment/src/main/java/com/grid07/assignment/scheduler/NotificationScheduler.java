package com.grid07.assignment.scheduler;

import com.grid07.assignment.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final RedisService redisService;

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void sweepPendingNotifications() {
        Set<String> keys = redisService.getPendingNotifKeys();
        if (keys == null || keys.isEmpty())
            return;

        for (String key : keys) {
            // key format: user:{id}:pending_notifs
            String[] parts = key.split(":");
            Long userId = Long.valueOf(parts[1]);

            List<String> notifs = redisService.popAllPendingNotifs(userId);
            if (notifs == null || notifs.isEmpty())
                continue;

            String first = notifs.get(0);
            int others = notifs.size() - 1;

            if (others > 0) {
                System.out.println("Summarized Push Notification to User " + userId + ": " + first + " and " + others
                        + " others interacted with your posts.");
            } else {
                System.out.println("Summarized Push Notification to User " + userId + ": " + first);
            }
        }
    }
}