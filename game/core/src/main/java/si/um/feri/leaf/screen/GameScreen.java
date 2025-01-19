package si.um.feri.leaf.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
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

    Marker marker;

    private ArrayList<Vector2> garbagePositions;
    private ArrayList<Integer> garbageTypes;
    private ArrayList<Float> garbageTimers;
    private ArrayList<TextureRegion> broomImages;
    private int remainingBrooms = 5;
    private float videoTime = 0;
    private TextureAtlas textureAtlas;
    private TextureRegion birdRegion;
    private ArrayList<TextureRegion> garbageRegions;

    private Vector2 birdPosition;
    private float birdTimer;
    private TextButton backButton;
    private TextButton scoreButton;

    private int health = 100;
    private int score = 0;
    private Random random;
    private Stage stage;
    private Skin skin;
    private boolean gameOver = false;
    private SpriteBatch overlayBatch;
    private boolean isDialogVisible = false;
    private float groundLevel = 60f;
    private ArrayList<Vector2> magnetPowerUps;
    private TextureRegion magnetRegion;
    private float magnetSpawnInterval;

    private Sound garbageSound;
    private Sound gameOverSound;
    private Sound birdCollisionSound;

    public GameScreen(LeafLink game, Marker marker) throws FileNotFoundException {
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.character = new Character();
        this.marker= marker;
        this.assetManager = game.getAssetManager();
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
        birdCollisionSound= assetManager.get(AssetDescriptors.SOUND_BRID_COLLISION);

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

        magnetPowerUps = new ArrayList<>();
        magnetRegion = textureAtlas.findRegion(RegionNames.MAGNET);

        garbagePositions = new ArrayList<>();
        garbageTypes = new ArrayList<>();
        garbageTimers = new ArrayList<>();
        random = new Random();

        birdPosition = new Vector2(-100, -100);

        stage = new Stage(new ScreenViewport());
        skin= assetManager.get("ui/flat-earth-ui.json", Skin.class);
        Gdx.input.setInputProcessor(stage);
        createBackButton(skin);

        magnetSpawnInterval= 5 + random.nextFloat() * 5;

        spawnGarbage();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = assetManager.get(AssetDescriptors.UI_SKIN);
        createBackButton(skin);

        scoreButton = new TextButton("Score: "+ score, skin);
        scoreButton.setDisabled(true);

        scoreButton.setPosition(Gdx.graphics.getWidth() / 2 - scoreButton.getWidth() / 2 + 70, Gdx.graphics.getHeight() - 70);
        scoreButton.setSize(150, 50);

        stage.addActor(scoreButton);

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


            spriteBatch.begin();

            if (videoPlayer != null) {
                videoPlayer.update();
                Texture frame = videoPlayer.getTexture();
                if (frame != null) {
                    // Track video time
                    videoTime += delta;

                    if (videoTime >= 30) {
                        videoTime = 0;
                        videoPlayer.play();
                    }

                    spriteBatch.draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            }

            magnetSpawnInterval  -= delta;
            if (magnetSpawnInterval  <= 0 && !character.isMagnetActive()) {
                spawnMagnetPowerUp();
                magnetSpawnInterval  = 5 + random.nextFloat() * 5;
            }


            character.render(spriteBatch);

            for (int i = 0; i < garbagePositions.size(); i++) {
                TextureRegion garbageRegion = garbageRegions.get(garbageTypes.get(i));
                spriteBatch.draw(garbageRegion, garbagePositions.get(i).x, garbagePositions.get(i).y, 50, 50);
            }

            if (birdTimer <= 7) {
                spriteBatch.draw(birdRegion, birdPosition.x, birdPosition.y, 64, 64);
            }

            // Draw the broom images
            for (int i = 0; i < remainingBrooms; i++) {
                spriteBatch.draw(broomImages.get(i), 10 + i * 70, Gdx.graphics.getHeight() - 70, 64, 64);
            }

            for (Vector2 pos : magnetPowerUps) {
                spriteBatch.draw(magnetRegion, pos.x, pos.y, 50, 50);
            }

            backButton.draw(spriteBatch, 1f);

            spriteBatch.end();

            checkCollisions();
        } else {
            spriteBatch.begin();
            if (videoPlayer != null) {
                videoPlayer.update();
                Texture frame = videoPlayer.getTexture();
                if (frame != null) {
                    spriteBatch.draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            }
            spriteBatch.end();

            stage.act(delta);
            stage.draw();
        }

        stage.act(delta);
        stage.draw();
    }



    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            character.jump();
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
                garbageTimers.set(i, random.nextFloat() * 5 + 2);
            }
        }

        if (character.isMagnetActive()) {
            for (int i = garbagePositions.size() - 1; i >= 0; i--) {
                Vector2 garbagePos = garbagePositions.get(i);
                Vector2 direction = new Vector2(character.getBounds().x, character.getBounds().y).sub(garbagePos).nor();
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
            birdPosition.set(Gdx.graphics.getWidth(), random.nextFloat() * (Gdx.graphics.getHeight() - 100f - 70f) + 70f);
            birdTimer = 0;
        } else {
            birdPosition.x -= 200 * delta;

            if (birdPosition.x < -64) {
                birdPosition.set(-100, -100);
            }
        }
    }

    private void updateMagnets(float delta) {
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
                showGameOverDialog();
            }
        }
    }

    public void showGameOverDialog() {
        backButton.setVisible(false);
        scoreButton.setVisible(false);

        if (overlayBatch == null) {
            overlayBatch = new SpriteBatch();
        }

        ScreenUtils.clear(172 / 255f, 225 / 255f, 175 / 255f, 0f);

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

        isDialogVisible = true;
    }


    private void restartGame() throws FileNotFoundException {
        backButton.setVisible(true);
        scoreButton.setVisible(true);
        game.setScreen(new GameScreen(game,marker));
    }

    private void spawnGarbage() {
        for (int i = 0; i < 2; i++) {
            spawnGarbageItem();
        }
    }

    private void spawnGarbageItem() {
        float yPosition = random.nextFloat() * (Gdx.graphics.getHeight() - 150f - groundLevel) + groundLevel;

        garbagePositions.add(new Vector2(Gdx.graphics.getWidth() + random.nextInt(300), yPosition));
        garbageTypes.add(random.nextInt(garbageRegions.size()));
        garbageTimers.add(random.nextFloat() * 3 + 1);
    }

    private void spawnMagnetPowerUp() {
        if (!character.isMagnetActive()) {
            Vector2 magnetPos = new Vector2(
                Gdx.graphics.getWidth() + random.nextFloat() * 200,
                random.nextFloat() * (Gdx.graphics.getHeight() - groundLevel - 50) + groundLevel
            );
            magnetPowerUps.add(magnetPos);
        }
    }


    private void createBackButton(Skin skin) {
        backButton = new TextButton("Back to Map", skin);

        backButton.setSize(200, 50);
        backButton.setPosition(Gdx.graphics.getWidth() - 220, Gdx.graphics.getHeight() - 70);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MapScreen(game));
            }
        });

        stage.addActor(backButton);
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
