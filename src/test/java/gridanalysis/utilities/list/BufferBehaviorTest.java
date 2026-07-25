package gridanalysis.utilities.list;

import java.util.Arrays;

/** Focused behavioral checks for construction buffers and their shared slices. */
public final class BufferBehaviorTest {
    public static void main(String[] args) {
        sharedIntegerSlice();
        prefixScan();
        pairedSort();
        stablePartition();
        rootSwap();
        sharedObjectSlice();
    }

    private static void sharedIntegerSlice() {
        IntegerList root = new IntegerList(new int[] {1, 2, 3, 4});
        IntegerList slice = root.getSubList(1, 3);
        slice.set(0, 20);
        check(root.get(1) == 20, "integer slice must share storage");
    }

    private static void prefixScan() {
        IntegerList values = new IntegerList(new int[] {1, 2, 3, 4});
        int total = values.prefixSum();
        check(total == 10, "prefix sum total");
        check(Arrays.equals(values.trimCopy(), new int[] {1, 3, 6, 10}),
                "inclusive prefix scan");
    }

    private static void pairedSort() {
        IntegerList keys = new IntegerList(new int[] {3, 1, 2});
        IntegerList values = new IntegerList(new int[] {30, 10, 20});
        keys.sort_pairs(values);
        check(Arrays.equals(keys.trimCopy(), new int[] {1, 2, 3}), "sorted keys");
        check(Arrays.equals(values.trimCopy(), new int[] {10, 20, 30}), "paired values");
    }

    private static void stablePartition() {
        IntegerList input = new IntegerList(new int[] {10, 11, 12, 13});
        IntegerList flags = new IntegerList(new int[] {0, 1, 0, 1});
        IntegerList output = new IntegerList(new int[4]);
        int selected = input.partition_stable(output, flags);
        check(selected == 2, "partition selected count");
        check(Arrays.equals(output.trimCopy(), new int[] {11, 13, 10, 12}),
                "stable partition order");
    }

    private static void rootSwap() {
        IntegerList left = new IntegerList(new int[] {1, 2});
        IntegerList right = new IntegerList(new int[] {3, 4});
        left.swap(right);
        check(Arrays.equals(left.trimCopy(), new int[] {3, 4}), "left swapped storage");
        check(Arrays.equals(right.trimCopy(), new int[] {1, 2}), "right swapped storage");
    }

    private static void sharedObjectSlice() {
        ObjectList<String> root = new ObjectList<>(new String[] {"a", "b", "c"});
        ObjectList<String> slice = root.getSubList(1, 3);
        slice.set(0, "B");
        check("B".equals(root.get(1)), "object slice must share storage");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
