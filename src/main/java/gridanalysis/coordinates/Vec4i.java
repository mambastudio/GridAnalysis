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
public class Vec4i {
    public int x, y, z, w;
    
    public Vec4i(){x = y = z = w = 0;}    
    public Vec4i(int x, int y){this(x, y, 0, 0);}    
    public Vec4i(int x, int y, int z, int w){this.x = x; this.y = y; this.z = z; this.w = w;}
    
    public Vec4i copy(){return new Vec4i(x, y, z, w);}
}
