package si.um.feri.leaf.pollenGame;

import com.badlogic.gdx.math.Rectangle;

public class Cloud {
    Rectangle bounds;
    String size; // "small", "medium", "big"
    float respawnTimer;

    public Cloud(Rectangle bounds, String size) {
        this.bounds = bounds;
        this.size = size;
        this.respawnTimer = 0;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setRespawnTimer(float respawnTimer) {
        this.respawnTimer = respawnTimer;
    }

    public float getRespawnTimer() {
        return respawnTimer;
    }

    public void decreaseRespawnTimer(float delta) {
        respawnTimer -= delta;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
