package edu.tedu.radar;

/**
 * An object that a {@link Radar} can detect, positioned on a 2D plane
 * relative to the radar's origin (0, 0).
 */
public class Target {

    private final String id;
    private final double x;
    private final double y;
    private final double rcs;

    /**
     * @param id  unique identifier of the target
     * @param x   horizontal coordinate relative to the radar's origin
     * @param y   vertical coordinate relative to the radar's origin
     * @param rcs radar cross-section, a non-negative measure of how strong
     *            a signal the target reflects back to the radar
     * @throws IllegalArgumentException if {@code rcs} is negative
     */
    public Target(String id, double x, double y, double rcs) {
        if (rcs < 0) {
            throw new IllegalArgumentException("rcs < 0");
        }
        this.id = id;
        this.x = x;
        this.y = y;
        this.rcs = rcs;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRcs() {
        return rcs;
    }
}
