/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import gridanalysis.gridclasses.Range;
import static gridanalysis.irreg.Common.clamp;
import static gridanalysis.irreg.Float2.add;
import static gridanalysis.irreg.Float2.div;
import static gridanalysis.irreg.Float2.mul;
import gridanalysis.utilities.IntArray;
import static java.lang.Math.cbrt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 * @author user
 */
public class Grid2 {
    /// Counts the number of elements in the union of two sorted arrays.
    public static int count_union(IntArray p0, int c0, IntArray p1, int c1) {
        int i = 0;
        int j = 0;

        int count = 0;
        while (i < c0 & j < c1) {
            int k0 = p0.get(i) <= p1.get(j) ? 1 : 0;
            int k1 = p0.get(i) >= p1.get(j) ? 1 : 0;
            i += k0;
            j += k1;
            count++;
        }

        return count + (c1 - j) + (c0 - i);
    }
    
    /// Computes the dimensions of a grid using the formula : R{x, y, z} = e{x, y, z} * (N * d / V)^(1/3).
    public static void compute_grid_dims(Float2 e, int N, float d, int[] dims) {
        float V = e.x * e.y;
        float r = (float) (cbrt(d * N / V));
        dims[0] = Math.max(1, (int)(e.x * r));
        dims[1] = Math.max(1, (int)(e.y * r));
    }

    
    public static void compute_bboxes(GridInfo info, Tri2[] tris, BBox2[] bboxes) {
        info.bbox = IntStream.range(0, tris.length)
                .parallel()
                .mapToObj(i -> {
                    BBox2 box = tris[i].bbox();
                    bboxes[i] = box;
                    return box;
                }).reduce(BBox2.empty(), BBox2::extend, BBox2::extend);
    }
    
    /// Computes the range of voxels covered by a bounding box on a grid.
    public static Range2 find_coverage(BBox2 bb, Float2 inv_org, Float2 inv_size, int[] dims) {
        int lx = (int) clamp(bb.min.x * inv_size.x - inv_org.x, 0, dims[0] - 1);
        int ly = (int) clamp(bb.min.y * inv_size.y - inv_org.y, 0, dims[1] - 1);
        
        int hx = (int) clamp(bb.max.x * inv_size.x - inv_org.x, 0, dims[0] - 1);
        int hy = (int) clamp(bb.max.y * inv_size.y - inv_org.y, 0, dims[1] - 1);       

        return new Range2(lx, hx, ly, hy);
    }
    
    public static void gen_top_refs(
                        GridInfo info,
                        BBox2[] bboxes,
                        Tri2[] tris,
                        final ArrayList<Ref> refs) {
        Float2 cell_size = info.cell_size();
        Float2 inv_size  = div(1.0f, cell_size);
        Float2 inv_org   = mul(info.bbox.min, inv_size);

        IntList approx_ref_counts = new IntList(tris.length + 1);
        
        IntStream.range(0, tris.length)
                .parallel()
                .forEach(i->{
                    Range2 range = find_coverage(bboxes[i], inv_org, inv_size, info.dims);
                    approx_ref_counts.set(i + 1, range.size());
                });
        
        // Get the insertion position into the array of references
        approx_ref_counts.set(0, 0);
        approx_ref_counts.swap(approx_ref_counts.prefixSum());
        
        // Allocate and fill the array of references
        Common.clearFill(refs, approx_ref_counts.size(), ()-> new Ref(-1, -1, -1));
        IntStream.range(0, tris.length)
                .parallel()
                .forEach(i->{
                    Range2 cov = find_coverage(bboxes[i], inv_org, inv_size, info.dims);
                    Tri2 tri = tris[i];
                    
                    // Examine each cell and determine if the triangle is really inside
                    int index = approx_ref_counts.get(i);
                    cov.iterate((x, y)->{
                        Float2 cell_min = add(info.bbox.min, mul(cell_size, new Float2(x, y)));
                        //new Ref(i, x + info.dims[0] * y, 0)
                        Float2 cell_max = add(info.bbox.min, mul(cell_size, new Float2(x + 1, y + 1)));
                        if(Tri_Overlap_Box.tri_overlap_box(true, true, tri.v0, tri.e1, tri.e2, tri.normal(), cell_min, cell_max))
                            refs.set(index++, null);
                    });
                    
                });
    }
}
