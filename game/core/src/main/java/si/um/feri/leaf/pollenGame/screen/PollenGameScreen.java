package si.um.feri.leaf.pollenGame.screen;

import static si.um.feri.leaf.pollenGame.config.GameConfig.HEART_HEIGHT;
import static si.um.feri.leaf.pollenGame.config.GameConfig.HEART_WIDTH;
import static si.um.feri.leaf.pollenGame.config.GameConfig.TILE_SIZE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;

import assets.AssetDescriptors;
import assets.RegionNames;
import si.um.feri.leaf.LeafLink;
import si.um.feri.leaf.pollenGame.Player;
import si.um.feri.leaf.pollenGame.Cloud;

public class PollenGameScreen extends ScreenAdapter {
    private final LeafLink game;
    private final AssetManager assetManager;
    private final boolean isTwoPlayerMode;

    private Player player1;
    private Player player2;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private FitViewport gameViewport;
    private FitViewport hudViewport;
    private ShapeRenderer hudShapeRenderer;
    private BitmapFont font;

    private ArrayList<Cloud> clouds;
    private TextureRegion smallCloudTexture;
    private TextureRegion mediumCloudTexture;
    private TextureRegion bigCloudTexture;

    private TextureRegion heartHudTexture;

    private TextureRegion fullHeart;
    private TextureRegion halfHeart;
    private TextureRegion emptyHeart;
    private TextureRegion quarterHeart;
    private TextureRegion threeQuarterHeart;

    private Sound maleCough;
    private Sound femaleCough;

    private float hudScaleFactor;

    public PollenGameScreen(LeafLink game,
                            String player1Character,
                            String player2Character,
                            boolean isTwoPlayerMode) {
        this.game = game;
        this.isTwoPlayerMode = isTwoPlayerMode;
        this.assetManager = game.getAssetManager();

        maleCough = assetManager.get(AssetDescriptors.MALE_COUGHING);
        femaleCough = assetManager.get(AssetDescriptors.FEMALE_COUGHING);

        TextureAtlas tiledAtlas = assetManager.get(AssetDescriptors.TILED_ATLAS);

        heartHudTexture = tiledAtlas.findRegion(RegionNames.HEART_HUD);

        boolean isMale1 = !player1Character.equals("character4");
        player1 = new Player(
            tiledAtlas,
            player1Character,
            TILE_SIZE,
            TILE_SIZE,
            isMale1 ? maleCough : femaleCough
        );
        player1.setControls(Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE);

        if (isTwoPlayerMode && player2Character != null) {
            boolean isMale2 = !player2Character.equals("character4");
            player2 = new Player(
                tiledAtlas,
                player2Character,
                TILE_SIZE,
                TILE_SIZE,
                isMale2 ? maleCough : femaleCough
            );
            player2.setControls(
                Input.Keys.UP,
                Input.Keys.DOWN,
                Input.Keys.LEFT,
                Input.Keys.RIGHT,
                Input.Keys.NUMPAD_0
            );
        }
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load("tiled/mapLEAF.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        float mapPixelWidth = mapWidth * TILE_SIZE;
        float mapPixelHeight = mapHeight * TILE_SIZE;

        camera = new OrthographicCamera();
        gameViewport = new FitViewport(mapPixelWidth, mapPixelHeight, camera);
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0);
        camera.update();

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(800, 480, hudCamera);
        hudCamera.position.set(400, 240, 0);
        hudCamera.update();

        player1.setPosition(50, 170);
        if (isTwoPlayerMode && player2 != null) {
            player2.setPosition(300, 170);
        }

        hudShapeRenderer = new ShapeRenderer();
        font = assetManager.get(AssetDescriptors.TILED_FONT);
        hudScaleFactor = hudViewport.getWorldWidth() / 800f;

        emptyHeart = new TextureRegion(heartHudTexture, 0, 0, HEART_WIDTH, HEART_HEIGHT);
        quarterHeart = new TextureRegion(heartHudTexture, HEART_WIDTH, 0, HEART_WIDTH, HEART_HEIGHT);
        halfHeart = new TextureRegion(heartHudTexture, 2 * HEART_WIDTH, 0, HEART_WIDTH, HEART_HEIGHT);
        threeQuarterHeart = new TextureRegion(heartHudTexture, 3 * HEART_WIDTH, 0, HEART_WIDTH, HEART_HEIGHT);
        fullHeart = new TextureRegion(heartHudTexture, 4 * HEART_WIDTH, 0, HEART_WIDTH, HEART_HEIGHT);

        TextureAtlas atlas = assetManager.get(AssetDescriptors.TILED_ATLAS);
        smallCloudTexture = new TextureRegion(atlas.findRegion(RegionNames.SMALL_CLOUD));
        mediumCloudTexture = new TextureRegion(atlas.findRegion(RegionNames.MEDIUM_CLOUD));
        bigCloudTexture = new TextureRegion(atlas.findRegion(RegionNames.BIG_CLOUD));

        clouds = new ArrayList<>();
        MapObjects cloudObjects = map.getLayers().get("CloudSpawn").getObjects();
        for (Object obj : cloudObjects) {
            if (obj instanceof RectangleMapObject) {
                RectangleMapObject rectObject = (RectangleMapObject) obj;
                String size = (String) rectObject.getProperties().get("class");
                clouds.add(new Cloud(rectObject.getRectangle(), size));
            }
        }
    }

