package com.example.aurix.brain;

import com.example.aurix.config.AurixConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ReplyPlanner {
    private final Random random = new Random();
    private Instant lastReplyAt = Instant.EPOCH;

    public boolean shouldReply(AurixConfig config, List<ConversationMemory.ChatLine> recentLines) {
        if (!config.enabled || recentLines.isEmpty()) {
            return false;
        }

        long since = Duration.between(lastReplyAt, Instant.now()).getSeconds();
        if (since < config.minSecondsBetweenReplies) {
            return false;
        }

        if (config.replyWhenMentioned && wasMentioned(config, recentLines)) {
            return true;
        }

        if (recentLines.size() < config.minMessagesBeforeOrganicReply) {
            return false;
        }

        return random.nextDouble() < config.organicReplyChance;
    }

    public void markReplied() {
        this.lastReplyAt = Instant.now();
    }

    private boolean wasMentioned(AurixConfig config, List<ConversationMemory.ChatLine> recentLines) {
        String target = config.botName.toLowerCase(Locale.ROOT);
        for (int i = Math.max(0, recentLines.size() - config.triggerWindowMessages); i < recentLines.size(); i++) {
            String msg = recentLines.get(i).content().toLowerCase(Locale.ROOT);
            if (msg.contains(target)) {
                return true;
            }
        }
        return false;
    }
}
