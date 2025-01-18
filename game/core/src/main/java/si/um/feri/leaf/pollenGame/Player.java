package si.um.feri.leaf.pollenGame;


import static si.um.feri.leaf.pollenGame.config.GameConfig.COLLISION_DAMAGE_DELAY;
import static si.um.feri.leaf.pollenGame.config.GameConfig.DAMAGE_COOLDOWN;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
public class Player {
    private Texture playerTilemap;
    private TextureRegion[][] frames;
    private Animation<TextureRegion> currentAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Sprite sprite;

    private float stateTime;
    private float speed = 100f;

    private float collisionTimer = 0;
    private Cloud currentCollisionCloud = null;

    private float damageCooldownTimer = 0;

    private int health = 100;
    private int score = 0;
    private Sound coughSound;
    private float soundTimer = 0;
    private float soundDuration = 0;

    // Control keys
    private int keyUp;
    private int keyDown;
    private int keyLeft;
    private int keyRight;

    private int keyAction;
    public Player(TextureAtlas atlas, String characterName, int tileWidth, int tileHeight, Sound coughSound) {
        System.out.println("Character name: " + characterName);
        TextureAtlas.AtlasRegion region = atlas.findRegion("characterSprites/" + characterName);

        if (region == null) {
            throw new IllegalArgumentException("Region not found: characterSprites/" + characterName);
        }

        TextureRegion[][] splitFrames = region.split(tileWidth, tileHeight);

        if (splitFrames.length < 4 || splitFrames[0].length < 4) {
            throw new IllegalArgumentException("Invalid sprite sheet: Not enough frames for animations");
        }

        this.coughSound = coughSound;
        this.soundDuration = 1.0f;

        walkDownAnimation = new Animation<>(0.2f, splitFrames[0][0], splitFrames[1][0], splitFrames[2][0], splitFrames[3][0]);
        walkUpAnimation = new Animation<>(0.2f, splitFrames[0][1], splitFrames[1][1], splitFrames[2][1], splitFrames[3][1]);
        walkRightAnimation = new Animation<>(0.2f, splitFrames[0][3], splitFrames[1][3], splitFrames[2][3], splitFrames[3][3]);
        walkLeftAnimation = new Animation<>(0.2f, splitFrames[0][2], splitFrames[1][2], splitFrames[2][2], splitFrames[3][2]);

        currentAnimation = walkDownAnimation;
        sprite = new Sprite(splitFrames[0][0]);
        sprite.setOriginCenter();

    }

    public void setControls(int keyUp, int keyDown, int keyLeft, int keyRight, int keyAction) {
        this.keyUp = keyUp;
        this.keyDown = keyDown;
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyAction = keyAction;
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }

    public float getX() {
        return sprite.getX();
    }

    public float getY() {
        return sprite.getY();
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public int getActionKey() {
        return keyAction;
    }


    public void update(float delta, MapObjects unwalkableObjects, ArrayList<Cloud> clouds) {
        float oldX = sprite.getX();
        float oldY = sprite.getY();

        boolean moving = false;

        if (Gdx.input.isKeyPressed(keyUp)) {
            moving = moveUp(delta, oldY, unwalkableObjects);
        } else if (Gdx.input.isKeyPressed(keyDown)) {
            moving = moveDown(delta, oldY, unwalkableObjects);
        }
        if (Gdx.input.isKeyPressed(keyLeft)) {
            moving = moveLeft(delta, oldX, unwalkableObjects);
        } else if (Gdx.input.isKeyPressed(keyRight)) {
            moving = moveRight(delta, oldX, unwalkableObjects);
        }

        clampPosition();

        if (moving) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }
        sprite.setRegion(currentAnimation.getKeyFrame(stateTime, true));

        if (soundTimer > 0) {
            soundTimer -= delta;
        }

        handleCloudCollision(delta, clouds);
    }

    public boolean moveUp(float delta, float oldY, MapObjects unwalkableObjects) {
        sprite.translateY(speed * delta);
        if (isColliding(unwalkableObjects)) {
            sprite.setY(oldY);
            return false;
        }
        currentAnimation = walkUpAnimation;
        return true;
    }

    public boolean moveDown(float delta, float oldY, MapObjects unwalkableObjects) {
        sprite.translateY(-speed * delta);
        if (isColliding(unwalkableObjects)) {
            sprite.setY(oldY);
            return false;
        }
        currentAnimation = walkDownAnimation;
        return true;
    }

    public boolean moveLeft(float delta, float oldX, MapObjects unwalkableObjects) {
        sprite.translateX(-speed * delta);
        if (isColliding(unwalkableObjects)) {
            sprite.setX(oldX);
            return false;
        }
        currentAnimation = walkLeftAnimation;
        return true;
    }

    public boolean moveRight(float delta, float oldX, MapObjects unwalkableObjects) {
        sprite.translateX(speed * delta);
        if (isColliding(unwalkableObjects)) {
            sprite.setX(oldX);
            return false;
        }
        currentAnimation = walkRightAnimation;
        return true;
    }

    private void handleCloudCollision(float delta, ArrayList<Cloud> clouds) {
        if (damageCooldownTimer > 0) {
            damageCooldownTimer -= delta;
        }

        boolean isCollidingWithCloud = false;

        for (Cloud cloud : clouds) {
            if (cloud.getSize() != null && sprite.getBoundingRectangle().overlaps(cloud.getBounds())) {
                if (currentCollisionCloud == cloud) {
                    collisionTimer += delta;
                } else {
                    currentCollisionCloud = cloud;
                    collisionTimer = 0;
                }

                isCollidingWithCloud = true;

                if (damageCooldownTimer <= 0 && collisionTimer >= COLLISION_DAMAGE_DELAY) {
                    takeDamage();
                    damageCooldownTimer = DAMAGE_COOLDOWN;
                }
                break;
            }
        }

        if (!isCollidingWithCloud) {
            collisionTimer = 0;
            currentCollisionCloud = null;
        }
    }

    private void takeDamage() {
        health -= 5;
        health = Math.max(0, health);


        if (coughSound != null && soundTimer <= 0) {
            coughSound.play();
            soundTimer = soundDuration;
        }

        System.out.println("Player took damage! Health: " + health);
    }

    private void clampPosition() {
        float minX = 0;
        float minY = 0;
        float maxX = Gdx.graphics.getWidth() - sprite.getWidth();
        float maxY = Gdx.graphics.getHeight() - sprite.getHeight();

        float clampedX = MathUtils.clamp(sprite.getX(), minX, maxX);
        float clampedY = MathUtils.clamp(sprite.getY(), minY, maxY);

        sprite.setPosition(clampedX, clampedY);
    }

    private boolean isColliding(MapObjects collisionObjects) {
        if (collisionObjects == null) return false;

        Rectangle playerRect = sprite.getBoundingRectangle();

        for (MapObject object : collisionObjects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                if (playerRect.overlaps(rect)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void reset() {
        sprite.setPosition(10, 170);
        health = 100;
        score = 0;
        stateTime = 0;
        currentAnimation = walkDownAnimation;

        System.out.println("Player respawned! Health: " + health + ", Score: " + score);
    }

    //        if (Gdx.input.isKeyJustPressed(player.getActionKey())) {
    //get action key


    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    public void render(Batch batch) {
        sprite.draw(batch);
    }

    public void dispose() {
        playerTilemap.dispose();
    }
}
