package assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.video.VideoPlayer;

public class AssetDescriptors {
    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
        new AssetDescriptor<TextureAtlas>(AssetPaths.GAMEPLAY, TextureAtlas.class);
    public static final AssetDescriptor<BitmapFont> FONT =
        new AssetDescriptor<BitmapFont>(AssetPaths.FONT, BitmapFont.class);

    public static final AssetDescriptor<BitmapFont> TILED_FONT =
        new AssetDescriptor<BitmapFont>(AssetPaths.TILED_FONT, BitmapFont.class);

    public static final AssetDescriptor<TextureAtlas> TILED_ATLAS =
        new AssetDescriptor<TextureAtlas>(AssetPaths.TILED_ATLAS, TextureAtlas.class);

    private AssetDescriptors() {
    }
}
