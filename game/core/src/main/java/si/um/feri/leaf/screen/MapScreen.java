    package si.um.feri.leaf.screen;

    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.Input;
    import com.badlogic.gdx.InputMultiplexer;
    import com.badlogic.gdx.Screen;
    import com.badlogic.gdx.assets.AssetManager;
    import com.badlogic.gdx.graphics.Color;
    import com.badlogic.gdx.graphics.OrthographicCamera;
    import com.badlogic.gdx.graphics.Texture;
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
    import si.um.feri.leaf.utils.*;

    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.util.ArrayList;
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

        public MapScreen(LeafLink game) {
            this.game = game;
            this.assetManager = game.getAssetManager();
        }

        @Override
        public void show() {
            shapeRenderer = new ShapeRenderer();
            spriteBatch = new SpriteBatch();
            gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

            MongoDBHelper.connect();
            MongoCollection<Document> evenCollection = MongoDBHelper.getCollection("events");
            MongoCollection<Document> parkCollection = MongoDBHelper.getCollection("parks");


//            fetchAndLogEvents();
            fetchAndLogEvents(evenCollection, parkCollection);

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

            while (cursor.hasNext()) {
                Document event = cursor.next();

                String name = event.getString("name");
                ObjectId parkId = event.getObjectId("parkId");
                String description = event.getString("description");
                String date = event.getDate("date").toString();

                Document park = parkCollection.find(new Document("_id", parkId)).first();

                if (park != null) {
                    Double lat = park.getDouble("lat");
                    Double lng = park.getDouble("long");

                    if (lat != null && lng != null) {
                        System.out.println("Event: " + name);
                        System.out.println("Park ID: " + parkId);
                        System.out.println("Description: " + description);
                        System.out.println("Date: " + date);
                        System.out.println("Park Latitude: " + lat);
                        System.out.println("Park Longitude: " + lng);

                        Marker marker = new Marker(lat, lng);
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


        private void fetchParkDetails(String parkId) {
            LeafLinkApi apiService = ApiClient.getRetrofitInstance().create(LeafLinkApi.class);

            Call<Park> parkCall = apiService.getPark(parkId);
            parkCall.enqueue(new Callback<Park>() {
                @Override
                public void onResponse(Call<Park> call, Response<Park> response) {
                    if (response.isSuccessful()) {
                        Park park = response.body();
                        if (park != null && park.getLat() != null && park.getLng() != null) {
                            Gdx.app.log("LeafLink", "Fetched Park: " + park.getName() + " (Lat: " + park.getLat() + ", Lng: " + park.getLng() + ")");
                            // Add marker with valid coordinates
                            Marker marker = new Marker(park.getLat(), park.getLng());
                            markers.add(marker);
                        } else {
                            Gdx.app.log("LeafLink", "Invalid coordinates for Park ID: " + parkId);
                            addDefaultMarker(); // Add default marker if coordinates are invalid
                        }
                    } else {
                        Gdx.app.log("LeafLink", "Failed to fetch park details: " + response.message());
                        addDefaultMarker(); // Add default marker in case of failure
                    }
                }

                @Override
                public void onFailure(Call<Park> call, Throwable t) {
                    Gdx.app.log("LeafLink", "Error fetching park details: " + t.getMessage());
                    addDefaultMarker(); // Add default marker in case of failure
                }
            });
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


        private void addDefaultMarker() {
            // You can use any default coordinates here. For example:
            double defaultLat = 46.5525;
            double defaultLng = 15.7012;

            Marker defaultMarker = new Marker(defaultLat, defaultLng);
            markers.add(defaultMarker);
            Gdx.app.log("LeafLink", "Default marker added at: " + defaultLat + ", " + defaultLng);
        }


        @Override
        public void render(float delta) {
            ScreenUtils.clear(0, 0, 0, 1);

            handleInput();

            camera.update();

            tiledMapRenderer.setView(camera);
            tiledMapRenderer.render();

            drawMarkers();
        }

        @Override
        public void resize(int width, int height) {
            // Resize logic if required
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
            assetManager.dispose();
            if (tiledMap != null) {
                tiledMap.dispose();
            }
            if (markerTexture != null) {
                markerTexture.dispose();
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
            // Convert screen touch position to world coordinates
            touchPosition.set(x, y, 0);
            camera.unproject(touchPosition);

            // Iterate through markers and check if the touch position is within the bounds of the marker
            for (Marker marker : markers) {
                Vector2 markerPosition = MapRasterTiles.getPixelPosition(marker.lat, marker.lng, beginTile.x, beginTile.y);

                float markerWidth = 24;
                float markerHeight = 36;
                float offsetY = markerHeight / 2;

                // Check if touch position is within the marker area
                if (touchPosition.x > markerPosition.x - markerWidth / 2 &&
                    touchPosition.x < markerPosition.x + markerWidth / 2 &&
                    touchPosition.y > markerPosition.y - offsetY &&
                    touchPosition.y < markerPosition.y + markerHeight - offsetY) {

                    // Marker clicked, open new screen
                    try {
                        game.setScreen(new GameScreen(game, marker));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
            }

            return false;
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
