package si.um.feri.leaf.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import assets.AssetDescriptors;
import assets.RegionNames;
import si.um.feri.leaf.LeafLink;
import si.um.feri.leaf.utils.Constants;

public class IntroScreen extends ScreenAdapter {
    public static final float INTRO_DURATION_IN_SEC = 4.6f;

    private final LeafLink game;
    private final AssetManager assetManager;

    private Viewport viewport;
    private TextureAtlas gameplayAtlas;

    private float duration = 0f;

    private Stage stage;

    public IntroScreen(LeafLink game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    @Override
    public void show() {
        viewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());

        // Load assets asynchronously
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.finishLoading();

        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        if (gameplayAtlas != null) {
            stage.addActor(createTreeWithShake());
        } else {
            Gdx.app.error("IntroScreen", "Failed to load assets.");
        }
    }

    private Actor createTreeWithShake() {
        // Load tree image
        Image tree = new Image(gameplayAtlas.findRegion(RegionNames.TREE));

        // Set size and position to center
        tree.setSize(260, 300); // Adjust size as needed
        tree.setPosition(
            viewport.getWorldWidth() / 2f - tree.getWidth() / 2f,
            viewport.getWorldHeight() / 2f - tree.getHeight() / 2f
        );
        tree.setOrigin(Align.center);

        tree.addAction(
            Actions.sequence(
                Actions.moveBy(5f, 0f, 0.3f),   // Shake right
                Actions.moveBy(-10f, 0f, 0.4f), // Shake left
                Actions.moveBy(5f, 0f, 0.2f),   // Shake right
                Actions.delay(0.5f),            // Delay before leaves fall
                Actions.run(() -> {
                    stage.addActor(createFallingLeavesAnimation()); // Start falling leaves after shake
                })
            )
        );

        return tree;
    }


    private Actor createFallingLeavesAnimation() {
        String[] leafRegions = {
            RegionNames.LEAF_RED, RegionNames.LEAF_GREEN
        };

        int leafCount = 20;
        for (int i = 0; i < leafCount; i++) {
            String regionName = leafRegions[(int) (Math.random() * leafRegions.length)];
            Image leaf = new Image(gameplayAtlas.findRegion(regionName));

            // Randomize initial position and size
            float startX = (float) (Math.random() * viewport.getWorldWidth());
            float startY = viewport.getWorldHeight() + 50 + (i * 20);
            float size = 40 + (float) (Math.random() * 40);

            leaf.setSize(size, size);
            leaf.setPosition(startX, startY);
            leaf.setOrigin(Align.center);

            leaf.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.moveTo(
                            (float) (Math.random() * viewport.getWorldWidth()),
                            -50,
                            (float) (2f + Math.random() * 1f)
                        ),
                        Actions.rotateBy(360, 2f)
                    ),
                    Actions.removeActor()
                )
            );

            // Add leaf to the stage
            stage.addActor(leaf);
        }

        return new Actor();
    }





    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(172 / 255f, 225 / 255f, 175 / 255f, 0f);

        duration += delta;

        if (duration > INTRO_DURATION_IN_SEC) {
            game.setScreen(new MapScreen(game));
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }


}
