package edu.tedu.radar;

/**
 * The result of a {@link Radar} detecting a {@link Target} during a scan:
 * where it was, how strong the signal was, and how threatening it is judged
 * to be.
 */
public class Detection {

    private final String targetId;
    private final double distanceKm;
    private final double bearingDeg;
    private final double snr;
    private final ThreatLevel threat;

    public Detection(String targetId, double distanceKm, double bearingDeg, double snr, ThreatLevel threat) {
        this.targetId = targetId;
        this.distanceKm = distanceKm;
        this.bearingDeg = bearingDeg;
        this.snr = snr;
        this.threat = threat;
    }

    public String getTargetId() {
        return targetId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getBearingDeg() {
        return bearingDeg;
    }

    public double getSnr() {
        return snr;
    }

    public ThreatLevel getThreat() {
        return threat;
    }

    /**
     * A detection is considered high-confidence only when it was classified
     * as a {@link ThreatLevel#HIGH} threat.
     */
    public boolean isHighConfidence() {
        return this.threat == ThreatLevel.HIGH;
    }

    /**
     * Ranks how urgently this detection should be handled, from immediate
     * interception down to passive logging. Evaluated in order:
     * <ol>
     *   <li>HIGH threat within 10 km &rarr; intercept immediately</li>
     *   <li>HIGH threat beyond 10 km &rarr; monitor</li>
     *   <li>MEDIUM threat with SNR above 3.0 &rarr; track</li>
     *   <li>everything else &rarr; log only</li>
     * </ol>
     */
    public String assessDetectionPriority() {
        if (this.threat == ThreatLevel.HIGH && this.distanceKm < 10.0) {
            return "PRIORITY_1_INTERCEPT";
        } else if (this.threat == ThreatLevel.HIGH) {
            return "PRIORITY_2_MONITOR";
        } else if (this.threat == ThreatLevel.MEDIUM && this.snr > 3.0) {
            return "PRIORITY_3_TRACK";
        } else {
            return "PRIORITY_4_LOG";
        }
    }
}