    @Override
    public void render(float delta) {
        if (isGameOver()) {
            renderGameOver();
            return;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateClouds(delta);
        updatePlayers(delta);

        gameViewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        mapRenderer.getBatch().begin();
        player1.render(mapRenderer.getBatch());
        if (isTwoPlayerMode && player2 != null) {
            player2.render(mapRenderer.getBatch());
        }
        renderClouds();
        mapRenderer.getBatch().end();

        hudViewport.apply();
        renderHUD();
    }


    private boolean isGameOver() {
        if (!isTwoPlayerMode) {
            return player1.isDead();
        } else {
            if (player2 == null) {
                return player1.isDead();
            }
            return (player1.isDead() && player2.isDead());
        }
    }

    private void updatePlayers(float delta) {
        // Update player1 only if alive
        if (!player1.isDead()) {
            player1.update(delta, map.getLayers().get("Unwalkable").getObjects(), clouds);
            handlePlayerAction(player1);
        }

        if (isTwoPlayerMode && player2 != null && !player2.isDead()) {
            player2.update(delta, map.getLayers().get("Unwalkable").getObjects(), clouds);
            handlePlayerAction(player2);
        }
    }

    private void handlePlayerAction(Player player) {
        if (Gdx.input.isKeyJustPressed(player.getActionKey())) {
            for (Cloud cloud : clouds) {
                if (cloud.getSize() != null && player.getBounds().overlaps(cloud.getBounds())) {
                    switch (cloud.getSize()) {
                        case "big":
                            cloud.setSize("medium");
                            break;
                        case "medium":
                            cloud.setSize("small");
                            break;
                        case "small":
                            cloud.setSize(null);
                            cloud.setRespawnTimer(8);
                            break;
                    }
                }
            }
        }
    }

    private void updateClouds(float delta) {
        for (Cloud cloud : clouds) {
            if (cloud.getSize() == null) {
                cloud.decreaseRespawnTimer(delta);
                if (cloud.getRespawnTimer() <= 0) {
                    cloud.setSize(getRandomCloudSize());
                }
            }
        }
    }

    private String getRandomCloudSize() {
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0:
                return "big";
            case 1:
                return "medium";
            case 2:
                return "small";
            default:
                return null;
        }
    }

