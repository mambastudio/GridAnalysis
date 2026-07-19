package gridanalysis.coordinates;

/** Two unsigned 16-bit coordinates stored in Java {@code short} values. */
public record UShort2(short x, short y) {
    public static final int MAX_VALUE = 0xffff;

    public static UShort2 from(Vec2i value) {
        return new UShort2(pack(value.x), pack(value.y));
    }

    public int unsignedX() { return Short.toUnsignedInt(x); }
    public int unsignedY() { return Short.toUnsignedInt(y); }
    public Vec2i toVec2i() { return new Vec2i(unsignedX(), unsignedY()); }

    private static short pack(int value) {
        if (value < 0 || value > MAX_VALUE) {
            throw new IllegalArgumentException("unsigned short coordinate out of range: " + value);
        }
        return (short) value;
    }
}
