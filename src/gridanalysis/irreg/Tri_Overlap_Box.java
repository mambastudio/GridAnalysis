/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Float2.add;
import static gridanalysis.irreg.Float2.dot;
import static gridanalysis.irreg.Float2.mul;
import static gridanalysis.irreg.Float2.sub;

/**
 *
 * @author user
 */
public class Tri_Overlap_Box {
    /// Tests for intersection between a plane and a box.
    public static boolean plane_overlap_box( Float2 n, float d,  Float2 min,  Float2 max) {
        Float2 first = new Float2(), last = new Float2();

        first.x = n.x > 0 ? min.x : max.x;
        first.y = n.y > 0 ? min.y : max.y;

        last.x = n.x <= 0 ? min.x : max.x;
        last.y = n.y <= 0 ? min.y : max.y;

        float d0 = dot(n, first) - d;
        float d1 = dot(n, last)  - d;
        return d0 * d1 <= 0.0f;
    }

    public static boolean axis_test_x( Float2 half_size,
                             Float2 e,  Float2 f,
                             Float2 v0,  Float2 v1) {
        float p0 = 0;
        float p1 = 0;
        float rad = 0;
        return Math.min(p0, p1) <= rad && Math.max(p0, p1) >= -rad;
    }

    public static boolean axis_test_y( Float2 half_size,
                             Float2 e,  Float2 f,
                             Float2 v0,  Float2 v1) {
         float p0 = 0;
         float p1 = 0;
         float rad = 0;
        return Math.min(p0, p1) <= rad && Math.max(p0, p1) >= -rad;
    }

    public static boolean axis_test_z( Float2 half_size,
                             Float2 e,  Float2 f,
                             Float2 v0,  Float2 v1) {
        float p0 = e.x * v0.y - e.y * v0.x;
        float p1 = e.x * v1.y - e.y * v1.x;
        float rad = f.y * half_size.x + f.x * half_size.y;
        return Math.min(p0, p1) <= rad && Math.max(p0, p1) >= -rad;
    }

    /// Tests for intersection between a box and triangle
    //template <bool bounds_check = true, bool cross_checks = true>
    public static boolean tri_overlap_box(boolean bounds_check, boolean cross_checks, Float2 v0,  Float2 e1,  Float2 e2,  Float2 n,  Float2 min,  Float2 max) {
        if (!plane_overlap_box(n, dot(v0, n), min, max))
            return false;

        Float2 v1 = sub(v0, e1);
        Float2 v2 = add(v0, e2);

        if (bounds_check) {
            float min_x = Math.min(v0.x, Math.min(v1.x, v2.x));
            float max_x = Math.max(v0.x, Math.max(v1.x, v2.x));
            if (min_x > max.x || max_x < min.x) return false;

            float min_y = Math.min(v0.y, Math.min(v1.y, v2.y));
            float max_y = Math.max(v0.y, Math.max(v1.y, v2.y));
            if (min_y > max.y || max_y < min.y) return false;
        }

        if (cross_checks) {
            Float2 center    = mul(add(max, min), 0.5f);
            Float2 half_size = mul(add(max, min), 0.5f);

            Float2 w0 = sub(v0, center);
            Float2 w1 = sub(v1, center);
            Float2 w2 = sub(v2, center);

            Float2 f1 = new Float2(Math.abs(e1.x), Math.abs(e1.y));
            if (!axis_test_x(half_size, e1, f1, w0, w2)) return false;
            if (!axis_test_y(half_size, e1, f1, w0, w2)) return false;
            if (!axis_test_z(half_size, e1, f1, w1, w2)) return false;

            Float2 f2 = new Float2(Math.abs(e2.x), Math.abs(e2.y));
            if (!axis_test_x(half_size, e2, f2, w0, w1)) return false;
            if (!axis_test_y(half_size, e2, f2, w0, w1)) return false;
            if (!axis_test_z(half_size, e2, f2, w1, w2)) return false;

            Float2 e3 = add(e1, e2);

            Float2 f3 = new Float2(Math.abs(e3.x), Math.abs(e3.y));
            if (!axis_test_x(half_size, e3, f3, w0, w2)) return false;
            if (!axis_test_y(half_size, e3, f3, w0, w2)) return false;
            if (!axis_test_z(half_size, e3, f3, w0, w1)) return false;
        }

        return true;
    }

}
