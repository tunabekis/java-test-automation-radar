package edu.tedu.radar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mockito-based integration tests for {@link Radar#scan(List)}: exercises the
 * full scan pipeline (range filtering, SNR filtering, threat classification,
 * geometry, sorting) against mocked {@link Target} objects.
 */
class RadarIntegrationTest {

    private Radar radar;

    @Mock
    private Target target1;
    @Mock
    private Target target2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        radar = new Radar("IntegrationRadar", 100.0, 1.0);
    }

    // --- CATEGORY 1: HAPPY PATH INTEGRATIONS ---

    @Test
    void case01_HighThreatIntegration() {
        when(target1.getId()).thenReturn("T1");
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(50.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(1, results.size());
        assertEquals(ThreatLevel.HIGH, results.get(0).getThreat());
        verify(target1, atLeastOnce()).getRcs();
    }

    @Test
    void case02_MediumThreatIntegration() {
        when(target1.getId()).thenReturn("T2");
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(16.5);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(1, results.size());
        assertEquals(ThreatLevel.MEDIUM, results.get(0).getThreat());
    }

    @Test
    void case03_LowThreatIntegration() {
        when(target1.getId()).thenReturn("T3");
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(12.1);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(1, results.size());
        assertEquals(ThreatLevel.LOW, results.get(0).getThreat());
    }

    // --- CATEGORY 2: GEOMETRY INTEGRATIONS ---

    @Test
    void case04_TargetAtOrigin() {
        when(target1.getId()).thenReturn("T4");
        when(target1.getX()).thenReturn(0.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(5.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(0.0, results.get(0).getDistanceKm());
    }

    @Test
    void case05_TargetInQuadrant2() {
        when(target1.getId()).thenReturn("T5");
        when(target1.getX()).thenReturn(-3.0);
        when(target1.getY()).thenReturn(4.0);
        when(target1.getRcs()).thenReturn(20.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(126.8, results.get(0).getBearingDeg(), 0.1);
    }

    @Test
    void case06_TargetInQuadrant3() {
        when(target1.getId()).thenReturn("T6");
        when(target1.getX()).thenReturn(-3.0);
        when(target1.getY()).thenReturn(-4.0);
        when(target1.getRcs()).thenReturn(20.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(233.1, results.get(0).getBearingDeg(), 0.1);
    }

    @Test
    void case07_TargetInQuadrant4() {
        when(target1.getId()).thenReturn("T7");
        when(target1.getX()).thenReturn(3.0);
        when(target1.getY()).thenReturn(-4.0);
        when(target1.getRcs()).thenReturn(20.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(306.9, results.get(0).getBearingDeg(), 0.1);
    }

    // --- CATEGORY 3: BOUNDARY INTEGRATIONS ---

    @Test
    void case08_MaxRangeBoundaryIncluded() {
        when(target1.getId()).thenReturn("T8");
        when(target1.getX()).thenReturn(100.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(500.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(1, results.size());
    }

    @Test
    void case09_OutOfRangeExcluded() {
        when(target1.getX()).thenReturn(100.1);
        when(target1.getY()).thenReturn(0.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertTrue(results.isEmpty());
        verify(target1, never()).getRcs();
    }

    @Test
    void case10_SnrThresholdExact() {
        when(target1.getId()).thenReturn("T10");
        when(target1.getX()).thenReturn(9.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(10.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(1, results.size());
        assertEquals(1.0, results.get(0).getSnr(), 0.001);
    }

    @Test
    void case11_SnrBelowThreshold() {
        when(target1.getX()).thenReturn(9.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(9.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertTrue(results.isEmpty());
    }

    // --- CATEGORY 4: THREAT LOGIC BOUNDARIES ---

    @Test
    void case12_JustBelowHigh() {
        when(target1.getId()).thenReturn("T12");
        when(target1.getX()).thenReturn(9.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(20.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(ThreatLevel.MEDIUM, results.get(0).getThreat());
    }

    @Test
    void case13_JustAboveHigh() {
        when(target1.getId()).thenReturn("T13");
        when(target1.getX()).thenReturn(9.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(20.1);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(ThreatLevel.HIGH, results.get(0).getThreat());
    }

    @Test
    void case14_JustBelowMedium() {
        when(target1.getId()).thenReturn("T14");
        when(target1.getX()).thenReturn(9.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(12.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(ThreatLevel.LOW, results.get(0).getThreat());
    }

    @Test
    void case15_JustAboveMedium() {
        when(target1.getId()).thenReturn("T15");
        when(target1.getX()).thenReturn(9.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(12.1);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertEquals(ThreatLevel.MEDIUM, results.get(0).getThreat());
    }

    // --- CATEGORY 5: MULTI-OBJECT INTEGRATION ---

    @Test
    void case16_MultipleTargetsIntegration() {
        when(target1.getId()).thenReturn("A");
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(50.0);

        when(target2.getId()).thenReturn("B");
        when(target2.getX()).thenReturn(10.0);
        when(target2.getY()).thenReturn(0.0);
        when(target2.getRcs()).thenReturn(50.0);

        List<Detection> results = radar.scan(Arrays.asList(target1, target2));

        assertEquals(2, results.size());
    }

    @Test
    void case17_SortingIntegration() {
        when(target1.getId()).thenReturn("Z_Zulu");
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(50.0);

        when(target2.getId()).thenReturn("A_Alpha");
        when(target2.getX()).thenReturn(10.0);
        when(target2.getY()).thenReturn(0.0);
        when(target2.getRcs()).thenReturn(50.0);

        List<Detection> results = radar.scan(Arrays.asList(target1, target2));

        assertEquals("A_Alpha", results.get(0).getTargetId());
        assertEquals("Z_Zulu", results.get(1).getTargetId());
    }

    @Test
    void case18_MixedValidInvalidIntegration() {
        when(target1.getId()).thenReturn("Valid");
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(50.0);

        when(target2.getX()).thenReturn(200.0);
        when(target2.getY()).thenReturn(0.0);

        List<Detection> results = radar.scan(Arrays.asList(target1, target2));

        assertEquals(1, results.size());
        assertEquals("Valid", results.get(0).getTargetId());
    }

    // --- CATEGORY 6: EDGE CASES ---

    @Test
    void case19_ZeroRCS() {
        when(target1.getX()).thenReturn(10.0);
        when(target1.getY()).thenReturn(0.0);
        when(target1.getRcs()).thenReturn(0.0);

        List<Detection> results = radar.scan(Collections.singletonList(target1));

        assertTrue(results.isEmpty());
    }

    @Test
    void case20_EmptyListInput() {
        List<Detection> results = radar.scan(Collections.emptyList());
        assertTrue(results.isEmpty());
    }
}
