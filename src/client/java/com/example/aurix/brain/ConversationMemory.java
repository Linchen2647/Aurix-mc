package com.example.aurix.brain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConversationMemory {
    public record ChatLine(
            Instant time,
            String sender,
            String content,
            boolean self
    ) {}

    private final List<ChatLine> lines = new ArrayList<>();

    public synchronized void add(String sender, String content, boolean self, int maxSize) {
        lines.add(new ChatLine(Instant.now(), sender, content, self));
        while (lines.size() > maxSize) {
            lines.remove(0);
        }
    }

    public synchronized List<ChatLine> lastN(int n) {
        int from = Math.max(0, lines.size() - n);
        return new ArrayList<>(lines.subList(from, lines.size()));
    }
}
