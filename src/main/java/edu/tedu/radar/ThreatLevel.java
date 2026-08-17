package edu.tedu.radar;

/**
 * Classification of how dangerous a detected target is, derived from its
 * signal-to-noise ratio (SNR) relative to a radar's configured threshold.
 */
public enum ThreatLevel {
    LOW,
    MEDIUM,
    HIGH
}
