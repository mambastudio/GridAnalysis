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
public class BBox {
    public Vec2f min, max;
    
    public BBox()
    {
        min = new Vec2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        max = new Vec2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }
    
    public BBox(Vec2f min, Vec2f max)
    {
        this.min = min; 
        this.max = max;
    }
    
    public BBox extend(BBox bb) {
        min = Vec2f.min(min, bb.min);
        max = Vec2f.max(max, bb.max);
        return this;
    }

    public BBox overlap(BBox bb) {
        min = Vec2f.max(min, bb.min);
        max = Vec2f.min(max, bb.max);
        return this;
    }

    public Vec2f extents(){
        return max.sub(min);
    }

    public Vec2f center() {
        return (max.add(min)).mul(0.5f);
    }
    
    public float half_area() {
        Vec2f len = max.sub(min);
        float kx = Math.max(len.x, 0.0f);
        float ky = Math.max(len.y, 0.0f);
        return kx * ky;
    }

    public boolean is_empty(){
        return min.x > max.x || min.y > max.y;
    }

    public boolean is_inside(Vec2f f) {
        return f.x >= min.x && f.y >= min.y && 
               f.x <= max.x && f.y <= max.y;
    }

    public boolean is_overlapping(BBox bb) {
        return min.x <= bb.max.x && max.x >= bb.min.x &&
               min.y <= bb.max.y && max.y >= bb.min.y;
    }

    public boolean is_included(BBox bb) {
        return min.x >= bb.min.x && max.x <= bb.max.x &&
               min.y >= bb.min.y && max.y <= bb.max.y;
    }

    public boolean is_strictly_included(BBox bb) {
        return is_included(bb) &&
               (min.x > bb.min.x || max.x < bb.max.x ||
                min.y > bb.min.y || max.y < bb.max.y);
    }
    
     @Override
    public final String toString() {
        return String.format("(%.2f, %.2f) to (%.2f, %.2f)", min.x, min.y, max.x, max.y);
    }
}
