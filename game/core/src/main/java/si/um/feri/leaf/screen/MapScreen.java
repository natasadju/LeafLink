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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.types.ObjectId;

import assets.AssetDescriptors;
import assets.RegionNames;
import si.um.feri.leaf.LeafLink;
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
    private ZoomXY beginTile;
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.5525, 15.7012);
    private List<Marker> markers = new ArrayList<>();
    private List<AirMarker> airMarkers = new ArrayList<>();
    private boolean eventWindowVisible = false;
    private Marker selectedMarker = null;
    private AirMarker selectedAirMarker = null;
    private BitmapFont font;
    private Stage stage;
    private Skin uiSkin;
    private TextButton editButton;
    private SelectBox<String> typeSelectBox;
    private boolean showParks = true;


    public MapScreen(LeafLink game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        uiSkin= assetManager.get("ui/flat-earth-ui.json", Skin.class);
        font= new BitmapFont();


        stage = new Stage(new ScreenViewport());
        editButton = new TextButton("Edit Data", uiSkin);
        editButton.setSize(150, 50);
        editButton.setPosition(Gdx.graphics.getWidth() - 280, 20);
        editButton.setTouchable(Touchable.enabled);

        GestureDetector gestureDetector = new GestureDetector(this);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gestureDetector);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        editButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Edit Data button clicked!");
                editData();
            }
        });

        typeSelectBox = new SelectBox<>(uiSkin);
        typeSelectBox.setItems("Events", "Parks");
        typeSelectBox.setSize(200, 40);
        typeSelectBox.setPosition(20, Gdx.graphics.getHeight() - 60);
        typeSelectBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Toggle between parks and events
                showParks = typeSelectBox.getSelected().equals("Parks");
                updateMarkers(); // Update markers based on selection
            }
        });

        stage.addActor(editButton);
        stage.addActor(typeSelectBox);

        MongoDBHelper.connect();
        MongoCollection<Document> evenCollection = MongoDBHelper.getCollection("events");
        MongoCollection<Document> parkCollection = MongoDBHelper.getCollection("parks");

        fetchAndLogEvents(evenCollection, parkCollection);
        fetchAndLogAirQuality();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.viewportWidth = Constants.MAP_WIDTH / 2f;
        camera.viewportHeight = Constants.MAP_HEIGHT / 2f;
        camera.zoom = 1.0f;
        camera.update();

        touchPosition = new Vector3();


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
        @SuppressWarnings("NewApi") SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        while (cursor.hasNext()) {
            Document event = cursor.next();

            String name = event.getString("name");
            ObjectId parkId = event.getObjectId("parkId");
            String description = event.getString("description");
            Object dateObject = event.get("date");

            Date date = null;

            if (dateObject instanceof String) {
                try {
                    date = dateFormatter.parse((String) dateObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    date = null;
                }
            } else if (dateObject instanceof Date) {
                date = (Date) dateObject;
            }

            String formattedDate = date != null ? outputDateFormat.format(date) : "Unknown date";

            Document park = parkCollection.find(new Document("_id", parkId)).first();
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

                    Marker marker = new Marker(lat, lng, name, date, description);
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

    private void fetchAndLogAirQuality() {
        MongoDBHelper.connect();
        MongoCollection<Document> airQualityCollection = MongoDBHelper.getCollection("airqualities");

        String[] stations = {"MB Vrbanski", "MB Titova"};
        double[][] coordinates = {
            {46.568996, 15.630670}, // MB Vrbanski
            {46.560192, 15.651521}  // MB Titova
        };

        for (int i = 0; i < stations.length; i++) {
            String station = stations[i];
            double lat = coordinates[i][0];
            double lon = coordinates[i][1];

            Document latestRecord = airQualityCollection.find(new Document("station", station))
                .sort(new Document("timestamp", -1))
                .first();

            if (latestRecord != null) {
                Integer pm25 = latestRecord.getInteger("pm25");
                Integer pm10 = latestRecord.getInteger("pm10");
                Date timestamp = latestRecord.getDate("timestamp");

                System.out.println("Latest Air Quality Measurement for " + station + ":");
                System.out.println("PM2.5: " + pm25 + " µg/m³");
                System.out.println("PM10: " + pm10 + " µg/m³");
                System.out.println("Timestamp: " + timestamp);

                String markerStyle = determineMarkerStyle(pm25, pm10);
                placeMarker(lat, lon, station, markerStyle, timestamp, pm25,pm10);

            } else {
                System.out.println("No air quality data available for station '" + station + "'.");
            }
        }
    }

    private String determineMarkerStyle(Integer pm25, Integer pm10) {
        if (pm25 == null || pm10 == null) return "defaultMarker";

        if (pm25 <= 10 && pm10 <= 20) return "greenMarker"; // Good
        if (pm25 <= 20 && pm10 <= 40) return "yellowMarker"; // Moderate
        if (pm25 <= 25 && pm10 <= 50) return "orangeMarker"; // Unhealthy for sensitive groups
        if (pm25 <= 50 && pm10 <= 100) return "redMarker"; // Unhealthy
        if (pm25 <= 75 && pm10 <= 150) return "purpleMarker"; // Very unhealthy
        return "brownMarker"; // Hazardous
    }

    private void placeMarker(double lat, double lon, String station, String markerStyle, Date timestamp, Integer pm25, Integer pm10) {
        System.out.println("Placing " + markerStyle + " marker for station " + station + " at coordinates: (" + lat + ", " + lon + ")");
        TextureRegion markerIcon = getMarkerIcon(markerStyle);

        if (markerIcon == null) {
            System.err.println("Failed to place marker: Texture not found for style " + markerStyle);
            return;
        }

        airMarkers.add(new AirMarker(lat, lon, station, timestamp, markerStyle, pm25, pm10));

    }

    private TextureRegion getMarkerIcon(String markerStyle) {
        TextureRegion region = null;

        switch (markerStyle) {
            case "greenMarker":
                region = gameplayAtlas.findRegion(RegionNames.AIR_GREEN);
                break;
            case "yellowMarker":
                region = gameplayAtlas.findRegion(RegionNames.AIR_YELLOW);
                break;
            case "orangeMarker":
                region = gameplayAtlas.findRegion(RegionNames.AIR_ORAGNE);
                break;
            case "redMarker":
                region = gameplayAtlas.findRegion(RegionNames.AIR_RED);
                break;
            case "purpleMarker":
                region = gameplayAtlas.findRegion(RegionNames.AIR_PURPLE);
                break;
            case "brownMarker":
                region = gameplayAtlas.findRegion(RegionNames.MARKER_RED);
                break;
            default:
                region = gameplayAtlas.findRegion(RegionNames.MARKER_GREEN);
                break;
        }

        if (region == null) {
            System.err.println("Region not found for marker style: " + markerStyle);
            return null;
        }

        return region;
    }




    private void updateMarkers() {
        markers.clear();

        if (showParks) {
            MongoDBHelper.connect();
            MongoCollection<Document> parkCollection = MongoDBHelper.getCollection("parks");
            fetchAndLogParks(parkCollection);
        } else {
            MongoDBHelper.connect();
            MongoCollection<Document> eventCollection = MongoDBHelper.getCollection("events");
            MongoCollection<Document> parkCollection = MongoDBHelper.getCollection("parks");
            fetchAndLogEvents(eventCollection, parkCollection);
        }
    }

    private void fetchAndLogParks(MongoCollection<Document> parkCollection) {
        MongoCursor<Document> cursor = parkCollection.find().iterator();
        while (cursor.hasNext()) {
            Document park = cursor.next();
            String name = park.getString("name");
            Double lat = park.getDouble("lat");
            Double lng = park.getDouble("long");

            if (lat != null && lng != null) {
                Marker marker = new Marker(lat, lng, name, null, "Park Location");
                markers.add(marker);
            }
        }
        cursor.close();
    }


    private void drawMarkers() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        TextureRegion markerTexture = gameplayAtlas.findRegion(RegionNames.LEAF_GREEN);

        for (Marker marker : markers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(marker.lat, marker.lng, beginTile.x, beginTile.y);

            float markerWidth = 35;
            float markerHeight = 40;
            float offsetY = markerHeight / 2;

            spriteBatch.draw(markerTexture,
                markerPosition.x - markerWidth / 2,
                markerPosition.y - offsetY,
                markerWidth,
                markerHeight);
        }

        spriteBatch.end();
    }

    private void drawAirMarkers() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Draw air markers
        for (AirMarker airMarker : airMarkers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(
                airMarker.lat,
                airMarker.lng,
                beginTile.x,
                beginTile.y
            );

            float markerWidth = 50;
            float markerHeight = 50;
            float offsetY = markerHeight / 2;

            TextureRegion markerTexture = getMarkerIcon(airMarker.getMarkerStyle());
            if (markerTexture != null) {
                spriteBatch.draw(markerTexture,
                    markerPosition.x - markerWidth / 2,
                    markerPosition.y - offsetY,
                    markerWidth,
                    markerHeight);
            }
        }

        spriteBatch.end();
    }

    private void drawEventSidebar() {
        // Sidebar dimensions
        float sidebarWidth = 300f;
        float sidebarHeight = Gdx.graphics.getHeight();
        float sidebarX = Gdx.graphics.getWidth() - sidebarWidth;
        float sidebarY = 0;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.rect(sidebarX, sidebarY, sidebarWidth, sidebarHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(sidebarX, sidebarY, sidebarWidth, sidebarHeight);
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        spriteBatch.begin();

        font.setColor(Color.WHITE);
        float textX = sidebarX + 10;
        float textY = Gdx.graphics.getHeight() - 20;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");

        if (selectedAirMarker != null) {
            font.draw(spriteBatch, "Air Quality Info:", textX, textY);
            textY -= 30;

            String stationName = selectedAirMarker.getStation();
            Integer pm25 = selectedAirMarker.getPm25();
            Integer pm10 = selectedAirMarker.getPm10();
            String formattedDate = selectedAirMarker.getDate() != null ? dateFormatter.format(selectedAirMarker.getDate()) : "Unknown date";

            font.draw(spriteBatch, "Station: " + stationName, textX, textY);
            textY -= 20;
            font.draw(spriteBatch, "PM2.5: " + (pm25 != null ? pm25 + " µg/m3" : "No data"), textX, textY);
            textY -= 20;
            font.draw(spriteBatch, "PM10: " + (pm10 != null ? pm10 + " µg/m3" : "No data"), textX, textY);
            textY -= 20;
            font.draw(spriteBatch, "Last Updated: " + formattedDate, textX, textY);

        } else if (selectedMarker != null) {
            if (showParks) {
                font.draw(spriteBatch, "Selected Park:", sidebarX + 20, sidebarHeight - 20);
                font.draw(spriteBatch, "Park Name: " + selectedMarker.getEventName(), sidebarX + 20, sidebarHeight - 60);
            } else {
                font.draw(spriteBatch, "Event Details:", textX, textY);
                textY -= 30;
                font.draw(spriteBatch, "Name: " + selectedMarker.getEventName(), textX, textY);
                textY -= 20;

                String formattedDate = selectedMarker.getDate() != null ? dateFormatter.format(selectedMarker.getDate()) : "Unknown date";
                font.draw(spriteBatch, "Date: " + formattedDate, textX, textY);
                textY -= 20;

                font.draw(spriteBatch, "Details: " + selectedMarker.getDescription(), textX, textY);
            }
        } else {
            font.draw(spriteBatch, "No event or air quality station selected.", textX, textY);
        }

        spriteBatch.end();
    }



    private void editData() {
        if (selectedMarker == null) {
            System.out.println("No event selected to edit.");
            return;
        }

        final TextField nameField = new TextField(selectedMarker.getEventName(), uiSkin);
        final TextField dateField = new TextField(selectedMarker.getDate() != null ? new SimpleDateFormat("dd MMM yyyy, HH:mm").format(selectedMarker.getDate()) : "", uiSkin);
        final TextArea descriptionField = new TextArea(selectedMarker.getDescription(), uiSkin);

        Dialog editDialog = new Dialog("Edit Event", uiSkin) {
            @Override
            protected void result(Object object) {
                if (object.equals(true)) {
                    String newName = nameField.getText();
                    String newDate = dateField.getText();
                    String newDescription = descriptionField.getText();

                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");
                    try {
                        Date date = inputDateFormat.parse(newDate);
                        selectedMarker.setDate(date);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error parsing the date.");
                        return;
                    }

                    selectedMarker.setEventName(newName);
                    selectedMarker.setDescription(newDescription);

                    saveMarkerToDatabase(selectedMarker);

                    System.out.println("Event updated successfully.");
                } else {
                    System.out.println("Edit cancelled.");
                }
            }
        };

        editDialog.getContentTable().add("Name: ").left();
        editDialog.getContentTable().add(nameField).width(300).row();
        editDialog.getContentTable().add("Date: ").left();
        editDialog.getContentTable().add(dateField).width(300).row();
        editDialog.getContentTable().add("Description: ").left();
        editDialog.getContentTable().add(descriptionField).width(300).row();

        editDialog.button("Save", true);
        editDialog.button("Cancel", false);

        editDialog.show(stage);
    }




    private void saveMarkerToDatabase(Marker marker) {
        MongoCollection<Document> eventCollection = MongoDBHelper.getCollection("events");

        Document query = new Document("name", marker.getEventName());
        Document updatedEvent = new Document()
            .append("name", marker.getEventName())
            .append("date", marker.getDate())
            .append("description", marker.getDescription());

        eventCollection.updateOne(query, new Document("$set", updatedEvent));
    }



    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers();
        drawAirMarkers();
        if (eventWindowVisible && (selectedMarker != null || selectedAirMarker != null)) {
            drawEventSidebar();
            editButton.setVisible(true);
            if(showParks || selectedAirMarker != null) editButton.setVisible(false);
        } else {
            editButton.setVisible(false);
        }


        stage.act(delta);
        stage.draw();
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
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (markerTexture != null) {
            markerTexture.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }



    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            touchPosition.set(x, y, 0);
            camera.unproject(touchPosition);

            for (Marker marker : markers) {
                Vector2 markerPosition = MapRasterTiles.getPixelPosition(marker.lat, marker.lng, beginTile.x, beginTile.y);

                float markerWidth = 24;
                float markerHeight = 36;
                if (touchPosition.x >= markerPosition.x - markerWidth / 2 &&
                    touchPosition.x <= markerPosition.x + markerWidth / 2 &&
                    touchPosition.y >= markerPosition.y - markerHeight / 2 &&
                    touchPosition.y <= markerPosition.y + markerHeight / 2) {

                    selectedMarker = marker;
                    eventWindowVisible = true;
                    return true;
                }
            }
        }

        for (AirMarker airMarker : airMarkers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(airMarker.lat, airMarker.lng, beginTile.x, beginTile.y);

            float markerWidth = 35;
            float markerHeight = 40;

            if (touchPosition.x > markerPosition.x - markerWidth / 2 &&
                touchPosition.x < markerPosition.x + markerWidth / 2 &&
                touchPosition.y > markerPosition.y - markerHeight / 2 &&
                touchPosition.y < markerPosition.y + markerHeight / 2) {

                selectedAirMarker= airMarker;
                eventWindowVisible= true;
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean tap(float x, float y, int count, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);

        for (Marker marker : markers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(marker.lat, marker.lng, beginTile.x, beginTile.y);
            float markerWidth = 24;
            float markerHeight = 36;

            if (touchPosition.x > markerPosition.x - markerWidth / 2 &&
                touchPosition.x < markerPosition.x + markerWidth / 2 &&
                touchPosition.y > markerPosition.y - markerHeight / 2 &&
                touchPosition.y < markerPosition.y + markerHeight / 2) {

                selectedAirMarker = null;

                if (count == 1) {
                    selectedMarker = marker;
                    eventWindowVisible = true;
                } else if (count == 2) {
                    startGame1(marker);
                }
                return true;
            }
        }

        for (AirMarker airMarker : airMarkers) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(airMarker.lat, airMarker.lng, beginTile.x, beginTile.y);
            float markerWidth = 35;
            float markerHeight = 40;

            if (touchPosition.x > markerPosition.x - markerWidth / 2 &&
                touchPosition.x < markerPosition.x + markerWidth / 2 &&
                touchPosition.y > markerPosition.y - markerHeight / 2 &&
                touchPosition.y < markerPosition.y + markerHeight / 2) {

                selectedMarker = null;

                if (count == 1) {
                    selectedAirMarker = airMarker;
                    eventWindowVisible = true;
                }
                else if (count == 2) {
                    startGame2(airMarker);
                }
                return true;
            }
        }

        eventWindowVisible = false;
        return false;
    }




    private void startGame1(Marker marker) {
        Gdx.app.log("MapScreen", "Starting game for event: " + marker.getEventName());
        try {
            game.setScreen(new GameScreen(game, marker));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void startGame2(AirMarker marker) {
        Gdx.app.log("MapScreen", "Starting game for event: " + marker.getStation());
        try {
            game.setScreen(new NewGame(game, marker));
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
