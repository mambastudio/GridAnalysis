/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

import static gridanalysis.irreg.Float2.max;
import static gridanalysis.irreg.Float2.min;
import static gridanalysis.irreg.Float2.sub;

/**
 *
 * @author user
 */
public class BBox2 {
    public Float2 min, max;
    public BBox2() {min = new Float2(Float.MAX_VALUE);  max = new Float2(-Float.MAX_VALUE);}
    public BBox2(Float2 f){min = (f); max = (f);}
    public BBox2(Float2 min, Float2 max){this.min=min; this.max=max;}

    public BBox2 extend(Float2 f) {        
        min = min(min, f);
        max = max(max, f);
        return this;
    }

    public BBox2 extend(BBox2 bb) {
        min = min(min, bb.min);
        max = max(max, bb.max);
        return this;
    }

    public BBox2 overlap(BBox2 bb) {
        min = max(min, bb.min);
        max = min(max, bb.max);
        return this;
    }
    
     public Float2 extents(){
        return sub(max, min);
    }

    public float half_area() {
        Float2 len = sub(max, min);
        float kx = Math.max(len.x, 0.0f);
        float ky = Math.max(len.y, 0.0f);       
        return kx * ky;
    }

    boolean is_empty() {
        return min.x > max.x || min.y > max.y;
    }

    boolean is_inside(Float2 f) {
        return f.x >= min.x && f.y >= min.y &&
               f.x <= max.x && f.y <= max.y;
    }

    boolean is_overlapping(BBox2 bb) {
        return min.x <= bb.max.x && max.x >= bb.min.x &&
               min.y <= bb.max.y && max.y >= bb.min.y;
    }

    boolean is_included(BBox2 bb) {
        return min.x >= bb.min.x && max.x <= bb.max.x &&
               min.y >= bb.min.y && max.y <= bb.max.y;
    }

    boolean is_strictly_included(BBox2 bb) {
        return is_included(bb) &&
               (min.x > bb.min.x || max.x < bb.max.x ||
                min.y > bb.min.y || max.y < bb.max.y);
    }
    
    @Override
    public final String toString() {
        return String.format("(%.2f, %.2f) to (%.2f, %.2f)", min.x, min.y, max.x, max.y);
    }

    static BBox2 empty() { return new BBox2(new Float2(Float.MAX_VALUE),  new Float2(-Float.MAX_VALUE)); }
    static BBox2 full()  { return new BBox2(new Float2(-Float.MAX_VALUE), new Float2(Float.MAX_VALUE)); }
}
