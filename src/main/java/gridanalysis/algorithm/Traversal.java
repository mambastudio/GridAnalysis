package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.SmallCell;
import gridanalysis.gridclasses.Tri;
import gridanalysis.utilities.list.IntegerList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Step-wise 2D traversal of a Hagrid grid.
 *
 * <p>This is the CPU reference counterpart of Hagrid's {@code traverse.cu}:
 * locate the voxel, look up its cell, intersect that cell's primitives, find
 * the farthest cell planes along the ray, and advance to the exit voxel. A
 * caller selects normal or expanded traversal by supplying the corresponding
 * immutable array of cell exit bounds. Full {@link Cell} and compressed
 * {@link SmallCell} representations are both supported.</p>
 */
public final class Traversal {
    private final Grid grid;
    private final Tri[] primitives;
    private final Cell[] cells;
    private final SmallCell[] smallCells;
    private final IntegerList references;
    private final Vec2f origin;
    private final Vec2f direction;
    private final double maxT;
    private final ArrayList<Vec2f> exits = new ArrayList<>();
    private final LinkedHashSet<Integer> testedRefs = new LinkedHashSet<>();

    private double t;
    private boolean done;
    private boolean hit;
    private int iteration;
    private int totalWork;
    private int currentCell = -1;
    private Vec2i voxel;
    private double lastExitT = Double.NaN;
    private double bestHitT = Double.POSITIVE_INFINITY;
    private int bestHitRef = -1;
    private String message = "Ray ready";

    public Traversal(Grid grid, Tri[] primitives, Cell[] cells, Vec2f origin, Vec2f end) {
        this(grid, primitives, cells, null, grid.ref_ids, origin, end);
    }

    public Traversal(Grid grid, Tri[] primitives, SmallCell[] cells,
            IntegerList references, Vec2f origin, Vec2f end) {
        this(grid, primitives, null, cells, references, origin, end);
    }

    private Traversal(Grid grid, Tri[] primitives, Cell[] cells, SmallCell[] smallCells,
            IntegerList references, Vec2f origin, Vec2f end) {
        this.grid = grid;
        this.primitives = primitives;
        this.cells = cells;
        this.smallCells = smallCells;
        this.references = references;
        this.origin = origin;
        this.direction = end.sub(origin);
        this.maxT = 1.0;

        Vec2f voxelPosition = origin.sub(grid.bbox.min).mul(grid.grid_inv());
        voxel = Vec2i.clamp(new Vec2i(voxelPosition), new Vec2i(), grid.grid_dims().sub(1));
    }

    /** Advances traversal by one cell, or stops at the nearest valid hit. */
    public void step() {
        if (done) {
            message = hit ? "Traversal already stopped at a primitive hit"
                    : "Traversal already left the grid";
            return;
        }

        Vec2i dimensions = grid.grid_dims();
        iteration++;
        testedRefs.clear();
        int cellId = GridAbstracts.lookup_entry(grid.entries, grid.shift, grid.dims, voxel);
        currentCell = cellId;
        Vec2i cellMin;
        Vec2i cellMax;
        BBox bounds;
        if (smallCells == null) {
            Cell cell = cells[cellId];
            cellMin = cell.min;
            cellMax = cell.max;
            bounds = grid.cellbound(cell);
            testPrimitives(cell.begin, cell.end);
        } else {
            SmallCell cell = smallCells[cellId];
            cellMin = cell.min().toVec2i();
            cellMax = cell.max().toVec2i();
            bounds = grid.cellbound(cell);
            testSentinelPrimitives(cell.begin());
        }
        double tx = boundaryT(bounds, 0);
        double ty = boundaryT(bounds, 1);
        double exitT = Math.min(tx, ty);
        lastExitT = exitT;

        totalWork += 1 + testedRefs.size();
        if (bestHitT <= exitT) {
            t = bestHitT;
            hit = true;
            done = true;
            message = "Iteration " + iteration + ": HIT T" + bestHitRef
                    + " at t=" + format(bestHitT) + " before exit t=" + format(exitT);
            return;
        }

        Vec2f exitPoint = origin.add(direction.mul((float) exitT));
        exits.add(exitPoint);
        Vec2f exitVoxelPosition = exitPoint.sub(grid.bbox.min).mul(grid.grid_inv());
        Vec2i exitVoxel = new Vec2i(exitVoxelPosition);
        Vec2i cellPoint = new Vec2i(
                direction.x >= 0 ? cellMax.x : cellMin.x,
                direction.y >= 0 ? cellMax.y : cellMin.y);
        Vec2i nextVoxel = new Vec2i(
                nearlyEqual(exitT, tx) ? cellPoint.x + (direction.x >= 0 ? 0 : -1) : exitVoxel.x,
                nearlyEqual(exitT, ty) ? cellPoint.y + (direction.y >= 0 ? 0 : -1) : exitVoxel.y);
        voxel = new Vec2i(
                direction.x >= 0 ? Math.max(nextVoxel.x, voxel.x) : Math.min(nextVoxel.x, voxel.x),
                direction.y >= 0 ? Math.max(nextVoxel.y, voxel.y) : Math.min(nextVoxel.y, voxel.y));
        t = exitT;

        if (t >= maxT || voxel.x < 0 || voxel.y < 0
                || voxel.x >= dimensions.x || voxel.y >= dimensions.y) {
            done = true;
            message = "Iteration " + iteration + ": exited grid from cell " + cellId;
        } else {
            message = "Iteration " + iteration + ": cell " + cellId
                    + " tested " + testedRefs + ", exit t=" + format(exitT)
                    + ", next voxel=" + voxel;
        }
    }

