package si.um.feri.leaf.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import assets.AssetDescriptors;
import assets.RegionNames;
import si.um.feri.leaf.LeafLink;
import si.um.feri.leaf.Character;
import si.um.feri.leaf.utils.Marker;

public class GameScreen extends ScreenAdapter {
    private final LeafLink game;
    private final AssetManager assetManager;
    private VideoPlayer videoPlayer;
    private SpriteBatch spriteBatch;
    private Character character;

    private ArrayList<Vector2> garbagePositions;
    private ArrayList<Integer> garbageTypes;
    private ArrayList<Float> garbageTimers;

    private TextureAtlas textureAtlas;
    private TextureRegion birdRegion;
    private ArrayList<TextureRegion> garbageRegions;

    private Vector2 birdPosition;
    private float birdTimer;

    private int health = 100;
    private int score = 0;
    private Random random;

    public GameScreen(LeafLink game, Marker marker) throws FileNotFoundException {
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.character = new Character();
        this.assetManager = new AssetManager();

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
            videoPlayer.play(Gdx.files.internal("videos/background.webm"));
        } catch (Exception e) {
            throw new FileNotFoundException("Video file not found: videos/background.webm");
        }

        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.finishLoading();

        textureAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        birdRegion = textureAtlas.findRegion(RegionNames.BIRD);

        garbageRegions = new ArrayList<>();
        garbageRegions.add(textureAtlas.findRegion(RegionNames.GARBAGE1));
        garbageRegions.add(textureAtlas.findRegion(RegionNames.GARBAGE2));
        garbageRegions.add(textureAtlas.findRegion(RegionNames.GARBAGE3));

        garbagePositions = new ArrayList<>();
        garbageTypes = new ArrayList<>();
        garbageTimers = new ArrayList<>();
        random = new Random();

        birdPosition = new Vector2(-100, -100);

        spawnGarbage();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();
        character.update(delta);

        updateGarbage(delta);
        updateBird(delta);

        spriteBatch.begin();

        if (videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();
            if (frame != null) {
                spriteBatch.draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }

        character.render(spriteBatch);

        for (int i = 0; i < garbagePositions.size(); i++) {
            TextureRegion garbageRegion = garbageRegions.get(garbageTypes.get(i));
            spriteBatch.draw(garbageRegion, garbagePositions.get(i).x, garbagePositions.get(i).y, 50, 50);
        }

        if (birdTimer <= 7) {
            spriteBatch.draw(birdRegion, birdPosition.x, birdPosition.y, 64, 64);
        }

        game.font.draw(spriteBatch, "Health: " + health, 10, Gdx.graphics.getHeight() - 10);
        game.font.draw(spriteBatch, "Score: " + score, 10, Gdx.graphics.getHeight() - 30);

        spriteBatch.end();

        checkCollisions();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            character.jump();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            character.squat();
        }
    }

    private void updateGarbage(float delta) {
        for (int i = garbagePositions.size() - 1; i >= 0; i--) {
            garbagePositions.get(i).x -= 200 * delta;

            if (garbagePositions.get(i).x < -32) {
                garbagePositions.remove(i);
                garbageTypes.remove(i);
                garbageTimers.remove(i);
            }
        }

        for (int i = garbageTimers.size() - 1; i >= 0; i--) {
            garbageTimers.set(i, garbageTimers.get(i) - delta);
            if (garbageTimers.get(i) <= 0) {
                spawnGarbageItem();
                garbageTimers.set(i, random.nextFloat() * 3 + 1);
            }
        }

        if (garbagePositions.size() < 5) {
            spawnGarbage();
        }
    }

    private void updateBird(float delta) {
        birdTimer += delta;

        if (birdTimer >= 7) {
            birdPosition.set(Gdx.graphics.getWidth(), random.nextInt(Gdx.graphics.getHeight() - 100));
            birdTimer = 0;
        } else {
            birdPosition.x -= 200 * delta;

            if (birdPosition.x < -64) {
                birdPosition.set(-100, -100);
            }
        }
    }

    private void checkCollisions() {
        for (int i = garbagePositions.size() - 1; i >= 0; i--) {
            Vector2 garbagePos = garbagePositions.get(i);
            Rectangle garbageBounds = new Rectangle(garbagePos.x, garbagePos.y, 50, 50);

            if (character.getBounds().overlaps(garbageBounds)) {
                garbagePositions.remove(i);
                garbageTypes.remove(i);
                garbageTimers.remove(i);
                score += 10;
            }
        }

        Rectangle birdBounds = new Rectangle(birdPosition.x, birdPosition.y, 64, 64);
        if (character.getBounds().overlaps(birdBounds)) {
            health -= 10;
            birdPosition.set(-100, -100);
        }

        if (health <= 0) {
            System.out.println("Game Over!");
            Gdx.app.exit();
        }
    }

    private void spawnGarbage() {
        for (int i = 0; i < 3; i++) {
            spawnGarbageItem();
        }
    }

    private void spawnGarbageItem() {
        garbagePositions.add(new Vector2(Gdx.graphics.getWidth() + random.nextInt(300), random.nextInt(Gdx.graphics.getHeight() - 100)));
        garbageTypes.add(random.nextInt(garbageRegions.size()));
        garbageTimers.add(random.nextFloat() * 3 + 1);
    }

    @Override
    public void dispose() {
        if (videoPlayer != null) {
            videoPlayer.dispose();
        }
        spriteBatch.dispose();
        assetManager.dispose();
        character.dispose();
    }
}
