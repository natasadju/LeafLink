package si.um.feri.leaf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Character {
    private Texture texture;
    private Vector2 position;
    private Vector2 velocity;
    private boolean isJumping;

    private final float gravity = -500f;
    private final float jumpForce = 300f;
    private final float groundLevel = 60f;

    public Character() {
        texture = new Texture("images/boy.png");
        position = new Vector2(100, groundLevel);
        velocity = new Vector2(0, 0);
        isJumping = false;
    }

    public void update(float delta) {
        if (isJumping) {
            velocity.y += gravity * delta;
            position.y += velocity.y * delta;

            if (position.y <= groundLevel) {
                position.y = groundLevel;
                isJumping = false;
                velocity.y = 0;
            }
        }
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            velocity.y = jumpForce;
        }
    }

    public void squat() {
        position.y -= 10; // Example squat behavior
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, 160, 160);
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, 160, 160);
    }

    public void dispose() {
        texture.dispose();
    }
}

