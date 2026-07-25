package gridanalysis.gridclasses;

/** Inclusive two-dimensional integer range. */
public record Range(int lx, int ly, int hx, int hy) {
    public int size() {
        return (hx - lx + 1) * (hy - ly + 1);
    }
}
