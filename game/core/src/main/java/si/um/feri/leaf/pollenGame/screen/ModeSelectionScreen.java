package si.um.feri.leaf.pollenGame.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import assets.AssetDescriptors;
import si.um.feri.leaf.LeafLink;
import com.badlogic.gdx.Gdx;

public class ModeSelectionScreen extends ScreenAdapter {
    private final LeafLink game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private OrthographicCamera camera;

    private AssetManager assetManager;

    public ModeSelectionScreen(LeafLink game) {
        this.game = game;
        this.batch = new SpriteBatch();
        assetManager = game.getAssetManager();
        this.font = assetManager.get(AssetDescriptors.TILED_FONT);
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 800, 480);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);

        String title = "Select Mode";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        float titleX = (800 - titleLayout.width) / 2;
        float titleY = 400;
        font.draw(batch, titleLayout, titleX, titleY);

        String singlePlayerText = "1. Single Player";
        String twoPlayerText = "2. Two Players";

        GlyphLayout singleLayout = new GlyphLayout(font, singlePlayerText);
        float singleX = (800 - singleLayout.width) / 2;
        float singleY = 300;

        GlyphLayout twoLayout = new GlyphLayout(font, twoPlayerText);
        float twoX = (800 - twoLayout.width) / 2;
        float twoY = 250;

        font.draw(batch, singleLayout, singleX, singleY);
        font.draw(batch, twoLayout, twoX, twoY);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            game.setScreen(new CharacterSelectionScreen(game, false));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            game.setScreen(new CharacterSelectionScreen(game, true));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
