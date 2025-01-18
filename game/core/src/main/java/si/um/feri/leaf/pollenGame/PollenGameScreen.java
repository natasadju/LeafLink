package si.um.feri.leaf.pollenGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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

import javax.swing.plaf.synth.Region;

import assets.AssetDescriptors;
import assets.RegionNames;
import si.um.feri.leaf.LeafLink;

public class PollenGameScreen extends ScreenAdapter {
    private static final int TILE_SIZE = 16;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Player player;
    private LeafLink game;
    private float hudScaleFactor;

    private ShapeRenderer hudShapeRenderer;
    private BitmapFont font;

    private Texture characterTexture;

    // Textures
    private TextureRegion smallCloudTexture;
    private TextureRegion bigCloudTexture;
    private TextureRegion mediumCloudTexture;
    private TextureAtlas tiledAtlas;

    private ArrayList<Cloud> clouds;

    public PollenGameScreen(LeafLink game, String characterName) {
        this.game = game;
        this.tiledAtlas = game.getAssetManager().get(AssetDescriptors.TILED_ATLAS);
        this.player = new Player(tiledAtlas, characterName, TILE_SIZE, TILE_SIZE);
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

        player.setPosition(10, 170);

        hudShapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        hudScaleFactor = Gdx.graphics.getWidth() / 800f;

        smallCloudTexture = new TextureRegion(tiledAtlas.findRegion(RegionNames.SMALL_CLOUD));
        mediumCloudTexture = new TextureRegion(tiledAtlas.findRegion(RegionNames.MEDIUM_CLOUD));
        bigCloudTexture = new TextureRegion(tiledAtlas.findRegion(RegionNames.BIG_CLOUD));

        clouds = new ArrayList<>();
        MapObjects cloudObjects = map.getLayers().get("CloudSpawn").getObjects();
        cloudObjects.forEach(object -> {
            RectangleMapObject rectObject = (RectangleMapObject) object;
            String size = (String) object.getProperties().get("class");
            clouds.add(new Cloud(rectObject.getRectangle(), size));
        });
    }


    @Override
    public void render(float delta) {
        if (player.isDead()) {
            gameOver();
            return;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateClouds(delta);
        handleInput();

        mapRenderer.setView(camera); // Camera stays static
        mapRenderer.render();

        player.render(mapRenderer.getBatch());
        player.update(delta, map.getLayers().get("Unwalkable").getObjects());
        renderClouds();
        renderHUD();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
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
                    cloud.setSize("big");
                }
            }
        }
    }


    private void renderClouds() {
        mapRenderer.getBatch().begin();
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

                mapRenderer.getBatch().draw(
                    texture,
                    cloud.getBounds().x,
                    cloud.getBounds().y,
                    cloud.getBounds().width,
                    cloud.getBounds().height
                );
            }
        }
        mapRenderer.getBatch().end();
    }


    private void gameOver() {
        mapRenderer.getBatch().begin();

        font.getData().setScale(1.5f * hudScaleFactor);
        font.setColor(Color.RED);
        GlyphLayout gameOverLayout = new GlyphLayout(font, "GAME OVER");
        float gameOverX = (hudCamera.viewportWidth - gameOverLayout.width) / 2f;
        float gameOverY = (hudCamera.viewportHeight + gameOverLayout.height) / 2f;
        font.draw(mapRenderer.getBatch(), gameOverLayout, gameOverX, gameOverY);

        font.getData().setScale(0.5f * hudScaleFactor);
        font.setColor(Color.WHITE);
        GlyphLayout restartLayout = new GlyphLayout(font, "Press R to Restart");
        float restartX = (hudCamera.viewportWidth - restartLayout.width) / 2f;
        float restartY = gameOverY - gameOverLayout.height - 50 * hudScaleFactor;
        font.draw(mapRenderer.getBatch(), restartLayout, restartX, restartY);

        mapRenderer.getBatch().end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();
        }
    }

    private void restartGame() {
        player.reset();
        for (Cloud cloud : clouds) {
            cloud.setSize("big");
            cloud.setRespawnTimer(0);
        }
    }

    private void renderHUD() {
        hudCamera.update();
        hudShapeRenderer.setProjectionMatrix(hudCamera.combined);
        mapRenderer.getBatch().setProjectionMatrix(hudCamera.combined);

        float barWidth = 100 * hudScaleFactor;
        float barHeight = 10 * hudScaleFactor;
        float barX = 20 * hudScaleFactor;
        float barY = hudCamera.viewportHeight - (30 * hudScaleFactor);
        float textX = 20 * hudScaleFactor;
        float textY = hudCamera.viewportHeight - (50 * hudScaleFactor);

        hudShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hudShapeRenderer.setColor(0.5f, 0, 0, 1);
        hudShapeRenderer.rect(barX, barY, barWidth + 4, barHeight + 4);

        float healthPercent = (float) player.getHealth() / 100.0f;
        float healthBarWidth = healthPercent * barWidth;
        hudShapeRenderer.setColor(1, 0, 0, 1);
        hudShapeRenderer.rect(barX + 2, barY + 2, healthBarWidth, barHeight);
        hudShapeRenderer.end();

        mapRenderer.getBatch().begin();
        font.getData().setScale(0.3f * hudScaleFactor);
        font.setColor(Color.YELLOW);
        font.draw(mapRenderer.getBatch(), "Lost Items Found: " + player.getScore(), textX, textY);
        mapRenderer.getBatch().end();
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
        map.dispose();
        mapRenderer.dispose();
        player.dispose();
        hudShapeRenderer.dispose();
    }
}
