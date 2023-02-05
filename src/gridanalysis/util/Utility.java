/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.util;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.gridclasses.Tri;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class Utility {
    public static ArrayList<Tri> generateTriangles(int nTriangles, Vec2f min, Vec2f max)
    {
        ArrayList<Tri> triangles = new ArrayList();
        for(int i = 0; i<nTriangles; i++)
        {
            float x1 = randomFloat(min.x, max.x);
            float y1 = randomFloat(min.y, max.y);
            float x2 = randomFloat(min.x, max.x);
            float y2 = randomFloat(min.y, max.y);
            float x3 = randomFloat(min.x, max.x);
            float y3 = randomFloat(min.y, max.y);
            
            triangles.add(new Tri(new Vec2f(x1, y1), new Vec2f(x2, y2), new Vec2f(x3, y3)));
        }
        
        return triangles;
    }
    
    private static float randomFloat(float min, float max)
    {
        return (float) (min + Math.random() * (max - min));
    }
}
