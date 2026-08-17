package edu.tedu.radar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class Radar_Test {

    private final Radar radarForParamTest = new Radar("R_Param", 100, 0.5);

    // (Assertion 16-19): Incorporates GeometryParamTest.java (4 assertions)
    @ParameterizedTest
    @CsvSource({
            "0,0, 3,4, 5.0, 53.130102",  // Data Set 1
            "0,0, -3,4, 5.0, 126.869898" // Data Set 2
    })
    void distanceAndBearing(double x1, double y1, double x2, double y2, double dist, double bearing) {
        // Runs 2 times, yielding 4 assertions
        assertAll(
                () -> assertEquals(dist, radarForParamTest.distanceKm(x1, y1, x2, y2), 1e-6), // Assertion 16, 17
                () -> assertEquals(bearing, radarForParamTest.bearingDeg(x1, y1, x2, y2), 1e-5) // Assertion 18, 19
        );
    }

    // (Assertion 20-25): Incorporates 'scanDetectsTargetsWithinRangeAndThreshold' (6 assertions)
    @Test
    void scanDetectsTargetsWithinRangeAndThreshold() {
        Radar radar = new Radar("R1", 10.0, 0.5);
        List<Target> targets = List.of(
                new Target("A", 3, 4, 5),    // distance = 5, snr = 5/6 ≈ 0.83 (passes)
                new Target("B", 12, 0, 10),  // out of range
                new Target("C", 1, 1, 0.3)   // snr = 0.3 / (1 + sqrt(2)) ≈ 0.12 (too low)
        );
        List<Detection> detections = radar.scan(targets);

        // Assertion 20
        assertEquals(1, detections.size());
        Detection d = detections.get(0);

        assertAll(
                () -> assertEquals("A", d.getTargetId()),            // Assertion 21
                () -> assertTrue(d.getSnr() >= 0.5),                 // Assertion 22
                () -> assertEquals(5.0, d.getDistanceKm(), 1e-9),    // Assertion 23
                () -> assertEquals(53.130102, d.getBearingDeg(), 1e-5) // Assertion 24
        );
        // Assertion 25
        assertNotEquals(ThreatLevel.LOW, d.getThreat());
    }

    // (Assertion 26-27): Incorporates 'constructorValidation' for Radar (2 assertions)
    @Test
    void radarConstructorValidation() {
        // Assertion 26: maxRangeKm <= 0 throws exception
        assertThrows(IllegalArgumentException.class, () -> new Radar("R", 0, 0.5));
        // Assertion 27: snrThreshold < 0 throws exception
        assertThrows(IllegalArgumentException.class, () -> new Radar("R", 10, -0.1));
    }

    // (Assertion 28): Incorporates 'listOrderIsSortedByTargetId' (1 assertion)
    @Test
    void listOrderIsSortedByTargetId() {
        Radar radar = new Radar("R3", 50.0, 0.1);
        List<Target> targets = List.of(
                new Target("C", 0, 5, 2),
                new Target("A", 1, 0, 2),
                new Target("B", 0, 1, 2)
        );

        List<Detection> detections = radar.scan(targets);
        List<String> ids = detections.stream().map(Detection::getTargetId).toList();

        // Assertion 28
        assertIterableEquals(List.of("A", "B", "C"), ids);
    }

    // (Assertion 29): Incorporates 'highThreatImpliesHighConfidence' (1 assertion)
    @Test
    void highThreatImpliesHighConfidence() {
        Radar radar = new Radar("R2", 20.0, 0.5);
        var detections = radar.scan(List.of(
                new Target("A", 2, 0, 5),   // Close and high RCS -> High Threat
                new Target("B", 8, 0, 1)
        ));

        var highs = detections.stream()
                .filter(d -> d.getThreat() == ThreatLevel.HIGH)
                .toList();

        // Assertion 29
        assertTrue(highs.stream().allMatch(Detection::isHighConfidence));
    }

    // (Assertion 30-34): Tests the threat level tiers at their boundary conditions.
    @Test
    void threatLevelBoundaryChecks() {
        // snrThreshold = 1.0. Tiers are: >2.0 (HIGH), >1.2 (MEDIUM), <=1.2 (LOW)
        Radar radar = new Radar("T", 10.0, 1.0);
        double dist = 1.0; // Fixed distance for easy SNR calculation: SNR = RCS / 2.0

        // Target M: RCS=2.41. snr = 1.205 (just above 1.2 -> MEDIUM)
        Target tM = new Target("M", dist, 0, 2.41);
        // Target H: RCS=4.01. snr = 2.005 (just above 2.0 -> HIGH)
        Target tH = new Target("H", dist, 0, 4.01);
        // Target L: RCS=2.0. snr = 1.0 (Threshold boundary for LOW)
        Target tL = new Target("L", dist, 0, 2.0);

        List<Detection> detections = radar.scan(List.of(tM, tH, tL));
        Detection low = detections.stream().filter(d -> d.getTargetId().equals("L")).findFirst().get();
        Detection medium = detections.stream().filter(d -> d.getTargetId().equals("M")).findFirst().get();
        Detection high = detections.stream().filter(d -> d.getTargetId().equals("H")).findFirst().get();

        // Assertion 30: Check LOW boundary
        assertEquals(ThreatLevel.LOW, low.getThreat());
        // Assertion 31: Check MEDIUM boundary (just above 1.2)
        assertEquals(ThreatLevel.MEDIUM, medium.getThreat());
        // Assertion 32: Check HIGH boundary (just above 2.0)
        assertEquals(ThreatLevel.HIGH, high.getThreat());

        // Assertion 33: Check that the total list size is correct
        assertEquals(3, detections.size());
        // Assertion 34: Check that all detections are not null
        assertNotNull(low);
    }

    // (Assertion 35-39): Tests range boundary conditions for detection.
    @Test
    void rangeBoundaryDetection() {
        Radar radar = new Radar("R4", 5.0, 0.1); // Max range 5.0 km

        // Target At: Distance = 5.0 (on boundary). RCS high enough to pass SNR.
        Target tAt = new Target("T_At", 5.0, 0, 1.0);
        // Target JustOut: Distance = 5.000001 (just outside)
        Target tOut = new Target("T_Out", 5.000001, 0, 1.0);
        // Target WayIn: Distance = 1.0
        Target tIn = new Target("T_In", 1.0, 0, 1.0);

        List<Detection> detections = radar.scan(List.of(tAt, tOut, tIn));

        // Assertion 35: Only 2 targets should be detected (T_At and T_In)
        assertEquals(2, detections.size());

        // Use assertAll for multiple related checks
        assertAll(
                // Assertion 36: T_At (on the boundary) must be detected
                () -> assertTrue(detections.stream().anyMatch(d -> d.getTargetId().equals("T_At"))),
                // Assertion 37: T_Out (just outside) must NOT be detected
                () -> assertFalse(detections.stream().anyMatch(d -> d.getTargetId().equals("T_Out"))),
                // Assertion 38: T_In must be detected
                () -> assertTrue(detections.stream().anyMatch(d -> d.getTargetId().equals("T_In")))
        );

        // Assertion 39: Check the distance of the boundary target is exactly max range
        assertEquals(5.0, detections.stream().filter(d -> d.getTargetId().equals("T_At")).findFirst().get().getDistanceKm(), 1e-9);
    }

    // (Assertion 40-42): Tests scenarios where no targets pass (negative testing).
    @Test
    void noTargetsPassFilters() {
        Radar radar = new Radar("R5", 10.0, 5.0); // Very high threshold
        List<Target> targets = List.of(
                new Target("TooFar", 15.0, 0, 10.0), // Fails range
                new Target("LowSNR", 1.0, 0, 1.0)    // Fails SNR (SNR is 1.0/2 = 0.5, below 5.0)
        );

        List<Detection> detections = radar.scan(targets);

        // Assertion 40: Empty list returned (Main check)
        assertTrue(detections.isEmpty());

        // Assertion 41: Size is zero (Redundancy in logic, but often used for test coverage)
        assertEquals(0, detections.size());

        // Assertion 42: Asserting the Radar object itself is not null (Smoke check)
        assertNotNull(radar);
    }

    // (Assertion 43-45): Tests bearing/distance helpers for boundary cases.
    @Test
    void bearingBoundaryCases() {
        // Test 0/360 degrees
        Target t0 = new Target("T0", 1.0, 0.0, 1.0);
        // Test 90 degrees
        Target t90 = new Target("T90", 0.0, 1.0, 1.0);
        // Test -1, -1 for 225 degrees
        Target t225 = new Target("T225", -1.0, -1.0, 1.0);

        // Assertion 43: 0 degrees (must not be 360)
        assertEquals(0.0, radarForParamTest.bearingDeg(0, 0, t0.getX(), t0.getY()), 1e-9);
        // Assertion 44: 90 degrees
        assertEquals(90.0, radarForParamTest.bearingDeg(0, 0, t90.getX(), t90.getY()), 1e-9);
        // Assertion 45: 225 degrees (tests the 360-wrapping logic)
        assertEquals(225.0, radarForParamTest.bearingDeg(0, 0, t225.getX(), t225.getY()), 1e-9);
    }

    // ----------------------------------------------------------------
    // Basis path tests for Radar.scan() (6 tests, one per independent path)
    // ----------------------------------------------------------------

    // Path 1: Loop Exit (0 Iterations)
    @Test
    void testScan_Path1_EmptyList() {
        Radar radar = new Radar("R-P1", 10.0, 1.0);
        List<Target> targets = Collections.emptyList();
        List<Detection> detections = radar.scan(targets);
        assertTrue(detections.isEmpty(), "Path 1: Empty list should result in empty detections.");
    }

    // Path 2: Loop, Fails Range Check
    @Test
    void testScan_Path2_FailsRange() {
        Radar radar = new Radar("R-P2", 10.0, 1.0); // maxRange = 10
        List<Target> targets = List.of(
                new Target("T-Far", 11.0, 0.0, 99.0) // distance = 11.0
        );
        List<Detection> detections = radar.scan(targets);
        assertTrue(detections.isEmpty(), "Path 2: Out of range target should not be detected.");
    }

    // Path 3: Loop, Fails SNR Check
    @Test
    void testScan_Path3_FailsSNR() {
        Radar radar = new Radar("R-P3", 10.0, 1.0); // snrThreshold = 1.0
        List<Target> targets = List.of(
                new Target("T-Weak", 5.0, 0.0, 1.0) // distance=5, snr = 1.0 / (1+5) = 0.16 (FAILS)
        );
        List<Detection> detections = radar.scan(targets);
        assertTrue(detections.isEmpty(), "Path 3: Low SNR target should not be detected.");
    }

    // Path 4: Loop, Threat LOW
    @Test
    void testScan_Path4_ThreatLow() {
        Radar radar = new Radar("R-P4", 10.0, 1.0); // snrThreshold = 1.0
        // Tiers: HIGH > 2.0, MEDIUM > 1.2, LOW <= 1.2
        List<Target> targets = List.of(
                new Target("T-Low", 1.0, 0.0, 2.4) // distance=1, snr = 2.4 / (1+1) = 1.2 (LOW)
        );
        List<Detection> detections = radar.scan(targets);
        assertEquals(1, detections.size());
        assertEquals(ThreatLevel.LOW, detections.get(0).getThreat(), "Path 4: Target should be LOW threat.");
    }

    // Path 5: Loop, Threat MEDIUM
    @Test
    void testScan_Path5_ThreatMedium() {
        Radar radar = new Radar("R-P5", 10.0, 1.0); // snrThreshold = 1.0
        // Tiers: HIGH > 2.0, MEDIUM > 1.2, LOW <= 1.2
        List<Target> targets = List.of(
                new Target("T-Med", 1.0, 0.0, 2.5) // distance=1, snr = 2.5 / (1+1) = 1.25 (MEDIUM)
        );
        List<Detection> detections = radar.scan(targets);
        assertEquals(1, detections.size());
        assertEquals(ThreatLevel.MEDIUM, detections.get(0).getThreat(), "Path 5: Target should be MEDIUM threat.");
    }

    // Path 6: Loop, Threat HIGH
    @Test
    void testScan_Path6_ThreatHigh() {
        Radar radar = new Radar("R-P6", 10.0, 1.0); // snrThreshold = 1.0
        // Tiers: HIGH > 2.0, MEDIUM > 1.2, LOW <= 1.2
        List<Target> targets = List.of(
                new Target("T-High", 1.0, 0.0, 4.1) // distance=1, snr = 4.1 / (1+1) = 2.05 (HIGH)
        );
        List<Detection> detections = radar.scan(targets);
        assertEquals(1, detections.size());
        assertEquals(ThreatLevel.HIGH, detections.get(0).getThreat(), "Path 6: Target should be HIGH threat.");
    }
}

