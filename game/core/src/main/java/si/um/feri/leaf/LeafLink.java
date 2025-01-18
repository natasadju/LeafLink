package si.um.feri.leaf;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.leaf.screen.IntroScreen;
import si.um.feri.leaf.utils.Constants;
import assets.AssetDescriptors;


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
        font = new BitmapFont();


        assetManager = new AssetManager();
        assetManager.load(AssetDescriptors.TILED_ATLAS);
        assetManager.load(AssetDescriptors.TILED_FONT);
        assetManager.load(AssetDescriptors.MALE_COUGHING);
        assetManager.load(AssetDescriptors.FEMALE_COUGHING);
        assetManager.load(AssetDescriptors.CHOOSE_CHARACTER);
        assetManager.load(AssetDescriptors.CHOOSE_SCREEN);
        assetManager.getLogger().setLevel(Logger.DEBUG);
        assetManager.finishLoading();
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
