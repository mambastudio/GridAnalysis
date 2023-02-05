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
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

/**
 *
 * @author user
 */
public class MMatrix4 {
    private final Transform m;
    
    public MMatrix4()
    {
        m = new Affine();
    }
    
    public MMatrix4(Transform m)
    {
        this.m = m;
    }
    
    public MMatrix4 inverse()
    {
        try {
            return new MMatrix4(m.createInverse());
        } catch (NonInvertibleTransformException ex) {
            Logger.getLogger(MMatrix4.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public Transform getTransform()
    {
        return m;
    }
    
    public MMatrix4 createConcatenation(MMatrix4 matrix)
    {
        return new MMatrix4(m.createConcatenation(matrix.getTransform()));
    }
    
    public MMatrix4 mul(MMatrix4 matrix)
    {
        return null;
    }
}
