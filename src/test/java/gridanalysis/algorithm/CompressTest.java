package gridanalysis.algorithm;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.Cell;
import gridanalysis.gridclasses.SmallCell;
import gridanalysis.utilities.list.IntegerList;

/** Lightweight compression assertions runnable without a test framework. */
public final class CompressTest {
    public static void main(String[] args) {
        Cell[] cells = {
            new Cell(new Vec2i(0, 0), 0, new Vec2i(40000, 2), 2),
            new Cell(new Vec2i(40000, 0), 2, new Vec2i(65535, 2), 2)
        };
        Compress.Result result = Compress.compress(
                cells, new IntegerList(new int[] {3, 7}), new Vec2i(65535, 2));
        check(result != null, "16-bit grid should compress");

        SmallCell first = result.cells()[0];
        check(first.min().unsignedX() == 0, "minimum coordinate changed");
        check(first.max().unsignedX() == 40000, "unsigned coordinate changed");
        check(first.begin() == 0, "first reference offset changed");
        check(result.cells()[1].begin() == -1, "empty cell must use begin=-1");
        check(result.references().size() == 3, "sentinel reference count is wrong");
        check(result.references().get(0) == 3
                && result.references().get(1) == 7
                && result.references().get(2) == -1,
                "reference sentinel layout is wrong");
        check(Compress.compress(cells, new IntegerList(), new Vec2i(65536, 1)) == null,
                "coordinates wider than 16 bits must be rejected");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
