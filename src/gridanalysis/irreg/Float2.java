/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gridanalysis.irreg;

/**
 *
 * @author user
 */
public class Float2 {
    public float x, y;

    public Float2() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Float2(float x) {
        this.x = x;
        this.y = x;
    }

    public Float2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float get(int axis) {
        return (axis == 0) ? x : y;
    }

    public void set(int axis, float value) {
        if (axis == 0) {
            x = value;
        } else {
            y = value;
        }
    }

    public static Float2 mul(float a, Float2 b) {
        return new Float2(a * b.x, a * b.y);
    }

    public static Float2 mul(Float2 a, float b) {
        return new Float2(a.x * b, a.y * b);
    }

    public static Float2 sub(Float2 a, Float2 b) {
        return new Float2(a.x - b.x, a.y - b.y);
    }

    public static Float2 add(Float2 a, Float2 b) {
        return new Float2(a.x + b.x, a.y + b.y);
    }

    public static Float2 mul(Float2 a, Float2 b) {
        return new Float2(a.x * b.x, a.y * b.y);
    }

    public static Float2 min(Float2 a, Float2 b) {
        return new Float2(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    public static Float2 max(Float2 a, Float2 b) {
        return new Float2(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    public static float dot(Float2 a, Float2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static float length(Float2 a) {
        return (float) Math.sqrt(dot(a, a));
    }

    public static Float2 normalize(Float2 a) {
        float invLength = 1.0f / length(a);
        return mul(a, invLength);
    }
}
