package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.coordinates.Vec2f;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Entry;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.Tri;
import gridanalysis.utilities.list.IntegerList;
import gridanalysis.utilities.list.ObjectList;

/** Lightweight assertions runnable without an external test framework. */
public final class ExpandTest {
    public static void main(String[] args) {
        expandsAcrossCompleteVerticalEdge();
        rejectsExpansionWhenOneEdgeNeighborIsNotASubset();
        expandsAlongYUsingThePerpendicularXEdge();
        partialExpansionStopsAtNeighborOnlyPrimitive();
    }

    private static void expandsAcrossCompleteVerticalEdge() {
        Grid grid = grid(false);
        Expand.expand(grid, 1);
        check(grid.cells.get(0).max.x == 2, "cell should expand across both Y neighbors");
    }

    private static void rejectsExpansionWhenOneEdgeNeighborIsNotASubset() {
        Grid grid = grid(true);
        Expand.expand(grid, 1);
        check(grid.cells.get(0).max.x == 1, "one incompatible edge neighbor must reject the whole expansion");
    }

    private static void expandsAlongYUsingThePerpendicularXEdge() {
        Grid grid = new Grid();
        grid.dims = new Vec2i(2, 2);
        grid.shift = 0;
        grid.entries = new Entry[] {
            new Entry(0, 0), new Entry(0, 0),
            new Entry(0, 1), new Entry(0, 1)
        };
        grid.num_entries = grid.entries.length;
        grid.ref_ids = new IntegerList(new int[] { 7, 7 });
        grid.cells = new ObjectList<>(new Cell[] {
            new Cell(new Vec2i(0, 0), 0, new Vec2i(2, 1), 1),
            new Cell(new Vec2i(0, 1), 1, new Vec2i(2, 2), 2)
        });
        grid.num_cells = 2;

        Expand.expand(grid, 1);
        check(grid.cells.get(0).max.y == 2, "Y expansion must scan across X");
    }

    private static void partialExpansionStopsAtNeighborOnlyPrimitive() {
        Grid grid = new Grid();
        grid.bbox = new BBox(new Vec2f(0, 0), new Vec2f(4, 1));
        grid.dims = new Vec2i(4, 1);
        grid.shift = 0;
        grid.entries = new Entry[] {
            new Entry(0, 0), new Entry(0, 1),
            new Entry(0, 1), new Entry(0, 1)
        };
        grid.num_entries = grid.entries.length;
        grid.ref_ids = new IntegerList(new int[] { 0 });
        grid.cells = new ObjectList<>(new Cell[] {
            new Cell(new Vec2i(0, 0), 0, new Vec2i(1, 1), 0),
            new Cell(new Vec2i(1, 0), 0, new Vec2i(4, 1), 1)
        });
        grid.num_cells = 2;
        Tri[] triangles = {
            new Tri(new Vec2f(3.2f, 0.2f), new Vec2f(3.8f, 0.5f), new Vec2f(3.2f, 0.8f))
        };

        Expand.expand(grid, triangles, 1, true);
        check(grid.cells.get(0).max.x == 3,
                "partial expansion should stop at the neighbor-only primitive boundary");
    }


    private static Grid grid(boolean incompatibleUpperNeighbor) {
        Grid grid = new Grid();
        grid.dims = new Vec2i(3, 2);
        grid.shift = 0;
        grid.entries = new Entry[] {
            new Entry(0, 0), new Entry(0, 1), new Entry(0, 3),
            new Entry(0, 0), new Entry(0, 2), new Entry(0, 3)
        };
        grid.num_entries = grid.entries.length;
        grid.ref_ids = incompatibleUpperNeighbor
                ? new IntegerList(new int[] { 1, 2, 1, 3, 3 })
                : new IntegerList(new int[] { 1, 2, 1, 2, 3 });
        grid.cells = new ObjectList<>(new Cell[] {
            new Cell(new Vec2i(0, 0), 0, new Vec2i(1, 2), 2),
            new Cell(new Vec2i(1, 0), 2, new Vec2i(2, 1), 3),
            new Cell(new Vec2i(1, 1), 3, new Vec2i(2, 2), 4),
            new Cell(new Vec2i(2, 0), 4, new Vec2i(3, 2), 5)
        });
        grid.num_cells = 4;
        return grid;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
