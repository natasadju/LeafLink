package si.um.feri.leaf.lwjgl3;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class AssetPacker {
    private static final boolean DRAW_DEBUG_OUTLINE = false;

    private static final String RAW_ASSETS_PATH = "lwjgl3/assets-raw";
    private static final String ASSETS_PATH = "assets";

    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.debug = DRAW_DEBUG_OUTLINE;

        /*TexturePacker.process(settings,
            RAW_ASSETS_PATH +"/images",
            ASSETS_PATH  + "/gameplay",
            "gameplay"
        );*/

        TexturePacker.process(settings,
            RAW_ASSETS_PATH,
            ASSETS_PATH + "/tiled",
            "tiled"
        );

    }
}
