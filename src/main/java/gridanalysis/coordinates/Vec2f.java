/*
 * floato change this license header, choose License Headers in Project Properties.
 * floato change this template file, choose floatools | floatemplates
 * and open the template in the editor.
 */
package gridanalysis.coordinates;

import static java.lang.Math.sqrt;


/**
 *
 * @author user
 */
public class Vec2f {
    public float x, y;
    
    public Vec2f(){}
    public Vec2f(float xy){this.x = xy; this.y = xy;}
    public Vec2f(float x, float y){this.x = x; this.y = y;}
    public Vec2f(Vec2i xy){this.x = xy.x; this.y = xy.y;}
    
    public static Vec2f min( Vec2f a,  Vec2f b){ return new Vec2f(Math.min(a.x, b.x), Math.min(a.y, b.y));}
    public static Vec2f max( Vec2f a,  Vec2f b) { return new Vec2f(Math.max(a.x, b.x), Math.max(a.y, b.y)); }
    public static Vec2f clamp( Vec2f a, float b, float c) { return new Vec2f(Math.min(Math.max(a.x, b), c), Math.min(Math.max(a.y, b), c)); }
    public static float dot( Vec2f a,  Vec2f b) { return a.x * b.x + a.y * b.y; }
    public static float length( Vec2f a) { return (float) sqrt(dot(a, a)); }
    public static Vec2f normalize( Vec2f a) { return a.mul(1.0f / length(a)); }
    
    public Vec2f mul(Vec2f a){return new Vec2f(x * a.x, y * a.y); }
    public Vec2f mul(float a){return new Vec2f(x * a, y * a); }
    public Vec2f div(Vec2f a){return new Vec2f(x / a.x, y / a.y); }
    public Vec2f div(float a){return new Vec2f(x / a, y / a); }
    public Vec2f add(Vec2f a){return new Vec2f(x + a.x, y + a.y); }
    public Vec2f add(float a){return new Vec2f(x + a, y + a); }
    public Vec2f sub(Vec2f a){return new Vec2f(x - a.x, y - a.y); }
    public Vec2f sub(float a){return new Vec2f(x - a, y - a); }
    public Vec2f neg(){return new Vec2f(-x, -y);}
    
    public Vec2f copy(){return new Vec2f(x, y);}
    
    public float get(int axis) {
        if (axis == 0) 
            return x;       
        else 
            return y;
    }
    
    public static Vec2f cross( Vec2f a,  Vec2f b) {
        return new Vec2f(0, 0);
    }
    
    @Override
    public final String toString() {
        return String.format("(%.2ff, %.2ff)", x, y);
    }
}