    private void renderClouds() {
        for (Cloud cloud : clouds) {
            if (cloud.getSize() != null) {
                TextureRegion texture;
                switch (cloud.getSize()) {
                    case "big":
                        texture = bigCloudTexture;
                        break;
                    case "medium":
                        texture = mediumCloudTexture;
                        break;
                    case "small":
                        texture = smallCloudTexture;
                        break;
                    default:
                        continue;
                }

                float textureWidth = texture.getRegionWidth();
                float textureHeight = texture.getRegionHeight();

                float rectWidth = cloud.getBounds().width;
                float rectHeight = cloud.getBounds().height;

                float scaleX = rectWidth / textureWidth;
                float scaleY = rectHeight / textureHeight;

                float scale = Math.min(scaleX, scaleY);

                float xOffset = (rectWidth - (textureWidth * scale)) / 2f;
                float yOffset = (rectHeight - (textureHeight * scale)) / 2f;

                mapRenderer.getBatch().draw(
                    texture,
                    cloud.getBounds().x + xOffset,
                    cloud.getBounds().y + yOffset,
                    textureWidth * scale,
                    textureHeight * scale
                );
            }
        }
    }

    private void renderGameOver() {
        mapRenderer.getBatch().begin();

        GlyphLayout gameOverLayout = new GlyphLayout(font, "GAME OVER");
        GlyphLayout restartLayout = new GlyphLayout(font, "Press R to Restart");

        float gameOverX = (hudCamera.viewportWidth - gameOverLayout.width) / 2f;
        float gameOverY = (hudCamera.viewportHeight + gameOverLayout.height) / 2f;

        float restartX = (hudCamera.viewportWidth - restartLayout.width) / 2f;
        float restartY = gameOverY - 50;

        font.draw(mapRenderer.getBatch(), gameOverLayout, gameOverX, gameOverY);
        font.draw(mapRenderer.getBatch(), restartLayout, restartX, restartY);

        mapRenderer.getBatch().end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();
        }
    }

    private void restartGame() {
        player1.reset();
        player1.setPosition(50, 170);

        if (isTwoPlayerMode && player2 != null) {
            player2.reset();
            player2.setPosition(300, 170);
        }

        for (Cloud cloud : clouds) {
            cloud.setSize(getRandomCloudSize());
            cloud.setRespawnTimer(0);
        }
    }

    private void renderHUD() {
        hudCamera.update();
        hudShapeRenderer.setProjectionMatrix(hudCamera.combined);
        mapRenderer.getBatch().setProjectionMatrix(hudCamera.combined);
        mapRenderer.getBatch().begin();

        renderPlayerHearts(player1, 50, hudCamera.viewportHeight - HEART_HEIGHT - 10);

        if (isTwoPlayerMode && player2 != null) {
            renderPlayerHearts(player2, hudCamera.viewportWidth - (5 * (HEART_WIDTH + 5)) - 50, hudCamera.viewportHeight - HEART_HEIGHT - 10);
        }

        mapRenderer.getBatch().end();
    }

    private void renderPlayerHearts(Player player, float startX, float startY) {
        int maxHearts = 5;
        int health = player.getHealth();
        int healthPerHeart = 20;

        for (int i = 0; i < maxHearts; i++) {
            TextureRegion heartRegion;

            int currentHeartHealth = health - (i * healthPerHeart);
            if (currentHeartHealth >= healthPerHeart) {
                heartRegion = fullHeart;
            } else if (currentHeartHealth >= 15) {
                heartRegion = threeQuarterHeart;
            } else if (currentHeartHealth >= 10) {
                heartRegion = halfHeart;
            } else if (currentHeartHealth >= 5) {
                heartRegion = quarterHeart;
            } else {
                heartRegion = emptyHeart;
            }


            mapRenderer.getBatch().draw(
                heartRegion,
                startX + i * (HEART_WIDTH + 5),
                startY,
                HEART_WIDTH,
                HEART_HEIGHT
            );
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        hudViewport.update(width, height);

        hudScaleFactor = hudViewport.getWorldWidth() / 800f;
    }

    @Override
    public void dispose() {
        if (map != null) {
            map.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (player1 != null) {
            player1.dispose();
        }
        if (player2 != null) {
            player2.dispose();
        }
        if (hudShapeRenderer != null) {
            hudShapeRenderer.dispose();
        }
        if (maleCough != null) {
            maleCough.dispose();
        }
        if (femaleCough != null) {
            femaleCough.dispose();
        }
    }
}
