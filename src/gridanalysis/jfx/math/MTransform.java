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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 *
 * @author user
 */
public class MTransform implements MTransformGeneric{
    private final MMatrix4 m;
    private final MMatrix4 mInv;
    
    public MTransform()
    {
        m = new MMatrix4();
        mInv = m.inverse();
    }
    
    public MTransform(MMatrix4 m, MMatrix4 mInv)
    {
        this.m = m;
        this.mInv = mInv;
    }
    
    @Override
    public void transformGraphicsContext(GraphicsContext graphicsContext)
    {
        graphicsContext.setTransform(
                m.getTransform().getMxx(), m.getTransform().getMyx(), m.getTransform().getMxy(),
                m.getTransform().getMyy(), m.getTransform().getTx(), m.getTransform().getTy());
    }
    
    public static MTransform translate(double x, double y)
    {
        return translate(new Point2D(x, y));
    }
    
    public static MTransform translate(Point2D delta) 
    {
        MMatrix4 m = new MMatrix4(
                Affine.translate(delta.getX(), delta.getY()));
        MMatrix4 minv = new MMatrix4(
                Affine.translate(delta.getX(), delta.getY()).createInverse());
        return new MTransform(m, minv);
    }
    
    public static MTransform scale(double x, double y)
    {
        return scale(new Point2D(x, y));
    }
    
    public static MTransform scale(Point2D scale) 
    {
        return scale(scale, Point2D.ZERO);
    }
    
    public static MTransform scale(double x, double y, double pivotX, double pivotY)
    {
        return scale(new Point2D(x, y), new Point2D(pivotX, pivotY));
    }
    
    public static MTransform scale(Point2D scale, Point2D pivot) 
    {
        try {
            MMatrix4 m = new MMatrix4(
                    Affine.scale(scale.getX(), scale.getY(), pivot.getX(), pivot.getY()));
            MMatrix4 mInv = new MMatrix4(
                    Affine.scale(scale.getX(), scale.getY(), pivot.getX(), pivot.getY()).createInverse());
            return new MTransform(m, mInv);
        } catch (NonInvertibleTransformException ex) {
            Logger.getLogger(MTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public Bounds transform(Bounds bound)
    {
        return m.getTransform().transform(bound);
    }
    
    @Override
    public Bounds inverseTransform(Bounds bound)
    {
        try {
            return m.getTransform().inverseTransform(bound);
        } catch (NonInvertibleTransformException ex) {
            Logger.getLogger(MTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public Point2D transform(Point2D point)
    {
        return m.getTransform().transform(point);
    }
    
    @Override
    public Point2D inverseTransform(Point2D point)
    {
        return mInv.getTransform().transform(point);
    }

    @Override
    public Point2D deltaTransform(Point2D point) { 
        return m.getTransform().deltaTransform(point);
    }

    @Override
    public Point2D deltaInverseTransform(Point2D point) {
        return mInv.getTransform().deltaTransform(point);
    }
        
    /**
     * This is fundamental when combining different matrix
     * 
     * MTransform t = MTransform.scale(2, 2).
     *                  createConcatenation(MTransform.translate(20, 20)).
     *                  createConcatenation(MTransform.scale(5, 2));
     * 
     * In the above sequence first you: scale -> translate -> scale
     * 
     * @param transform
     * @return 
     */       
    @Override
    public MTransform createConcatenation(MTransformGeneric transform)
    {                
        return new MTransform(
                transform.getMatrix().createConcatenation(getMatrix()), 
                getInverseMatrix().createConcatenation(transform.getInverseMatrix()));        
    }

    @Override
    public MMatrix4 getMatrix() {
        return m;
    }

    @Override
    public MMatrix4 getInverseMatrix() {
        return mInv;
    }

    @Override
    public MTransformGeneric inverseTransform() {
        return new MTransform(mInv, m);
    }
}
