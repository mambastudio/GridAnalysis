/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.gridclasses;

import gridanalysis.coordinates.Vec2f;

/**
 *
 * @author user
 */
public class Tri {
    public Vec2f v0; public float nx;
    public Vec2f e1; public float ny;
    public Vec2f e2; 

    public Tri(Vec2f v0, Vec2f v1, Vec2f v2) {
        init(v0, v1, v2);
    }
        
    private void init(Vec2f v0, Vec2f v1, Vec2f v2)
    {
        this.e1 = v0.sub(v1);
        this.e2 = v2.sub(v0);
        this.v0 = v0;
    }
    
    public Vec2f normal()
    {
        return new Vec2f();
    }
    
    public BBox bbox(){
        Vec2f v1 = v0.sub(e1);
        Vec2f v2 = v0.add(e2);
        return new BBox(Vec2f.min(v0, Vec2f.min(v1, v2)), Vec2f.max(v0, Vec2f.max(v1, v2)));    
    }
    
    public boolean plane_overlap_box(Vec2f n, float d, Vec2f min, Vec2f max)
    {
        Vec2f first = new Vec2f(
                        n.x > 0 ? min.x : max.x,
                        n.y > 0 ? min.y : max.y);

        Vec2f last = new Vec2f(
                        n.x <= 0 ? min.x : max.x,
                        n.y <= 0 ? min.y : max.y);
        
        float d0 = Vec2f.dot(n, first) - d;
        float d1 = Vec2f.dot(n, last)  - d;
        
        return d1 * d0 <= 0.0f;
    }
    
    public boolean axis_test_z( Vec2f half_size,
                                Vec2f e, Vec2f f,
                                Vec2f v0, Vec2f v1) {
        float p0 = e.x * v0.y - e.y * v0.x;
        float p1 = e.x * v1.y - e.y * v1.x;
        float rad = f.y * half_size.x + f.x * half_size.y;
        return Math.min(p0, p1) > rad | Math.max(p0, p1) < -rad;
    }
    
    public boolean intersect_tri_box(boolean bounds_check, boolean cross_axes, Vec2f v0, Vec2f e1,  Vec2f e2, Vec2f n, Vec2f min,  Vec2f max) {
        if (!plane_overlap_box(n, Vec2f.dot(v0, n), min, max))
            return false;
        
        Vec2f v1 = v0.sub(e1);
        Vec2f v2 = v0.add(e2);
        if (bounds_check) {
            float min_x = Math.min(v0.x, Math.min(v1.x, v2.x));
            float max_x = Math.max(v0.x, Math.max(v1.x, v2.x));
            if (min_x > max.x | max_x < min.x) return false;

            float min_y = Math.min(v0.y, Math.min(v1.y, v2.y));
            float max_y = Math.max(v0.y, Math.max(v1.y, v2.y));
            if (min_y > max.y | max_y < min.y) return false;
        }
        
        if (cross_axes) {
            Vec2f center    = max.add(min).mul(0.5f);
            Vec2f half_size = max.sub(min).mul(0.5f);

            Vec2f w0 = v0.sub(center);
            Vec2f w1 = v1.sub(center);
            Vec2f w2 = v2.sub(center);
            
            Vec2f f1 = new Vec2f(Math.abs(e1.x), Math.abs(e1.y));
            if (axis_test_z(half_size, e1, f1, w1, w2))
                return false;

            Vec2f f2 = new Vec2f(Math.abs(e2.x), Math.abs(e2.y));
            if (axis_test_z(half_size, e2, f2, w1, w2))
                return false;

            Vec2f e3 = e1.add(e2);

            Vec2f f3 = new Vec2f(Math.abs(e3.x), Math.abs(e3.y));
            if (axis_test_z(half_size, e3, f3, w0, w1))
                return false;
        }
        return true;
    }
    
    public boolean intersect_prim_cell(Tri tri, BBox bbox) {
        return intersect_tri_box(false, true, tri.v0, tri.e1, tri.e2, tri.normal(), bbox.min, bbox.max);
    }

}
