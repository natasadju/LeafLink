package si.um.feri.leaf.pollenGame;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class PowerUp {
    private final Rectangle bounds;
    private final TextureRegion texture;
    private boolean active;
    private float respawnTimer;

    public PowerUp(float x, float y, float width, float height, TextureRegion texture) {
        this.bounds = new Rectangle(x, y, width, height);
        this.texture = texture;
        this.active = false;
        this.respawnTimer = 0;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public float getRespawnTimer() {
        return respawnTimer;
    }

    public void setRespawnTimer(float timer) {
        this.respawnTimer = timer;
    }

    public void decreaseRespawnTimer(float delta) {
        if (respawnTimer > 0) {
            respawnTimer -= delta;
        }
    }

    public void render(Batch batch) {
        if (active) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
