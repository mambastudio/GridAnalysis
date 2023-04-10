/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.coordinates;

/**
 *
 * @author user
 */
public class Vec2i {
    public int x, y;
    
    
    public Vec2i(){}
    public Vec2i(int xy){this.x = xy; this.y = xy;}
    public Vec2i(int x, int y){this.x = x; this.y = y;}
    public Vec2i(Vec2i v){this.x = v.x; this.y = v.y;}
    public Vec2i(Vec2f xy){this.x = (int) xy.x; this.y = (int) xy.y;}
    
    public static Vec2i min( Vec2i a,  Vec2i b){ return new Vec2i(Math.min(a.x, b.x), Math.min(a.y, b.y));}
    public static Vec2i max( Vec2i a,  Vec2i b) { return new Vec2i(Math.max(a.x, b.x), Math.max(a.y, b.y)); }
    
    public static Vec2i clamp(Vec2i a, Vec2i b, Vec2i c){return new Vec2i(Math.min(Math.max(a.x, b.x), c.x), Math.min(Math.max(a.y, b.y), c.y));}
    
    public Vec2i mul(Vec2i a){return new Vec2i(x * a.x, y * a.y); }
    public Vec2i mul(int a){return new Vec2i(x * a, y * a); }
    public Vec2i div(Vec2i a){return new Vec2i(x / a.x, y / a.y); }
    public Vec2i div(int a){return new Vec2i(x / a, y / a); }
    public Vec2i add(Vec2i a){return new Vec2i(x + a.x, y + a.y); }
    public Vec2i add(int a){return new Vec2i(x + a, y + a); }
    public Vec2i sub(Vec2i a){return new Vec2i(x - a.x, y - a.y); }
    public Vec2i sub(int a){return new Vec2i(x - a, y - a); }
    public Vec2i neg(){return new Vec2i(-x, -y);}
    
    public Vec2i rightShift(int shift){return new Vec2i(x >> shift, y >> shift);}
    public Vec2i leftShift(int shift){return new Vec2i(x << shift, y << shift);}
    public Vec2i and(int shift){return new Vec2i(x & shift, y & shift);
    }
    
    public Vec2i copy(){return new Vec2i(x, y);}
    
    public int get(int axis) {
        if (axis == 0) 
            return x;       
        else 
            return y;
    }
    
    @Override
    public final String toString() {
        return String.format("(%3d, %3d)", x, y);
    }
}
