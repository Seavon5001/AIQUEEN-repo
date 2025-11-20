package com.aiqueen.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SchedulerService
 * - Keeps campaigns in-memory and runs scheduled jobs
 * - Replace with persistent DB and durable queue in production
 */
@Service
public class SchedulerService {

    private final Map<String, Map<String,Object>> scheduled = new ConcurrentHashMap<>();

    public String scheduleCampaign(Map<String,Object> campaign) {
        String id = UUID.randomUUID().toString();
        campaign.put("id", id);
        campaign.put("createdAt", System.currentTimeMillis());
        scheduled.put(id, campaign);
        return id;
    }

    // Cron: every minute check scheduled tasks (example). Adjust schedule as needed.
    @Scheduled(fixedDelay = 60000)
    public void runScheduledTasks() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Map<String,Object>> e : scheduled.entrySet()) {
            Map<String,Object> c = e.getValue();
            // Example logic: check scheduled time and execute
            Object whenObj = c.get("when");
            if (whenObj != null) {
                try {
                    long when = Long.parseLong(String.valueOf(whenObj));
                    if (when <= now) {
                        // Execute campaign: publish, call, queue tasks, etc.
                        // For now, just log and remove
                        System.out.println("Executing campaign: " + e.getKey() + " -> " + c);
                        scheduled.remove(e.getKey());
                        // TODO: call service to post to social APIs / send emails / schedule calls
                    }
                } catch (NumberFormatException ex) {
                    // ignore invalid format
                }
            }
        }
    }
}
