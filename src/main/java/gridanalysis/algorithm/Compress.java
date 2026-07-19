package gridanalysis.algorithm;

import gridanalysis.coordinates.UShort2;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.Grid;
import gridanalysis.gridclasses.SmallCell;
import gridanalysis.utilities.list.IntegerList;

/** Converts full cells to 16-bit bounds and sentinel-terminated references. */
public final class Compress {
    private Compress() {
    }

    public record Result(SmallCell[] cells, IntegerList references) {
    }

    /** Compresses a completed grid in place, returning false when 16-bit bounds cannot represent it.
     * @param grid
     * @return  */
    public static boolean compress(Grid grid) {
        if (grid.small_cells != null) return true;
        if (grid.cells == null) return false;

        Cell[] cells = new Cell[grid.num_cells];
        for (int i = 0; i < cells.length; i++) cells[i] = grid.cells.get(i);
        Result result = compress(cells, grid.ref_ids, grid.grid_dims());
        if (result == null) return false;

        grid.small_cells = result.cells();
        grid.ref_ids = result.references();
        grid.num_refs = grid.ref_ids.size();
        grid.cells = null;
        return true;
    }

    /** Creates a compressed representation without modifying its source cells or references.
     * @param cells
     * @param references
     * @param dimensions
     * @return  */
    public static Result compress(Cell[] cells, IntegerList references, Vec2i dimensions) {
        if (dimensions.x > UShort2.MAX_VALUE || dimensions.y > UShort2.MAX_VALUE) {
            return null;
        }

        int sentinelCount = 0;
        for (Cell cell : cells) {
            int count = cell.end - cell.begin;
            if (count > 0) sentinelCount += count + 1;
        }

        int[] packedReferences = new int[sentinelCount];
        SmallCell[] smallCells = new SmallCell[cells.length];
        int cursor = 0;
        for (int id = 0; id < cells.length; id++) {
            Cell cell = cells[id];
            int count = cell.end - cell.begin;
            int begin = count > 0 ? cursor : -1;
            smallCells[id] = new SmallCell(
                    UShort2.from(cell.min), UShort2.from(cell.max), begin);

            for (int i = cell.begin; i < cell.end; i++) {
                packedReferences[cursor++] = references.get(i);
            }
            if (count > 0) packedReferences[cursor++] = -1;
        }
        return new Result(smallCells, new IntegerList(packedReferences));
    }
}
