package com.hlala.llm;

/**
 * Scores free text (report descriptions now, chat messages later) for scam
 * signals. Isolated behind this interface so a model-backed version can be
 * swapped in, or switched off, without touching any API contract.
 */
public interface FraudSignalScorer {

    /** 0.0 = no concern, 1.0 = almost certainly a scam pattern. */
    double score(String text);

    default boolean isUrgent(String text) {
        return score(text) >= 0.7;
    }
}
