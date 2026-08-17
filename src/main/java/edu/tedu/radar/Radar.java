package edu.tedu.radar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scans a list of {@link Target}s and reports the ones that are both within
 * range and strong enough to detect, each classified with a {@link ThreatLevel}.
 */
public class Radar {

    private final String id;
    private final double maxRangeKm;
    private final double snrThreshold;

    /**
     * @param id           unique identifier of this radar
     * @param maxRangeKm   maximum detection range in kilometers
     * @param snrThreshold minimum signal-to-noise ratio required to register a detection
     * @throws IllegalArgumentException if {@code maxRangeKm} is not positive or {@code snrThreshold} is negative
     */
    public Radar(String id, double maxRangeKm, double snrThreshold) {
        if (maxRangeKm <= 0 || snrThreshold < 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        this.id = id;
        this.maxRangeKm = maxRangeKm;
        this.snrThreshold = snrThreshold;
    }

    /**
     * Scans the given targets and returns a detection for every target that is
     * within {@link #maxRangeKm} and whose signal-to-noise ratio meets
     * {@link #snrThreshold}. Results are sorted by target id.
     */
    public List<Detection> scan(List<Target> targets) {
        List<Detection> detections = new ArrayList<>();
        for (Target target : targets) {
            double distance = distanceKm(0, 0, target.getX(), target.getY());
            if (distance > maxRangeKm) {
                continue;
            }
            double snr = target.getRcs() / (1.0 + distance);
            if (snr < snrThreshold) {
                continue;
            }
            double bearing = bearingDeg(0, 0, target.getX(), target.getY());
            ThreatLevel level = classifyThreatLevel(snr);
            detections.add(new Detection(target.getId(), distance, bearing, snr, level));
        }
        detections.sort(Comparator.comparing(Detection::getTargetId));
        return detections;
    }

    /**
     * Maps a signal-to-noise ratio to a threat level: more than twice the
     * configured threshold is HIGH, more than 1.2x is MEDIUM, otherwise LOW.
     */
    private ThreatLevel classifyThreatLevel(double snr) {
        if (snr > snrThreshold * 2) {
            return ThreatLevel.HIGH;
        } else if (snr > snrThreshold * 1.2) {
            return ThreatLevel.MEDIUM;
        } else {
            return ThreatLevel.LOW;
        }
    }

    // Package-private helpers, exposed for direct parameterized testing.
    double distanceKm(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    double bearingDeg(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        return (angle + 360) % 360;
    }

    public String getId() {
        return id;
    }

    public double getMaxRangeKm() {
        return maxRangeKm;
    }

    public double getSnrThreshold() {
        return snrThreshold;
    }
}
