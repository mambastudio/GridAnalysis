/*
 * The MIT License
 *
 * Copyright 2022 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package gridanalysis.jfx.math;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author user
 */
public interface MTransformGeneric {
    
    public void transformGraphicsContext(GraphicsContext graphicsContext);
    
    /**
     * This is fundamental when combining different matrix
     * 
     * MTransform t = MTransform.scale(2, 2).
     *                  createConcatenation(MTransform.translate(20, 20)).
     *                  createConcatenation(MTransform.scale(5, 2));
     * 
     * In the following sequence first you: scale -> translate -> scale
     * 
     * @param transform
     * @return 
     */   
    public MTransformGeneric createConcatenation(MTransformGeneric transform);
    
    public Bounds transform(Bounds bound);
    public Bounds inverseTransform(Bounds bound);
            
    public Point2D transform(Point2D point);
    public Point2D deltaTransform(Point2D point);
    
    public Point2D inverseTransform(Point2D point);
    public Point2D deltaInverseTransform(Point2D point);
    
    public MMatrix4 getMatrix();
    public MMatrix4 getInverseMatrix();
    
    public MTransformGeneric inverseTransform();
    
    
    default MTransform asMTransform()
    {
        return new MTransform(getMatrix(), getInverseMatrix());
    }
}
