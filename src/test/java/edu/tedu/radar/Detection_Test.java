package edu.tedu.radar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Detection_Test {

    // --- Constructor, getters and confidence logic (15 assertions) ---

    // (Assertion 7-12): Checks valid construction and all getters
    @Test
    void detectionInitializationAndGetters() {
        Detection d = new Detection("D1", 15.0, 90.0, 3.5, ThreatLevel.HIGH);

        assertEquals("D1", d.getTargetId());
        assertEquals(15.0, d.getDistanceKm());
        assertEquals(90.0, d.getBearingDeg());
        assertEquals(3.5, d.getSnr());
        assertEquals(ThreatLevel.HIGH, d.getThreat());
        assertNotNull(d);
    }

    // (Assertion 13-15): Tests the isHighConfidence() logic boundary for all threat levels
    @Test
    void highConfidenceLogic() {
        Detection lowThreat = new Detection("L", 1, 1, 1, ThreatLevel.LOW);
        Detection mediumThreat = new Detection("M", 1, 1, 2, ThreatLevel.MEDIUM);
        Detection highThreat = new Detection("H", 1, 1, 3, ThreatLevel.HIGH);

        assertFalse(lowThreat.isHighConfidence());
        assertFalse(mediumThreat.isHighConfidence());
        assertTrue(highThreat.isHighConfidence());
    }

    // --- Basis path tests for assessDetectionPriority() (4 tests) ---

    /**
     * Basis Path 1: (A -> B -> C -> I)
     * Conditions: threat == HIGH && distance < 10.0 -> TRUE
     * Expected: "PRIORITY_1_INTERCEPT"
     */
    @Test
    void testAssessPriority_Path1_HighAndClose() {
        // Inputs: threat=HIGH, distance=5.0 (< 10.0), snr=5.0
        Detection d = new Detection("T1", 5.0, 0, 5.0, ThreatLevel.HIGH);
        assertEquals("PRIORITY_1_INTERCEPT", d.assessDetectionPriority());
    }

    /**
     * Basis Path 2: (A -> B -> D -> E -> I)
     * Conditions: threat == HIGH && distance < 10.0 -> FALSE
     * threat == HIGH                   -> TRUE
     * (i.e., HIGH threat, distance >= 10.0)
     * Expected: "PRIORITY_2_MONITOR"
     */
    @Test
    void testAssessPriority_Path2_HighAndFar() {
        // Inputs: threat=HIGH, distance=15.0 (>= 10.0), snr=5.0
        Detection d = new Detection("T2", 15.0, 0, 5.0, ThreatLevel.HIGH);
        assertEquals("PRIORITY_2_MONITOR", d.assessDetectionPriority());
    }

    /**
     * Basis Path 3: (A -> B -> D -> F -> G -> I)
     * Conditions: threat == HIGH -> FALSE
     * threat == MEDIUM && snr > 3.0 -> TRUE
     * Expected: "PRIORITY_3_TRACK"
     */
    @Test
    void testAssessPriority_Path3_MediumAndHighSNR() {
        // Inputs: threat=MEDIUM, distance=20.0, snr=4.0 (> 3.0)
        Detection d = new Detection("T3", 20.0, 0, 4.0, ThreatLevel.MEDIUM);
        assertEquals("PRIORITY_3_TRACK", d.assessDetectionPriority());
    }

    /**
     * Basis Path 4: (A -> B -> D -> F -> H -> I)
     * Conditions: threat == HIGH -> FALSE
     * threat == MEDIUM && snr > 3.0 -> FALSE
     * (Covers two cases: MEDIUM threat with low SNR, or LOW threat)
     * Expected: "PRIORITY_4_LOG"
     */
    @Test
    void testAssessPriority_Path4_LowPriority() {
        // Case 4a: MEDIUM threat, low SNR
        // Inputs: threat=MEDIUM, distance=20.0, snr=2.0 (<= 3.0)
        Detection d_med_low_snr = new Detection("T4a", 20.0, 0, 2.0, ThreatLevel.MEDIUM);

        // Case 4b: LOW threat
        // Inputs: threat=LOW, distance=5.0, snr=5.0
        Detection d_low = new Detection("T4b", 5.0, 0, 5.0, ThreatLevel.LOW);

        assertAll(
                () -> assertEquals("PRIORITY_4_LOG", d_med_low_snr.assessDetectionPriority()),
                () -> assertEquals("PRIORITY_4_LOG", d_low.assessDetectionPriority())
        );
    }

    // --- Equality-boundary tests (kill CONDITIONALS_BOUNDARY mutants on distance/snr) ---

    /**
     * At exactly distance == 10.0 the "distance &lt; 10.0" condition is false,
     * so a HIGH threat must fall through to PRIORITY_2_MONITOR, not PRIORITY_1_INTERCEPT.
     */
    @Test
    void testAssessPriority_DistanceBoundaryEqualsTen() {
        Detection d = new Detection("T5", 10.0, 0, 5.0, ThreatLevel.HIGH);
        assertEquals("PRIORITY_2_MONITOR", d.assessDetectionPriority());
    }

    /**
     * At exactly snr == 3.0 the "snr &gt; 3.0" condition is false,
     * so a MEDIUM threat must fall through to PRIORITY_4_LOG, not PRIORITY_3_TRACK.
     */
    @Test
    void testAssessPriority_SnrBoundaryEqualsThree() {
        Detection d = new Detection("T6", 20.0, 0, 3.0, ThreatLevel.MEDIUM);
        assertEquals("PRIORITY_4_LOG", d.assessDetectionPriority());
    }
}
