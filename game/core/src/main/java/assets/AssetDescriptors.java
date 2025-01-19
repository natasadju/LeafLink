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
    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<Skin>(AssetPaths.UI_SKIN, Skin.class);
    public static final AssetDescriptor<Sound> SOUND_CLICK =
        new AssetDescriptor<Sound>(AssetPaths.SOUND_CLICK, Sound.class);

    public static final AssetDescriptor<Sound> SOUND_GAMEOVER =
        new AssetDescriptor<Sound>(AssetPaths.SOUND_GAMEOVER, Sound.class);

    public static final AssetDescriptor<Sound> SOUND_BRID_COLLISION =
        new AssetDescriptor<Sound>(AssetPaths.SOUND_BRID_COLLISION, Sound.class);

    public static final AssetDescriptor<BitmapFont> TILED_FONT =
        new AssetDescriptor<BitmapFont>(AssetPaths.TILED_FONT, BitmapFont.class);

    public static final AssetDescriptor<TextureAtlas> TILED_ATLAS =
        new AssetDescriptor<TextureAtlas>(AssetPaths.TILED_ATLAS, TextureAtlas.class);

    public static final AssetDescriptor<Sound> FEMALE_COUGHING =
        new AssetDescriptor<Sound>(AssetPaths.FEMALE_COUGHING, Sound.class);

    public static final AssetDescriptor<Sound> MALE_COUGHING =
        new AssetDescriptor<Sound>(AssetPaths.MALE_COUGHING, Sound.class);

    public static final AssetDescriptor<Sound> CHOOSE_CHARACTER =
        new AssetDescriptor<Sound>(AssetPaths.CHOOSE_CHARACTER, Sound.class);

    public static final AssetDescriptor<Music> CHOOSE_SCREEN =
        new AssetDescriptor<Music>(AssetPaths.CHOOSE_SCREEN, Music.class);

    public static final AssetDescriptor<Music> MAIN_BACKGROUND_MUSIC =
        new AssetDescriptor<Music>(AssetPaths.MAIN_BACKGROUND_MUSIC, Music.class);

    private AssetDescriptors() {
    }
}
