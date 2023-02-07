/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * && open the template in the editor.
 */
package gridanalysis.utilities;

import gridanalysis.coordinates.Vec2f;
import gridanalysis.coordinates.Vec2i;
import gridanalysis.gridclasses.BBox;
import gridanalysis.gridclasses.Tri;
import gridanalysis.jfx.shape.MTriangle;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;

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
            Tri tri = generate_random_triangle(min, max);            
            triangles.add(tri);
        }
        
        return triangles;
    }
    
    public static ArrayList<MTriangle> generateTriangles(GraphicsContext context, ArrayList<Tri> tris, int nTriangles, Vec2f min, Vec2f max)
    {
        ArrayList<Tri> triangles = generateTriangles(nTriangles, min, max);
        tris.addAll(triangles);
        
        ArrayList<MTriangle> mtriangles = new ArrayList();
        
        triangles.forEach(tri -> {
            mtriangles.add(new MTriangle(context, tri));
        });
        return mtriangles;
    }
    
    public static Tri generate_random_triangle(Vec2f triangle_min, Vec2f triangle_max)
    {
        float x1 = randomFloat(triangle_min.x, triangle_max.x);
        float y1 = randomFloat(triangle_min.y, triangle_max.y);
        float x2 = randomFloat(triangle_min.x, triangle_max.x);
        float y2 = randomFloat(triangle_min.y, triangle_max.y);
        float x3 = randomFloat(triangle_min.x, triangle_max.x);
        float y3 = randomFloat(triangle_min.y, triangle_max.y);

        return new Tri(new Vec2f(x1, y1), new Vec2f(x2, y2), new Vec2f(x3, y3));
    }
    
    private static float randomFloat(float min, float max)
    {
        return (float) (min + Math.random() * (max - min));
    }
    
    public static Vec2i getGridCoord(int i, Vec2i grid)
    {
        int x = i % grid.x;
        int y = (i / grid.x) % grid.y;
        
        return new Vec2i(x, y);
    }
    
    public static int getGridIndex(int x, int y, Vec2i grid)
    {
        return x + grid.x * y;
    }
    
    public static Vec2f getBox(Vec2i dims, BBox bound)
    {
        return bound.extents().div(new Vec2f(dims));
    }
}
