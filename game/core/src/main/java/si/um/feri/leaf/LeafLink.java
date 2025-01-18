package si.um.feri.leaf;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.types.ObjectId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import si.um.feri.leaf.screen.IntroScreen;
import si.um.feri.leaf.utils.ApiClient;
import si.um.feri.leaf.utils.Constants;
import si.um.feri.leaf.utils.Geolocation;
import si.um.feri.leaf.utils.LeafLinkApi;
import si.um.feri.leaf.utils.MapRasterTiles;
import si.um.feri.leaf.utils.Marker;
import si.um.feri.leaf.utils.MongoDBHelper;
import si.um.feri.leaf.utils.ZoomXY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeafLink extends Game {
    private static final Logger log = new Logger(LeafLink.class.getSimpleName(), Logger.DEBUG);
    private SpriteBatch batch;
    private Texture image;
    private AssetManager assetManager;
    private TextureAtlas gameplay;
    public BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport hudViewport;
    private ShapeRenderer renderer;
    private boolean debug = true;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, camera);
        hudViewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderer = new ShapeRenderer();
        font= new BitmapFont();


        assetManager = new AssetManager();
        assetManager.getLogger().setLevel(Logger.DEBUG);
        setScreen(new IntroScreen(this));

    }


    @Override
    public void dispose() {
        batch.dispose();
        assetManager.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}
