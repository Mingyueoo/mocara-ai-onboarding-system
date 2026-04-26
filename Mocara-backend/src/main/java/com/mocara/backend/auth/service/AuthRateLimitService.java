package com.mocara.backend.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AuthRateLimitService {

    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000L;

    public void checkOrThrow(String action, String key) {
        long now = Instant.now().toEpochMilli();
        String bucketKey = action + ":" + key;
        Deque<Long> deque = attempts.computeIfAbsent(bucketKey, k -> new ConcurrentLinkedDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) {
                deque.pollFirst();
            }
            if (deque.size() >= MAX_ATTEMPTS) {
                throw new RateLimitException("Too many requests");
            }
            deque.addLast(now);
        }
    }
}
