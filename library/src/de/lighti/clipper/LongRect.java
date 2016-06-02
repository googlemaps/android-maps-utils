package de.lighti.clipper;

public class LongRect {
    public long left;
    public long top;
    public long right;
    public long bottom;

    public LongRect() {

    }

    public LongRect( long l, long t, long r, long b ) {
        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    public LongRect( LongRect ir ) {
        left = ir.left;
        top = ir.top;
        right = ir.right;
        bottom = ir.bottom;
    }
}
