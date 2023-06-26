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
            System.out.println(tri);
            triangles.add(tri);
        }
        
        //triangles.add(new Tri(new Vec2f(211.79f, 183.65f), new Vec2f(71.00f, 496.78f), new Vec2f(8.36f, 144.94f)));
        //triangles.add(new Tri(new Vec2f(388.88f, 116.95f), new Vec2f(172.85f, 77.25f), new Vec2f(7.38f, 28.90f)));
        
        //triangles.add(new Tri(new Vec2f(401.03f, 170.69f), new Vec2f(430.96f, 10.98f), new Vec2f(5.63f, 33.08f)));
        //triangles.add(new Tri(new Vec2f(417.81f, 299.59f), new Vec2f(385.65f, 185.33f), new Vec2f(488.00f, 468.06f)));
        
        return triangles;
    }
    
    public static ArrayList<Tri> generateSingleTriangle(int nTriangles, Vec2f min, Vec2f max)
    {
        ArrayList<Tri> triangles = new ArrayList();
        
        Tri tri = new Tri(new Vec2f(0, 200), new Vec2f(200, 200), new Vec2f(200, 0));
        triangles.add(tri);
        
        return triangles;
    }
    
    public static ArrayList<Tri> generateTwoTriangles(int nTriangles, Vec2f min, Vec2f max)
    {
        ArrayList<Tri> triangles = new ArrayList();
        float x = 100;
        
        triangles.add(new Tri(new Vec2f(370.77f + x, 330.81f), new Vec2f(316.49f + x, 137.53f), new Vec2f(392.41f + x, 180.43f)));
        triangles.add(new Tri(new Vec2f(74.20f, 85.51f), new Vec2f(77.92f, 321.43f), new Vec2f(218.57f, 6.09f)));
        
        return triangles;
    }
    
    public static ArrayList<Tri> generateDefinedTriangles(int nTriangles, Vec2f min, Vec2f max)
    {
        ArrayList<Tri> triangles = new ArrayList();
        float x = 100;
        
        //triangles.add(new Tri(new Vec2f(484.21f, 343.04f), new Vec2f(272.36f, 388.51f), new Vec2f(252.07f, 459.40f)));
        //triangles.add(new Tri(new Vec2f(297.17f, 348.41f), new Vec2f(92.83f, 262.06f), new Vec2f(387.04f, 233.84f)));
        triangles.add(new Tri(new Vec2f(76.76f, 74.55f), new Vec2f(203.25f, 21.29f), new Vec2f(85.96f, 362.30f)));
        triangles.add(new Tri(new Vec2f(242.88f, 238.59f), new Vec2f(101.71f, 436.95f), new Vec2f(431.02f, 165.62f)));
        return triangles;
    }
    
    public static ArrayList<MTriangle> generateTriangles(GraphicsContext context, ArrayList<Tri> tris, int nTriangles, Vec2f min, Vec2f max)
    {
        ArrayList<Tri> triangles = generateDefinedTriangles(nTriangles, min, max);
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
    
    public static Vec2f getCellSize(Vec2i dims, BBox bound)
    {
        return bound.extents().div(new Vec2f(dims));
    }
}
