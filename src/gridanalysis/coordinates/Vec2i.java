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
    
    public static Vec2i min( Vec2i a,  Vec2i b){ return new Vec2i(Math.min(a.x, b.x), Math.min(a.y, b.y));}
    public static Vec2i max( Vec2i a,  Vec2i b) { return new Vec2i(Math.max(a.x, b.x), Math.max(a.y, b.y)); }
    
    public Vec2i rightShift(int shift){return new Vec2i(x >> shift, y >> shift);}
    public Vec2i leftShift(int shift){return new Vec2i(x << shift, y << shift);}
    
    @Override
    public final String toString() {
        return String.format("(%1s, %1s)", x, y);
    }
}
