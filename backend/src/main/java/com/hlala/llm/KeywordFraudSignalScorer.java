package com.hlala.llm;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Placeholder scorer until a model-backed one exists. Flags the common
 * rental-scam phrasings: paying before seeing the room, paying outside the
 * app, and pressure to pay fast. Deliberately simple and transparent.
 */
@Component
public class KeywordFraudSignalScorer implements FraudSignalScorer {

    private static final List<String> SIGNALS = List.of(
            "pay before viewing", "deposit first", "send the money", "outside the app",
            "e-wallet", "cash send", "urgent payment", "someone else wants it",
            "never saw the room", "fake", "scam", "took my deposit");

    @Override
    public double score(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        long hits = SIGNALS.stream().filter(lower::contains).count();
        return Math.min(1.0, hits * 0.35);
    }
}