    private double boundaryT(BBox bounds, int axis) {
        double d = direction.get(axis);
        double boundary = d >= 0 ? bounds.max.get(axis) : bounds.min.get(axis);
        return (boundary - origin.get(axis)) / d;
    }

    private void testPrimitives(int begin, int end) {
        for (int i = begin; i < end; i++) {
            testPrimitive(references.get(i));
        }
    }

    private void testSentinelPrimitives(int begin) {
        if (begin < 0) return;
        for (int i = begin; references.get(i) >= 0; i++) {
            testPrimitive(references.get(i));
        }
    }

    private void testPrimitive(int primitiveId) {
            testedRefs.add(primitiveId);
            Tri tri = primitives[primitiveId];
            Vec2f[] vertices = {tri.p0(), tri.p1(), tri.p2()};
            for (int edge = 0; edge < 3; edge++) {
                double candidate = raySegmentT(vertices[edge], vertices[(edge + 1) % 3]);
                if (candidate >= 0 && candidate < bestHitT) {
                    bestHitT = candidate;
                    bestHitRef = primitiveId;
                }
        }
    }

    private double raySegmentT(Vec2f a, Vec2f b) {
        Vec2f edge = b.sub(a);
        Vec2f delta = a.sub(origin);
        double denominator = cross(direction, edge);
        if (Math.abs(denominator) < 1e-9) return Double.POSITIVE_INFINITY;
        double rayT = cross(delta, edge) / denominator;
        double segmentT = cross(delta, direction) / denominator;
        return segmentT >= 0 && segmentT <= 1 ? rayT : Double.POSITIVE_INFINITY;
    }

    public Vec2f origin() { return origin; }
    public Vec2f direction() { return direction; }
    public double maxT() { return maxT; }
    public Vec2f endPoint() { return origin.add(direction); }
    public Vec2f point() { return origin.add(direction.mul((float) Math.min(t, maxT))); }
    public List<Vec2f> exits() { return Collections.unmodifiableList(exits); }
    public Set<Integer> testedRefs() { return Collections.unmodifiableSet(testedRefs); }
    public boolean hit() { return hit; }
    public int currentCell() { return currentCell; }
    public String message() { return message; }

    public String details() {
        String hitText = bestHitRef < 0 ? "none" : "T" + bestHitRef + "@" + format(bestHitT);
        return "iteration=" + iteration + " voxel=" + voxel
                + " cell=" + currentCell + " tested=" + testedRefs
                + " nearestHit=" + hitText
                + " exit=" + (Double.isNaN(lastExitT) ? "-" : format(lastExitT))
                + " work=" + totalWork;
    }

    private static double cross(Vec2f a, Vec2f b) {
        return a.x * b.y - a.y * b.x;
    }

    private static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) <= 1e-7 * Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
    }

    private static String format(double value) {
        return String.format("%.4f", value);
    }
}
