package si.um.feri.leaf.pollenGame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import assets.AssetDescriptors;
import si.um.feri.leaf.LeafLink;

public class CharacterSelectionScreen extends ScreenAdapter {

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;

    private final LeafLink game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final AssetManager assetManager;

    private final Array<String> characterNames;
    private final Array<TextureRegion> previewRegions;

    private TextureAtlas tiledAtlas;
    private int selectedIndexP1 = 0;
    private int selectedIndexP2 = 0;

    private final boolean isTwoPlayerMode;
    private boolean firstPlayerConfirmed = false;

    private String chosenCharacterP1;
    private String chosenCharacterP2;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Sound chooseSound;
    private Music backgroundMusic;

    public CharacterSelectionScreen(LeafLink game, boolean isTwoPlayerMode) {
        this.game = game;
        this.isTwoPlayerMode = isTwoPlayerMode;

        this.batch = new SpriteBatch();
        this.assetManager = game.getAssetManager();
        this.font = assetManager.get(AssetDescriptors.TILED_FONT);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        backgroundMusic = assetManager.get(AssetDescriptors.CHOOSE_SCREEN);
        chooseSound = assetManager.get(AssetDescriptors.CHOOSE_CHARACTER);

        characterNames = new Array<>();
        characterNames.add("character1");
        characterNames.add("character2");
        characterNames.add("character3");
        characterNames.add("character4");

        tiledAtlas = assetManager.get(AssetDescriptors.TILED_ATLAS);
        previewRegions = new Array<>();
        for (String name : characterNames) {
            TextureRegion region = tiledAtlas.findRegion("characterIcons/" + name);
            if (region == null) {
                Gdx.app.error("CharacterSelectionScreen",
                    "Region not found for: characterIcons/" + name);
            }
            previewRegions.add(region);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }


        if (!firstPlayerConfirmed) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                selectedIndexP1 = (selectedIndexP1 - 1 + previewRegions.size) % previewRegions.size;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                selectedIndexP1 = (selectedIndexP1 + 1) % previewRegions.size;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                chooseSound.play();
                chosenCharacterP1 = characterNames.get(selectedIndexP1);
                firstPlayerConfirmed = true;

                if (!isTwoPlayerMode) {
                    backgroundMusic.stop();
                    game.setScreen(new PollenGameScreen(
                        game,
                        chosenCharacterP1,
                        /* no second character */ null,
                        false
                    ));
                    return;
                }
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedIndexP2 = (selectedIndexP2 - 1 + previewRegions.size) % previewRegions.size;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedIndexP2 = (selectedIndexP2 + 1) % previewRegions.size;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                chooseSound.play();
                chosenCharacterP2 = characterNames.get(selectedIndexP2);
                backgroundMusic.stop();
                game.setScreen(new PollenGameScreen(
                    game,
                    chosenCharacterP1,
                    chosenCharacterP2,
                    true
                ));
                return;
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.setColor(Color.GOLD);
        font.getData().setScale(1f);
        String title = "Character Selection";
        float titleWidth = font.draw(batch, title, 0, 0).width;
        float titleX = (VIRTUAL_WIDTH - titleWidth) / 2f;
        float titleY = VIRTUAL_HEIGHT - 50;
        font.draw(batch, title, titleX, titleY);

        font.getData().setScale(0.3f);

        float iconWidth = 128;
        float iconHeight = 128;
        float gap = 30;
        float totalWidth = (iconWidth + gap) * previewRegions.size - gap;
        float startX = (VIRTUAL_WIDTH - totalWidth) / 2f;
        float yPosition = (VIRTUAL_HEIGHT / 2f - 10f) - (iconHeight / 2f);

        float instructionsY = yPosition - 50;

        for (int i = 0; i < previewRegions.size; i++) {
            float xPosition = startX + i * (iconWidth + gap);

            TextureRegion region = previewRegions.get(i);

            if (!firstPlayerConfirmed && i == selectedIndexP1) {
                batch.setColor(Color.YELLOW);
            } else if (firstPlayerConfirmed && i == selectedIndexP2) {
                batch.setColor(Color.GREEN);
            } else {
                batch.setColor(Color.WHITE);
            }

            batch.draw(region, xPosition, yPosition, iconWidth, iconHeight);
        }

        font.setColor(Color.WHITE);
        if (!isTwoPlayerMode) {
            font.draw(batch,
                "Use A/D to highlight.",
                30,
                instructionsY);
            font.draw(batch,
                "Press ENTER to confirm.",
                30,
                instructionsY - 30);
        } else {
            if (!firstPlayerConfirmed) {
                font.draw(batch, "PLAYER 1: ", 30, instructionsY);
                font.draw(batch,
                    "Use A/D to choose, press ENTER to confirm.",
                    30,
                    instructionsY - 30);
            } else {
                font.draw(batch, "PLAYER 2: ", 30, instructionsY);
                font.draw(batch,
                    "Use LEFT/RIGHT to choose, press ENTER to confirm.",
                    30,
                    instructionsY - 30);
            }
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
