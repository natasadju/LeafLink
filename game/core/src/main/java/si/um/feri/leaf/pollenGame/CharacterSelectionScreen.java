package si.um.feri.leaf.pollenGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import si.um.feri.leaf.LeafLink;
import assets.AssetDescriptors;

public class CharacterSelectionScreen extends ScreenAdapter {
    private final LeafLink game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Array<String> characterNames;
    private final Array<TextureRegion> previewRegions;
    private int selectedIndex = 0;
    private AssetManager assetManager;

    //define tiled atlas
    private TextureAtlas tiledAtlas;

    public CharacterSelectionScreen(LeafLink game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.assetManager = game.getAssetManager();

        characterNames = new Array<>();
        characterNames.add("character1");
        characterNames.add("character2");
        characterNames.add("character3");
        characterNames.add("character4");

        tiledAtlas = assetManager.get(AssetDescriptors.TILED_ATLAS);

        previewRegions = new Array<>();
        for (String name : characterNames) {
            TextureRegion region = tiledAtlas.findRegion("characterIcons/" + name);
            if (region != null) {
                previewRegions.add(region);
            } else {
                Gdx.app.error("CharacterSelectionScreen", "Region not found for: characterIcons/" + name);
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedIndex = (selectedIndex - 1 + previewRegions.size) % previewRegions.size;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedIndex = (selectedIndex + 1) % previewRegions.size;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String selectedCharacter = characterNames.get(selectedIndex);
            game.setScreen(new PollenGameScreen(game, selectedCharacter));
        }

        batch.begin();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float iconWidth = 128;
        float iconHeight = 128;
        float gap = 20;
        float totalWidth = (iconWidth + gap) * previewRegions.size - gap;
        float startX = (screenWidth - totalWidth) / 2;
        float yPosition = screenHeight / 2 - iconHeight / 2;

        for (int i = 0; i < previewRegions.size; i++) {
            float xPosition = startX + i * (iconWidth + gap);

            if (i == selectedIndex) {
                batch.setColor(1, 1, 0, 1);
            } else {
                batch.setColor(1, 1, 1, 1);
            }

            batch.draw(previewRegions.get(i), xPosition, yPosition, iconWidth, iconHeight);
        }

        batch.setColor(1, 1, 1, 1);

        font.draw(batch, "Use LEFT/RIGHT to choose a character.", 20, 40);
        font.draw(batch, "Press ENTER to confirm.", 20, 20);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
