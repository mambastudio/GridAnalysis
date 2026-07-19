package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.MEngine;
import gridanalysis.jfx.shape.MCellInfo;
import gridanalysis.utilities.list.IntegerList;

/**
 * Two-dimensional port of Hagrid's cell expansion optimization.
 *
 * Expansion changes only cell exit bounds. Voxel-map ownership and primitive
 * references remain unchanged. An expansion is valid when every cell across
 * the crossed edge contains a subset of the current cell's references.
 */
public final class Expand extends GridAbstracts {
    private static final int AXES = 2;

    private final MEngine engine;
    private final Hagrid hagrid;

    public Expand(MEngine engine, Hagrid hagrid) {
        this.engine = engine;
        this.hagrid = hagrid;
    }

    public void expand_grid(Tri[] triangles, int iterations) {
        expand(hagrid.getIrregularGrid(), triangles, iterations, hagrid.partial_expansion);
        engine.setMCellInfo(MCellInfo.getCells(
                engine,
                hagrid.getIrregularGrid(),
                hagrid.getIrregularGrid().bbox,
                hagrid.getIrregularGrid().dims,
                hagrid.getIrregularGrid().shift));
    }

    /** Package-visible entry point used by focused algorithm tests. */
    static void expand(Grid grid, int iterations) {
        expand(grid, null, iterations, false);
    }

