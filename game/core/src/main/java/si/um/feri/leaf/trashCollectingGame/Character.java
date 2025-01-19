package si.um.feri.leaf.trashCollectingGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Character {
    private Texture texture;
    private Texture powerUpTexture;
    private Vector2 position;
    private Vector2 velocity;
    private boolean isJumping;
    private int jumpCount;

    private final float gravity = -500f;
    private final float jumpForce = 300f;
    private final float groundLevel = 60f;

    private boolean magnetActive = false;
    private float magnetTimer = 0;

    public Character() {
        texture = new Texture("images/boy.png");
        powerUpTexture = new Texture("images/boy_with_magnet.png"); // Add a new texture for power-up
        position = new Vector2(100, groundLevel);
        velocity = new Vector2(0, 0);
        isJumping = false;
        jumpCount = 0;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void update(float delta) {
        if (magnetActive) {
            magnetTimer -= delta;
            if (magnetTimer <= 0) {
                magnetActive = false;
            }
        }
        // Existing gravity and jump logic
        if (isJumping) {
            velocity.y += gravity * delta;
            position.y += velocity.y * delta;

            if (position.y <= groundLevel) {
                position.y = groundLevel;
                isJumping = false;
                velocity.y = 0;
                jumpCount = 0;
            }
        }
    }

    public boolean isMagnetActive() {
        return magnetActive;
    }

    public void activateMagnet(float duration) {
        this.magnetActive = true;
        this.magnetTimer = duration;
    }

    public void jump() {
        if (jumpCount < 3) { // Allow up to two jumps
            isJumping = true;
            velocity.y = jumpForce;
            jumpCount++;
        }
    }

    public void render(SpriteBatch batch) {
        if (magnetActive) {
            batch.draw(powerUpTexture, position.x, position.y, 100, 200);
        } else {
            batch.draw(texture, position.x, position.y, 100, 200);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, 100, 200);
    }

    public void dispose() {
        texture.dispose();
    }
}
