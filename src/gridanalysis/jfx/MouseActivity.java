/*
 * The MIT License
 *
 * Copyright 2022 jmburu.
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
package gridanalysis.jfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;

/**
 *
 * @author jmburu
 */
public class MouseActivity {
    private long lastClickTime = 0;
    
    private BooleanProperty isTouchPressed = null;
    
    private Point2D pressed = Point2D.ZERO;
    private Point2D delta = Point2D.ZERO;
    
    public MouseActivity()
    {
        isTouchPressed = new SimpleBooleanProperty();
    }
    
    public boolean isDoubleClick(long intervalRangeMsec)
    {
        long currentClickTime = System.currentTimeMillis();
        long diff = 0;       
        if(lastClickTime!=0 && currentClickTime!=0)
            diff = currentClickTime - lastClickTime;        
        lastClickTime = currentClickTime;
        
        return diff < intervalRangeMsec && intervalRangeMsec > 0;
    }
    
    public void setTouchPressed(boolean touchPressed)
    {
        isTouchPressed.set(touchPressed);
    }
    
    public boolean isTouchPressed()
    {
        return isTouchPressed.get();
    }
    
    public void setPoint(Point2D p)
    {
        delta = p.subtract(pressed);
        pressed = p;
    }

    public Point2D getDelta()
    {
        return delta;
    }
    
    public float getXFloatPoint()
    {
        return (float) pressed.getX();
    }
    
    public float getYFloatPoint()
    {
        return (float) pressed.getY();
    }
    
    @Override
    public final String toString() {
        return String.format("(%.2f, %.2f)", getXFloatPoint(), getYFloatPoint());
    }
}
