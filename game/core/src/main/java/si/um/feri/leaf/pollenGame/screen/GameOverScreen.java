package si.um.feri.leaf.pollenGame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import si.um.feri.leaf.LeafLink;
import assets.AssetDescriptors;
import si.um.feri.leaf.screen.MapScreen;

public class GameOverScreen extends ScreenAdapter {
    private final LeafLink game;
    private final AssetManager assetManager;
    private final boolean isTwoPlayerMode;
    private final int player1Score;
    private final int player2Score;
    private final boolean player1Dead;
    private final boolean player2Dead;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final BitmapFont font;

    public GameOverScreen(LeafLink game, boolean isTwoPlayerMode, int player1Score, int player2Score,
                          boolean player1Dead, boolean player2Dead) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.isTwoPlayerMode = isTwoPlayerMode;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.player1Dead = player1Dead;
        this.player2Dead = player2Dead;

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(800, 480, camera);
        this.font = assetManager.get(AssetDescriptors.TILED_FONT);

        camera.position.set(400, 240, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.getData().setScale(1.5f);
        font.setColor(Color.RED);
        String titleMessage = "GAME OVER";
        GlyphLayout titleLayout = new GlyphLayout(font, titleMessage);
        float titleX = (viewport.getWorldWidth() - titleLayout.width) / 2f;
        float titleY = (viewport.getWorldHeight() / 2f) + 120;
        font.draw(batch, titleLayout, titleX, titleY);

        font.getData().setScale(0.9f);

        String resultMessage;
        if (!isTwoPlayerMode) {
            if (player1Dead) {
                resultMessage = "You Lost!";
            } else {
                resultMessage = "Level Complete!\nScore: " + player1Score;
            }
        } else {
            if (player1Dead && !player2Dead) {
                resultMessage = "PLAYER 2 WINS!\nPlayer 1 is Dead!";
            } else if (player2Dead && !player1Dead) {
                resultMessage = "PLAYER 1 WINS!\nPlayer 2 is Dead!";
            } else if (player1Score > player2Score) {
                resultMessage = "PLAYER 1 WINS!\nScore: " + player1Score;
            } else if (player2Score > player1Score) {
                resultMessage = "PLAYER 2 WINS!\nScore: " + player2Score;
            } else {
                resultMessage = "IT'S A TIE!\nScore: " + player1Score;
            }
        }

        font.setColor(Color.WHITE);
        GlyphLayout resultLayout = new GlyphLayout(font, resultMessage);
        float resultX = (viewport.getWorldWidth() - resultLayout.width) / 2f;
        float resultY = titleY - 80;
        font.draw(batch, resultLayout, resultX, resultY);

        font.getData().setScale(0.3f);
        font.setColor(Color.YELLOW);
        String optionsMessage = "Press [R] to Restart | Press [M] to Return to Map";
        GlyphLayout optionsLayout = new GlyphLayout(font, optionsMessage);
        float optionsX = (viewport.getWorldWidth() - optionsLayout.width) / 2f;
        float optionsY = resultY - 80;
        font.draw(batch, optionsLayout, optionsX, optionsY);

        batch.end();

        font.getData().setScale(1f);

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            returnToMap();
        }
    }

    private void restartGame() {
        game.setScreen(new ModeSelectionScreen(game));
        dispose();
    }

    private void returnToMap() {
        game.setScreen(new MapScreen(game));
        dispose();
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
