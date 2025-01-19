package si.um.feri.leaf.pollenGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class Rain {
    private Texture rainTexture;
    private Animation<TextureRegion> rainAnimation;
    private float stateTime;
    private float screenWidth;
    private float screenHeight;
    private List<Raindrop> raindrops;
    private int dropCount; // Number of raindrops to spawn

    public Rain(String texturePath, int frameWidth, int frameHeight, int dropCount, float screenWidth, float screenHeight) {
        this.rainTexture = new Texture(texturePath);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.dropCount = dropCount;

        // Split the texture into frames
        TextureRegion[][] frames = TextureRegion.split(rainTexture, frameWidth, frameHeight);

        // Flatten the 2D array into a 1D array
        TextureRegion[] rainFrames = frames[0]; // Use the first row as animation frames
        this.rainAnimation = new Animation<>(0.1f, rainFrames);
        this.rainAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Initialize raindrops
        this.raindrops = new ArrayList<>();
        for (int i = 0; i < dropCount; i++) {
            float x = MathUtils.random(0, screenWidth - frameWidth); // Ensure raindrop spawns fully on-screen
            float y = MathUtils.random(0, screenHeight);            // Random y position
            float speed = MathUtils.random(30f, 100f);              // Random speed
            raindrops.add(new Raindrop(x, y, speed));
        }

        this.stateTime = 0f;
    }

    public void update(float delta) {
        stateTime += delta;

        for (Raindrop raindrop : raindrops) {
            // Move the raindrop at a 240-degree angle
            raindrop.x -= raindrop.speed * 0.5f * delta; // Slower x-direction
            raindrop.y -= raindrop.speed * delta;       // Faster y-direction

            // Reset raindrop to a random position above and to the right if it goes off-screen
            if (raindrop.y < -rainAnimation.getKeyFrame(0).getRegionHeight() || raindrop.x < -rainAnimation.getKeyFrame(0).getRegionWidth()) {
                raindrop.y = MathUtils.random(screenHeight, screenHeight + 50); // Slightly above the top
                raindrop.x = MathUtils.random(0, screenWidth - rainAnimation.getKeyFrame(0).getRegionWidth()); // Ensure full width coverage
            }
        }
    }

    public void render(Batch batch) {
        TextureRegion currentFrame = rainAnimation.getKeyFrame(stateTime, true);

        batch.begin();
        for (Raindrop raindrop : raindrops) {
            batch.draw(
                currentFrame,
                raindrop.x,
                raindrop.y,
                currentFrame.getRegionWidth(),
                currentFrame.getRegionHeight()
            );
        }
        batch.end();
    }

    public void dispose() {
        rainTexture.dispose();
    }

    // Inner class for individual raindrops
    private static class Raindrop {
        float x, y, speed;

        public Raindrop(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
    }
}
