package si.um.feri.leaf.pollenGame.screen;

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
    private static final int TILE_SIZE = 16;

    private final LeafLink game;
    private final AssetManager assetManager;
    private final boolean isTwoPlayerMode;

    private Player player1;
    private Player player2;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private ShapeRenderer hudShapeRenderer;
    private BitmapFont font;

    private ArrayList<Cloud> clouds;
    private TextureRegion smallCloudTexture;
    private TextureRegion mediumCloudTexture;
    private TextureRegion bigCloudTexture;

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
        FitViewport viewport = new FitViewport(mapPixelWidth, mapPixelHeight);
        camera = new OrthographicCamera();
        viewport.setCamera(camera);
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0);
        camera.update();

        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        hudCamera.update();

        player1.setPosition(50, 170);
        if (isTwoPlayerMode && player2 != null) {
            player2.setPosition(300, 170);
        }

        hudShapeRenderer = new ShapeRenderer();
        font = assetManager.get(AssetDescriptors.TILED_FONT);
        hudScaleFactor = Gdx.graphics.getWidth() / 800f;

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

        mapRenderer.setView(camera);
        mapRenderer.render();

        mapRenderer.getBatch().begin();
        player1.render(mapRenderer.getBatch());
        if (isTwoPlayerMode && player2 != null) {
            player2.render(mapRenderer.getBatch());
        }
        renderClouds();
        mapRenderer.getBatch().end();

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
                            cloud.setRespawnTimer(5);
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
            case 0: return "big";
            case 1: return "medium";
            case 2: return "small";
            default: return null;
        }
    }

    private void renderClouds() {
        for (Cloud cloud : clouds) {
            if (cloud.getSize() != null) {
                TextureRegion texture;
                switch (cloud.getSize()) {
                    case "big":    texture = bigCloudTexture;    break;
                    case "medium": texture = mediumCloudTexture; break;
                    case "small":  texture = smallCloudTexture;  break;
                    default:       continue;
                }
                mapRenderer.getBatch().draw(
                    texture,
                    cloud.getBounds().x,
                    cloud.getBounds().y,
                    cloud.getBounds().width,
                    cloud.getBounds().height
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

        hudShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float p1BarX = 20 * hudScaleFactor;
        float p1BarY = hudCamera.viewportHeight - (30 * hudScaleFactor);
        drawHealthBar(hudShapeRenderer, player1, p1BarX, p1BarY);

        if (isTwoPlayerMode && player2 != null) {
            float p2BarX = 20 * hudScaleFactor;
            float p2BarY = hudCamera.viewportHeight - (60 * hudScaleFactor);
            drawHealthBar(hudShapeRenderer, player2, p2BarX, p2BarY);
        }

        hudShapeRenderer.end();

        mapRenderer.getBatch().begin();
        font.setColor(Color.YELLOW);
        font.getData().setScale(0.3f * hudScaleFactor);

        String p1ScoreText = "P1 Score: " + player1.getScore();
        font.draw(mapRenderer.getBatch(), p1ScoreText, 20 * hudScaleFactor,
            hudCamera.viewportHeight - (80 * hudScaleFactor));

        if (isTwoPlayerMode && player2 != null) {
            String p2ScoreText = "P2 Score: " + player2.getScore();
            font.draw(mapRenderer.getBatch(), p2ScoreText, 20 * hudScaleFactor,
                hudCamera.viewportHeight - (100 * hudScaleFactor));
        }
        mapRenderer.getBatch().end();
    }

    private void drawHealthBar(ShapeRenderer shapeRenderer, Player player, float x, float y) {
        float barWidth = 100 * hudScaleFactor;
        float barHeight = 10 * hudScaleFactor;

        shapeRenderer.setColor(0.5f, 0, 0, 1);
        shapeRenderer.rect(x, y, barWidth + 4, barHeight + 4);

        float healthPercent = (float) player.getHealth() / 100f;
        float fillWidth = healthPercent * barWidth;
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(x + 2, y + 2, fillWidth, barHeight);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = map.getProperties().get("width", Integer.class) * TILE_SIZE;
        camera.viewportHeight = map.getProperties().get("height", Integer.class) * TILE_SIZE;
        camera.update();

        hudCamera.setToOrtho(false, width, height);
        hudCamera.position.set(width / 2f, height / 2f, 0);
        hudCamera.update();

        hudScaleFactor = width / 800f;
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
