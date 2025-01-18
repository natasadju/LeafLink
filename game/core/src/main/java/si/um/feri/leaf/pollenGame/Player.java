package si.um.feri.leaf.pollenGame;


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

public class Player {
    private Texture playerTilemap;
    private TextureRegion[][] frames;
    private Animation<TextureRegion> currentAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Sprite sprite;

   /* private Sound pickupSound;
    private Sound heartBeatsSound;
*/
    private float stateTime;
    private float speed = 100f;
    private int health = 100;
    private int score = 0;


    public Player(TextureAtlas atlas, String characterName, int tileWidth, int tileHeight) {
        System.out.println("Character name: " + characterName);
        TextureAtlas.AtlasRegion region = atlas.findRegion("characterSprites/" + characterName);

        if (region == null) {
            throw new IllegalArgumentException("Region not found: characterSprites/" + characterName);
        }
        TextureRegion[][] splitFrames = region.split(tileWidth, tileHeight);

        if (splitFrames.length < 4 || splitFrames[0].length < 4) {
            throw new IllegalArgumentException("Invalid sprite sheet: Not enough frames for animations");
        }

        walkDownAnimation = new Animation<>(0.2f, splitFrames[0][0], splitFrames[1][0], splitFrames[2][0], splitFrames[3][0]);
        walkUpAnimation = new Animation<>(0.2f, splitFrames[0][1], splitFrames[1][1], splitFrames[2][1], splitFrames[3][1]);
        walkRightAnimation = new Animation<>(0.2f, splitFrames[0][3], splitFrames[1][3], splitFrames[2][3], splitFrames[3][3]);
        walkLeftAnimation = new Animation<>(0.2f, splitFrames[0][2], splitFrames[1][2], splitFrames[2][2], splitFrames[3][2]);

        currentAnimation = walkDownAnimation;
        sprite = new Sprite(splitFrames[0][0]);
        sprite.setOriginCenter();
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

    public void update(float delta, MapObjects unwalkableObjects) {
        float oldX = sprite.getX();
        float oldY = sprite.getY();

        boolean moving = false;

        // Movement logic with collision checks
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            sprite.translateY(speed * delta);
            if (isColliding(unwalkableObjects)) {
                sprite.setY(oldY); // Revert Y position if colliding
            } else {
                currentAnimation = walkUpAnimation;
                moving = true;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            sprite.translateY(-speed * delta);
            if (isColliding(unwalkableObjects)) {
                sprite.setY(oldY); // Revert Y position if colliding
            } else {
                currentAnimation = walkDownAnimation;
                moving = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            sprite.translateX(-speed * delta);
            if (isColliding(unwalkableObjects)) {
                sprite.setX(oldX); // Revert X position if colliding
            } else {
                currentAnimation = walkLeftAnimation;
                moving = true;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            sprite.translateX(speed * delta);
            if (isColliding(unwalkableObjects)) {
                sprite.setX(oldX);
            } else {
                currentAnimation = walkRightAnimation;
                moving = true;
            }
        }

        clampPosition();

        if (moving) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }

        sprite.setRegion(currentAnimation.getKeyFrame(stateTime, true));
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


    /*private void handleDamage(MapObjects damageObjects) {
        if (damageObjects == null) return;

        Rectangle playerRect = sprite.getBoundingRectangle();

        for (MapObject object : damageObjects) {
            if (object instanceof RectangleMapObject) {
                Rectangle damageRect = ((RectangleMapObject) object).getRectangle();


                if (playerRect.overlaps(damageRect)) {
                    health -= 1;
                    health = Math.max(0, health);
                    System.out.println("Player took damage! Health: " + health);

                  *//*  if (heartBeatsSound != null) {
                        heartBeatsSound.play();
                    }
*//*
                    break;
                }
            }
        }
    }*/

    public boolean isDead() {
        return health <= 0;
    }

    public void reset() {
        sprite.setPosition(10,170);
        health = 100;
        score = 0;
        stateTime = 0;
        currentAnimation = walkDownAnimation;


        System.out.println("Player respawned! Health: " + health + ", Score: " + score);
    }


    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }


    public void render(Batch batch) {
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }

    public void dispose() {
        playerTilemap.dispose();
    }
}
