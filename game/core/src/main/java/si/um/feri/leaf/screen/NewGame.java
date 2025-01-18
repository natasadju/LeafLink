package si.um.feri.leaf.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.FileNotFoundException;

import si.um.feri.leaf.Character;
import si.um.feri.leaf.LeafLink;
import si.um.feri.leaf.utils.AirMarker;
import si.um.feri.leaf.utils.Marker;

public class NewGame extends ScreenAdapter {

    LeafLink game;
    SpriteBatch spriteBatch;
    AssetManager assetManager;

    public NewGame(LeafLink game, AirMarker marker) throws FileNotFoundException {
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.assetManager = new AssetManager();
    }
}
