package si.um.feri.leaf.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.types.ObjectId;

import assets.AssetDescriptors;
import assets.RegionNames;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import si.um.feri.leaf.Event;
import si.um.feri.leaf.LeafLink;
import si.um.feri.leaf.Park;
import si.um.feri.leaf.pollenGame.CharacterSelectionScreen;
import si.um.feri.leaf.pollenGame.PollenGameScreen;
import si.um.feri.leaf.utils.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapScreen implements Screen, GestureDetector.GestureListener {

    private final LeafLink game;
    private final AssetManager assetManager;
    private TextureAtlas gameplayAtlas;
    private ShapeRenderer shapeRenderer;
    private Vector3 touchPosition;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;
    private Texture[] mapTiles;
    private Texture markerTexture;
    private SpriteBatch spriteBatch;
    private ZoomXY beginTile;   // Top-left tile
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.5525, 15.7012);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);
    private List<Marker> markers = new ArrayList<>();
    private boolean eventWindowVisible = false;
    private Marker selectedMarker = null;
    private BitmapFont font;

    private Texture buttonTexture;

    private float buttonX, buttonY, buttonWidth, buttonHeight;


    public MapScreen(LeafLink game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        font = new BitmapFont();

        MongoDBHelper.connect();
        MongoCollection<Document> evenCollection = MongoDBHelper.getCollection("events");
        MongoCollection<Document> parkCollection = MongoDBHelper.getCollection("parks");

        fetchAndLogEvents(evenCollection, parkCollection);

        buttonTexture = new Texture(Gdx.files.internal("assets/images/pollen_game.png"));

        buttonWidth = 100f;
        buttonHeight = 80f;
        buttonX = 20f;
        buttonY = Gdx.graphics.getHeight() - buttonHeight - 20f;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 1.0f;
        camera.update();

        touchPosition = new Vector3();

        GestureDetector gestureDetector = new GestureDetector(this);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gestureDetector);
        Gdx.input.setInputProcessor(inputMultiplexer);

        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Constants.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);
            beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - ((Constants.NUM_TILES - 1) / 2), centerTile.y - ((Constants.NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();
        TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES, Constants.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Constants.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    private void fetchAndLogEvents(MongoCollection<Document> eventCollection, MongoCollection<Document> parkCollection) {
        MongoCursor<Document> cursor = eventCollection.find().iterator();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");

        while (cursor.hasNext()) {
            Document event = cursor.next();

            String name = event.getString("name");
            ObjectId parkId = event.getObjectId("parkId");
            String description = event.getString("description");
            Date date = event.getDate("date");

            Document park = parkCollection.find(new Document("_id", parkId)).first();
            String formattedDate = date != null ? dateFormatter.format(date).toString() : "Unknown date";

            if (park != null) {
                Double lat = park.getDouble("lat");
                Double lng = park.getDouble("long");

                if (lat != null && lng != null) {
                    System.out.println("Event: " + name);
                    System.out.println("Park ID: " + parkId);
                    System.out.println("Description: " + description);
                    System.out.println("Date: " + formattedDate);
                    System.out.println("Park Latitude: " + lat);
                    System.out.println("Park Longitude: " + lng);

                    Marker marker = new Marker(lat, lng, name, formattedDate, description);
                    markers.add(marker);
                } else {
                    System.out.println("Park " + parkId + " does not have valid latitude/longitude data.");
                }
            } else {
                System.out.println("Park with ID " + parkId + " not found.");
            }
        }
        cursor.close();
    }

    private void drawMarkers() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        TextureRegion markerTexture = gameplayAtlas.findRegion(RegionNames.MARKER_RED);

        for (Marker marker : markers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(marker.lat, marker.lng, beginTile.x, beginTile.y);

            float markerWidth = 24;  // Marker width in pixels
            float markerHeight = 36; // Marker height in pixels (taller for pin-like effect)
            float offsetY = markerHeight / 2; // Offset to position marker slightly above the point

            spriteBatch.draw(markerTexture,
                markerPosition.x - markerWidth / 2,
                markerPosition.y - offsetY,
                markerWidth,
                markerHeight);
        }

        spriteBatch.end();
    }

    private void drawEventSidebar() {
        // Sidebar dimensions
        float sidebarWidth = 300f; // Fixed width for the sidebar
        float sidebarHeight = Gdx.graphics.getHeight(); // Full screen height
        float sidebarX = Gdx.graphics.getWidth() - sidebarWidth; // Right edge of the screen
        float sidebarY = 0; // Bottom of the screen

        // Draw the sidebar background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.9f); // Dark gray with transparency
        shapeRenderer.rect(sidebarX, sidebarY, sidebarWidth, sidebarHeight);
        shapeRenderer.end();

        // Draw a border around the sidebar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(sidebarX, sidebarY, sidebarWidth, sidebarHeight);
        shapeRenderer.end();

        // Use screen coordinates for text rendering
        spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        spriteBatch.begin();

        // Render the event details inside the sidebar
        font.setColor(Color.WHITE);
        float textX = sidebarX + 10; // Padding from the left edge of the sidebar
        float textY = Gdx.graphics.getHeight() - 20; // Start from the top, leaving space for padding

        if (selectedMarker != null) {
            // Display event information
            font.draw(spriteBatch, "Event Details:", textX, textY);
            textY -= 30; // Space between lines
            font.draw(spriteBatch, "Name: " + selectedMarker.getEventName(), textX, textY);
            textY -= 20;
            font.draw(spriteBatch, "Date: " + selectedMarker.getDate(), textX, textY);
            textY -= 20;
            font.draw(spriteBatch, "Details: " + selectedMarker.getDescription(), textX, textY);
        } else {
            // Default message if no marker is selected
            font.draw(spriteBatch, "No event selected.", textX, textY);
        }

        spriteBatch.end();
    }


    private void drawEventWindow() {
        // Define window dimensions
        float windowWidth = 300f;
        float windowHeight = 150f;
        float margin = 10f; // Margin from the screen edges

        // Calculate window position in the right top corner
        float windowX = Gdx.graphics.getWidth() - windowWidth - margin;
        float windowY = Gdx.graphics.getHeight() - windowHeight - margin;

        // Draw the window background (semi-transparent gray)
        shapeRenderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.9f); // Semi-transparent gray color
        shapeRenderer.rect(windowX, windowY, windowWidth, windowHeight);
        shapeRenderer.end();

        // Draw the window border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE); // White border
        shapeRenderer.rect(windowX, windowY, windowWidth, windowHeight);
        shapeRenderer.end();

        // Draw the text inside the window
        spriteBatch.setProjectionMatrix(spriteBatch.getProjectionMatrix());
        spriteBatch.begin();

        // Set text color
        font.setColor(Color.WHITE);

        // Position text inside the window with padding
        float textX = windowX + 10; // Padding from left edge of window
        float textY = windowY + windowHeight - 10; // Start from near the top edge of the window

        // Display event details line by line
        String[] lines = {
            "Event: " + selectedMarker.getEventName(),
            "Date: " + selectedMarker.getDate(),
            "Details: " + selectedMarker.getDescription()
        };

        for (String line : lines) {
            font.draw(spriteBatch, line, textX, textY);
            textY -= 20; // Move down for each line
        }

        spriteBatch.end();
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers();

        drawStartButton();

        checkButtonClick();

        if (eventWindowVisible && selectedMarker != null) {
//            drawEventWindow();
            drawEventSidebar();
        }
    }

    private void checkButtonClick() {
        if (Gdx.input.justTouched()) {
            float screenX = Gdx.input.getX();
            float screenY = Gdx.input.getY();

            float correctedY = Gdx.graphics.getHeight() - screenY;

            boolean isInsideButton = screenX >= buttonX && screenX <= buttonX + buttonWidth
                && correctedY >= buttonY && correctedY <= buttonY + buttonHeight;

            if (isInsideButton) {
                startPollenGame();
            }
        }
    }

    private void startPollenGame() {
        Gdx.app.log("MapScreen", "Starting another game!");
        game.setScreen(new CharacterSelectionScreen(game));
    }

    private void drawStartButton() {
        spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()));

        spriteBatch.begin();
        spriteBatch.draw(buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight);
        spriteBatch.end();
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
        // Optional: Handle pause
    }

    @Override
    public void resume() {
        // Optional: Handle resume
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
//        assetManager.dispose();
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (markerTexture != null) {
            markerTexture.dispose();
        }

        if (buttonTexture != null) {
            buttonTexture.dispose();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition); // Convert screen to world coordinates

        for (Marker marker : markers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(marker.lat, marker.lng, beginTile.x, beginTile.y);

            float markerWidth = 24;  // Adjust as needed
            float markerHeight = 36;

            if (touchPosition.x > markerPosition.x - markerWidth / 2 &&
                touchPosition.x < markerPosition.x + markerWidth / 2 &&
                touchPosition.y > markerPosition.y - markerHeight / 2 &&
                touchPosition.y < markerPosition.y + markerHeight / 2) {

                if (count == 1) { // Single click
                    selectedMarker = marker;
                    eventWindowVisible = true;
                } else if (count == 2) { // Double click
                    startGame(marker);
                }
                return true;
            }
        }

        eventWindowVisible = false;
        selectedMarker = null;
        return false;
    }


    private void startGame(Marker marker) {
        Gdx.app.log("MapScreen", "Starting game for event: " + marker.getEventName());
        try {
            game.setScreen(new GameScreen(game, marker));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance)
            camera.zoom += 0.02;
        else
            camera.zoom -= 0.02;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Constants.MAP_WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Constants.MAP_HEIGHT - effectiveViewportHeight / 2f);
    }
}
