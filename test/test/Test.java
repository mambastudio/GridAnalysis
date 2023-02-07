/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import gridanalysis.coordinates.Vec2i;
import gridanalysis.utilities.Utility;

/**
 *
 * @author jmburu
 */
public class Test {
    public static void main(String... args)
    {
        Vec2i grid = new Vec2i(5, 5);
        int i = 24;
        
        Vec2i gridCoord = Utility.getGridCoord(i, grid);
        
        int x = gridCoord.x;
        int y = gridCoord.y;
        
        System.out.println("x: " +x+ " y: " +y);
        System.out.println("index: " +Utility.getGridIndex(x, y, grid));
    }
}
