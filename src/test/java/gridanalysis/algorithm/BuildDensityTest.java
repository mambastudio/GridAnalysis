package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.jfx.MEngine;

/** Regression checks for the dimensionally correct 2D density heuristic. */
public final class BuildDensityTest {
    public static void main(String[] args) {
        Build build = new Build(new MEngine(), new Hagrid());
        Vec2i unit = build.compute_grid_dims(
                new BBox(new Vec2f(0, 0), new Vec2f(1, 0.5f)), 600, 2.4f);
        Vec2i scaled = build.compute_grid_dims(
                new BBox(new Vec2f(0, 0), new Vec2f(600, 300)), 600, 2.4f);

        if (unit.x != scaled.x || unit.y != scaled.y) {
            throw new AssertionError("uniform scene scale changed dimensions: "
                    + unit + " != " + scaled);
        }
    }
}
