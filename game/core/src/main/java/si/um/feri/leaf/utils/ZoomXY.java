package si.um.feri.leaf.utils;

public class ZoomXY {
    public int zoom;
    public int x;
    public int y;

    public ZoomXY(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return zoom + "/" + x + "/" + y;
    }
}