    static void expand(Grid grid, Tri[] triangles, int iterations, boolean partialExpansion) {
        if (iterations <= 0 || grid.num_cells == 0) {
            return;
        }
        Session session = new Session(grid, triangles, partialExpansion);
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int axis = 0; axis < AXES; axis++) {
                session.stepAxis(axis);
            }
        }
        session.apply();
    }

    /** Stateful expansion runner used by the visual debugger and the full build. */
    public static final class Session {
        private final Grid grid;
        private final Tri[] triangles;
        private final boolean partialExpansion;
        private final IntegerList cellFlags;
        private Cell[] current;

        public Session(Grid grid, Tri[] triangles, boolean partialExpansion) {
            if (partialExpansion && triangles == null) {
                throw new IllegalArgumentException("Triangles are required for partial expansion");
            }
            this.grid = grid;
            this.triangles = triangles;
            this.partialExpansion = partialExpansion;
            this.current = copyCells(grid);
            this.cellFlags = new IntegerList(new int[grid.num_cells]);
            this.cellFlags.fill((1 << AXES) - 1);
        }

        public void stepAxis(int axis) {
            if (axis < 0 || axis >= AXES) {
                throw new IllegalArgumentException("axis must be 0 (X) or 1 (Y)");
            }
            Cell[] next = new Cell[current.length];
            int axisBit = 1 << axis;
            for (int id = 0; id < current.length; id++) {
                Cell cell = current[id].copy();
                if ((cellFlags.get(id) & axisBit) != 0) {
                    ContinueFlag continueFlag = new ContinueFlag();
                    int lower = findOverlap(grid, triangles, current, cell, axis, false,
                            partialExpansion, continueFlag);
                    int upper = findOverlap(grid, triangles, current, cell, axis, true,
                            partialExpansion, continueFlag);
                    setMin(cell, axis, getMin(cell, axis) + lower);
                    setMax(cell, axis, getMax(cell, axis) + upper);
                    cellFlags.set(id, (continueFlag.value ? axisBit : 0)
                            | (cellFlags.get(id) & ~axisBit));
                }
                next[id] = cell;
            }
            current = next;
        }

        public void apply() {
            for (int i = 0; i < current.length; i++) {
                grid.cells.set(i, current[i].copy());
            }
        }

        public Cell getCell(int id) {
            return current[id].copy();
        }

        public int size() {
            return current.length;
        }
    }

    private static int findOverlap(Grid grid, Tri[] triangles, Cell[] cells, Cell cell,
            int axis, boolean positive, boolean partialExpansion,
            ContinueFlag continueFlag) {
        Vec2i gridDims = grid.grid_dims();
        int boundary = positive ? getMax(cell, axis) : getMin(cell, axis);
        if ((positive && boundary >= gridDims.get(axis))
                || (!positive && boundary <= 0)) {
            return 0;
        }

        int otherAxis = 1 - axis;
        int distance = positive ? gridDims.get(axis) : -gridDims.get(axis);
        int maxDistance = distance;
        int cursor = getMin(cell, otherAxis);

        while (cursor < getMax(cell, otherAxis)) {
            Vec2i neighborVoxel = new Vec2i();
            set(neighborVoxel, axis, positive ? getMax(cell, axis) : getMin(cell, axis) - 1);
            set(neighborVoxel, otherAxis, cursor);

            int neighborId = lookup_entry(
                    grid.entries,
                    grid.shift,
                    grid.dims,
                    neighborVoxel);
            Cell neighbor = cells[neighborId];

            maxDistance = positive
                    ? Math.min(maxDistance, getMax(neighbor, axis) - getMax(cell, axis))
                    : Math.max(maxDistance, getMin(neighbor, axis) - getMin(cell, axis));
            distance = positive
                    ? Math.min(distance, maxDistance)
                    : Math.max(distance, maxDistance);

            if (!partialExpansion) {
                if (!isSubset(grid.ref_ids, cell, neighbor)) {
                    distance = 0;
                    break;
                }
            } else if (neighbor.begin < neighbor.end) {
                BBox cellBounds = grid.cellbound(cell);
                distance = limitByAdditionalPrimitives(
                        grid, triangles, cell, neighbor, cellBounds,
                        axis, positive, distance);
                if (distance == 0) {
                    break;
                }
            }

            int step = getMax(neighbor, otherAxis) - cursor;
            if (step <= 0) {
                throw new IllegalStateException("Voxel map returned a neighbor that does not advance across the cell edge");
            }
            cursor += step;
        }

        continueFlag.value |= distance == maxDistance;
        return distance;
    }

    /**
     * Aggressive expansion: primitives already present in the current cell do
     * not constrain it. Neighbor-only primitives limit the expansion to the
     * first virtual-grid boundary at which they may become relevant.
     */
    private static int limitByAdditionalPrimitives(Grid grid, Tri[] triangles,
            Cell cell, Cell neighbor, BBox cellBounds,
            int axis, boolean positive, int distance) {
        int currentRef = cell.begin;
        for (int neighborRef = neighbor.begin; neighborRef < neighbor.end; neighborRef++) {
            int primitiveId = grid.ref_ids.get(neighborRef);

            while (currentRef < cell.end
                    && grid.ref_ids.get(currentRef) < primitiveId) {
                currentRef++;
            }
            if (currentRef < cell.end
                    && grid.ref_ids.get(currentRef) == primitiveId) {
                continue;
            }

            distance = computeOverlap(
                    grid, triangles[primitiveId], cell, cellBounds,
                    axis, positive, distance);
            if (distance == 0) {
                break;
            }
        }
        return distance;
    }

    private static int computeOverlap(Grid grid, Tri primitive, Cell cell,
            BBox cellBounds, int axis, boolean positive, int distance) {
        int otherAxis = 1 - axis;
        BBox primitiveBounds = primitive.bbox();

        if (primitiveBounds.min.get(otherAxis) <= cellBounds.max.get(otherAxis)
                && primitiveBounds.max.get(otherAxis) >= cellBounds.min.get(otherAxis)) {
            Vec2f gridInverse = grid.grid_inv();
            float primitiveBoundary = positive
                    ? primitiveBounds.min.get(axis)
                    : primitiveBounds.max.get(axis);
            int primitiveVoxel = (int) ((primitiveBoundary - grid.bbox.min.get(axis))
                    * gridInverse.get(axis));

            distance = positive
                    ? Math.min(distance, primitiveVoxel - getMax(cell, axis))
                    : Math.max(distance, primitiveVoxel - getMin(cell, axis) + 1);
            distance = positive ? Math.max(distance, 0) : Math.min(distance, 0);
        }
        return distance;
    }

    /** Returns whether the neighbor references are a subset of the cell references. */
    private static boolean isSubset(IntegerList refs, Cell cell, Cell neighbor) {
        int cellCount = cell.end - cell.begin;
        int neighborCount = neighbor.end - neighbor.begin;
        if (neighborCount > cellCount) {
            return false;
        }
        if (neighborCount == 0) {
            return true;
        }

        int i = cell.begin;
        int j = neighbor.begin;
        while (i < cell.end && j < neighbor.end) {
            int a = refs.get(i);
            int b = refs.get(j);
            if (b < a) {
                return false;
            }
            if (a == b) {
                j++;
            }
            i++;
        }
        return j == neighbor.end;
    }

    private static Cell[] copyCells(Grid grid) {
        Cell[] result = new Cell[grid.num_cells];
        for (int i = 0; i < result.length; i++) {
            result[i] = grid.cells.get(i).copy();
        }
        return result;
    }

    private static int getMin(Cell cell, int axis) {
        return cell.min.get(axis);
    }

    private static int getMax(Cell cell, int axis) {
        return cell.max.get(axis);
    }

    private static void setMin(Cell cell, int axis, int value) {
        set(cell.min, axis, value);
    }

    private static void setMax(Cell cell, int axis, int value) {
        set(cell.max, axis, value);
    }

    private static void set(Vec2i value, int axis, int component) {
        if (axis == 0) {
            value.x = component;
        } else {
            value.y = component;
        }
    }

    private static final class ContinueFlag {
        boolean value;
    }
}
