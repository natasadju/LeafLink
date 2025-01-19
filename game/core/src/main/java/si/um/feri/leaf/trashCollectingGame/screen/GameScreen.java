package si.um.feri.leaf.trashCollectingGame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import assets.AssetDescriptors;
import assets.RegionNames;
import si.um.feri.leaf.LeafLink;
import si.um.feri.leaf.screen.MapScreen;
import si.um.feri.leaf.trashCollectingGame.Character;
import si.um.feri.leaf.utils.Marker;

public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 900f;
    private static final float WORLD_HEIGHT = 600f;

    private final LeafLink game;
    private final Marker marker;
    private final AssetManager assetManager;


    private OrthographicCamera gameCamera;
    private Viewport gameViewport;


    private OrthographicCamera uiCamera;
    private Viewport uiViewport;

    private Stage stage;
    private Skin skin;

    private SpriteBatch spriteBatch;
    private VideoPlayer videoPlayer;

    private Character character;

    private ArrayList<Vector2> garbagePositions;
    private ArrayList<Integer> garbageTypes;
    private ArrayList<Float> garbageTimers;

    private Vector2 birdPosition;
    private float birdTimer;

    private ArrayList<Vector2> magnetPowerUps;
    private float magnetSpawnInterval;

    private Sound garbageSound;
    private Sound gameOverSound;
    private Sound birdCollisionSound;

    private TextureAtlas textureAtlas;
    private TextureRegion birdRegion;
    private ArrayList<TextureRegion> garbageRegions;
    private ArrayList<TextureRegion> broomImages;
    private TextureRegion magnetRegion;

    private int remainingBrooms = 5;
    private boolean gameOver = false;
    private float videoTime = 0;
    private int score = 0;
    private Random random;

    private TextButton backButton;
    private TextButton scoreButton;
    private boolean isDialogVisible = false;

    private final float groundLevel = 60f;

    public GameScreen(LeafLink game, Marker marker) throws FileNotFoundException {
        this.game = game;
        this.marker = marker;
        this.assetManager = game.getAssetManager();

        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, gameCamera);

        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, uiCamera);

        spriteBatch = new SpriteBatch();
        random = new Random();

        stage = new Stage(uiViewport);
        Gdx.input.setInputProcessor(stage);


        if (assetManager.isLoaded(AssetDescriptors.GAMEPLAY)) {
            textureAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        } else {
            throw new IllegalStateException("GAMEPLAY atlas is not loaded!");
        }


        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
            videoPlayer.play(Gdx.files.internal("videos/background.webm"));
        } catch (Exception e) {
            throw new FileNotFoundException("Video file not found: videos/background.webm");
        }

        garbageSound = assetManager.get(AssetDescriptors.SOUND_CLICK);
        gameOverSound = assetManager.get(AssetDescriptors.SOUND_GAMEOVER);
        birdCollisionSound = assetManager.get(AssetDescriptors.SOUND_BRID_COLLISION);

        birdRegion = textureAtlas.findRegion(RegionNames.BIRD);

        garbageRegions = new ArrayList<>();
        garbageRegions.add(textureAtlas.findRegion(RegionNames.GARBAGE1));
        garbageRegions.add(textureAtlas.findRegion(RegionNames.GARBAGE2));
        garbageRegions.add(textureAtlas.findRegion(RegionNames.GARBAGE3));

        broomImages = new ArrayList<>();
        TextureRegion broomImage = textureAtlas.findRegion(RegionNames.BROOM);
        for (int i = 0; i < 5; i++) {
            broomImages.add(broomImage);
        }

        magnetRegion = textureAtlas.findRegion(RegionNames.MAGNET);

        character = new Character();

        garbagePositions = new ArrayList<>();
        garbageTypes = new ArrayList<>();
        garbageTimers = new ArrayList<>();
        magnetPowerUps = new ArrayList<>();

        birdPosition = new Vector2(-100, -100);

        magnetSpawnInterval = 5 + random.nextFloat() * 5;

        spawnGarbage();

        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        createBackButton(skin);

        scoreButton = new TextButton("Score: " + score, skin);
        scoreButton.setDisabled(true);
        stage.addActor(scoreButton);

        backButton.setPosition(0, 0);
        scoreButton.setPosition(0, 0);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        gameViewport.update(width, height, true);

        uiViewport.update(width, height, true);

        backButton.setPosition(uiViewport.getWorldWidth() - 220, uiViewport.getWorldHeight() - 70);
        scoreButton.setPosition(
            uiViewport.getWorldWidth() / 2f - scoreButton.getWidth() / 2f,
            uiViewport.getWorldHeight() - 70
        );
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (!gameOver) {
            handleInput();
            character.update(delta);
            updateGarbage(delta);
            updateBird(delta);
            updateMagnets(delta);
            checkCollisions();
        }

        gameViewport.apply();
        spriteBatch.setProjectionMatrix(gameCamera.combined);

        spriteBatch.begin();
        if (videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();
            if (frame != null) {
                videoTime += delta;
                if (videoTime >= 30) {
                    videoTime = 0;
                    videoPlayer.play();
                }
                spriteBatch.draw(frame, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            }
        }

        character.render(spriteBatch);

        for (int i = 0; i < garbagePositions.size(); i++) {
            TextureRegion garbageRegion = garbageRegions.get(garbageTypes.get(i));
            Vector2 pos = garbagePositions.get(i);
            spriteBatch.draw(garbageRegion, pos.x, pos.y, 50, 50);
        }

        if (birdTimer <= 7) {
            spriteBatch.draw(birdRegion, birdPosition.x, birdPosition.y, 64, 64);
        }

        for (int i = 0; i < remainingBrooms; i++) {
            spriteBatch.draw(broomImages.get(i),
                10 + i * 70,
                WORLD_HEIGHT - 70,
                64,
                64);
        }

        for (Vector2 pos : magnetPowerUps) {
            spriteBatch.draw(magnetRegion, pos.x, pos.y, 50, 50);
        }

        spriteBatch.end();

        uiViewport.apply();
        stage.act(delta);
        stage.draw();

        if (gameOver && !isDialogVisible) {
            showGameOverDialog();
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            character.jump();
        }
    }

    private void updateGarbage(float delta) {
        for (int i = garbagePositions.size() - 1; i >= 0; i--) {
            Vector2 pos = garbagePositions.get(i);
            pos.x -= 200 * delta;

            if (pos.x < -32) {
                garbagePositions.remove(i);
                garbageTypes.remove(i);
                garbageTimers.remove(i);
            }
        }

        for (int i = garbageTimers.size() - 1; i >= 0; i--) {
            garbageTimers.set(i, garbageTimers.get(i) - delta);
            if (garbageTimers.get(i) <= 0) {
                spawnGarbageItem();
                garbageTimers.set(i, random.nextFloat() * 5 + 4);
            }
        }

        if (character.isMagnetActive()) {
            for (int i = garbagePositions.size() - 1; i >= 0; i--) {
                Vector2 garbagePos = garbagePositions.get(i);
                Vector2 direction = new Vector2(character.getBounds().x, character.getBounds().y)
                    .sub(garbagePos)
                    .nor();
                garbagePos.add(direction.scl(300 * delta));
            }
        }

        if (garbagePositions.size() < 3) {
            spawnGarbage();
        }
    }

    private void updateBird(float delta) {
        birdTimer += delta;
        if (birdTimer >= 7) {
            float randomY = random.nextFloat() * (WORLD_HEIGHT - 100f - 70f) + 70f;
            birdPosition.set(WORLD_WIDTH, randomY);
            birdTimer = 0;
        } else {
            birdPosition.x -= 200 * delta;
            if (birdPosition.x < -64) {
                birdPosition.set(-100, -100);
            }
        }
    }

    private void updateMagnets(float delta) {
        magnetSpawnInterval -= delta;
        if (magnetSpawnInterval <= 0 && !character.isMagnetActive()) {
            spawnMagnetPowerUp();
            magnetSpawnInterval = 5 + random.nextFloat() * 5;
        }

        for (int i = magnetPowerUps.size() - 1; i >= 0; i--) {
            Vector2 magnetPos = magnetPowerUps.get(i);
            magnetPos.x -= 200 * delta;
            if (magnetPos.x < -50) {
                magnetPowerUps.remove(i);
            }
        }
    }

    private void checkCollisions() {
        for (int i = garbagePositions.size() - 1; i >= 0; i--) {
            Vector2 garbagePos = garbagePositions.get(i);
            Rectangle garbageBounds = new Rectangle(garbagePos.x, garbagePos.y, 50, 50);
            if (character.getBounds().overlaps(garbageBounds)) {
                garbageSound.play();
                garbagePositions.remove(i);
                garbageTypes.remove(i);
                garbageTimers.remove(i);
                score += 10;
                scoreButton.setText("Score: " + score);
            }
        }

        for (int i = magnetPowerUps.size() - 1; i >= 0; i--) {
            Vector2 magnetPos = magnetPowerUps.get(i);
            Rectangle magnetBounds = new Rectangle(magnetPos.x, magnetPos.y, 50, 50);
            if (character.getBounds().overlaps(magnetBounds)) {
                magnetPowerUps.remove(i);
                character.activateMagnet(10f);
            }
        }

        Rectangle birdBounds = new Rectangle(birdPosition.x, birdPosition.y, 64, 64);
        if (character.getBounds().overlaps(birdBounds)) {
            if (remainingBrooms > 0) {
                birdCollisionSound.play();
                remainingBrooms--;
                birdPosition.set(-100, -100);
            }
            if (remainingBrooms == 0 && !gameOver) {
                gameOverSound.play();
                gameOver = true;
            }
        }
    }

    private void spawnGarbage() {
        for (int i = 0; i < 2; i++) {
            spawnGarbageItem();
        }
    }

    private void spawnGarbageItem() {
        float xPos = WORLD_WIDTH + random.nextInt(300);
        float yPos = random.nextFloat() * (WORLD_HEIGHT - 150f - groundLevel) + groundLevel;

        garbagePositions.add(new Vector2(xPos, yPos));
        garbageTypes.add(random.nextInt(garbageRegions.size()));
        garbageTimers.add(random.nextFloat() * 3 + 1);
    }

    private void spawnMagnetPowerUp() {
        if (!character.isMagnetActive()) {
            float xPos = WORLD_WIDTH + random.nextFloat() * 200;
            float yPos = random.nextFloat() * (WORLD_HEIGHT - groundLevel - 50) + groundLevel;
            magnetPowerUps.add(new Vector2(xPos, yPos));
        }
    }

    private void createBackButton(Skin skin) {
        backButton = new TextButton("Back to Map", skin);
        backButton.setSize(200, 50);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MapScreen(game));
            }
        });

        stage.addActor(backButton);
    }

    /**
     * Show a dialog when the game ends.
     */
    private void showGameOverDialog() {
        isDialogVisible = true;
        backButton.setVisible(false);
        scoreButton.setVisible(false);

        Dialog gameOverDialog = new Dialog("Game Over", skin) {
            @Override
            protected void result(Object object) {
                boolean playAgain = (Boolean) object;
                if (playAgain) {
                    try {
                        restartGame();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    game.setScreen(new MapScreen(game));
                }
                isDialogVisible = false;
            }
        };

        gameOverDialog.text("You lost! What would you like to do?");
        gameOverDialog.button("Play Again", true);
        gameOverDialog.button("Back to Main", false);

        gameOverDialog.show(stage);
    }

    private void restartGame() throws FileNotFoundException {
        game.setScreen(new GameScreen(game, marker));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (videoPlayer != null) {
            videoPlayer.dispose();
        }
        spriteBatch.dispose();
        if (character != null) character.dispose();
        stage.dispose();
    }
}
