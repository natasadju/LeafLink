package si.um.feri.leaf.utils;

import static si.um.feri.leaf.utils.MapRasterTiles.fetchTile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainT {

    static String mapServiceUrl = "https://maps.geoapify.com/v1/tile/";
    static String token = "?&apiKey=" + Keys.GEOAPIFY;
    static String tilesetId = "positron";
    static String format = "@2x.png";
    static String assetsPath = "assets/mapTiles/";


    public static void main(String[] args) throws IOException {
        Geolocation centerGeolocation = new Geolocation(46.557314, 15.637771);
        int zoom = Constants.ZOOM;
        int numTiles = Constants.NUM_TILES; // Adjust this value based on your requirements

        downloadRequiredTiles(centerGeolocation, zoom, numTiles);
    }

    private static void downloadRequiredTiles(Geolocation centerGeolocation, int zoom, int numTiles) throws IOException {
        ZoomXY centerTile = MapRasterTiles.getTileNumber(centerGeolocation.lat, centerGeolocation.lng, zoom);
        for (int i = 0; i < numTiles; i++) {
            for (int j = 0; j < numTiles; j++) {
                int tileX = centerTile.x - ((numTiles - 1) / 2) + i;
                int tileY = centerTile.y - ((numTiles - 1) / 2) + j;
                String fileName = assetsPath + "tile_" + zoom + "_" + tileX + "_" + tileY + ".png";
                if (!isFileExists(fileName)) {
                    URL url = new URL(mapServiceUrl + tilesetId + "/" + zoom + "/" + tileX + "/" + tileY + format + token);
                    ByteArrayOutputStream bis = fetchTile(url);
                    writeBytesToFile(fileName, bis.toByteArray());
                } else {
                    System.out.println("File " + fileName + " already exists. Skipping download.");
                }
            }
        }
    }

    public static void downloadTile(Geolocation centerGeolocation, int zoom, int numTiles, int i, int j) throws IOException {
        ZoomXY centerTile = MapRasterTiles.getTileNumber(centerGeolocation.lat, centerGeolocation.lng, zoom);
        int tileX = centerTile.x - ((numTiles - 1) / 2) + i;
        int tileY = centerTile.y - ((numTiles - 1) / 2) + j;
        String fileName = assetsPath + "tile_" + zoom + "_" + tileX + "_" + tileY + ".png";
        if (!isFileExists(fileName)) {
            URL url = new URL(mapServiceUrl + tilesetId + "/" + zoom + "/" + tileX + "/" + tileY + format + token);
            System.out.println("Downloading tile: " + fileName);
            ByteArrayOutputStream bis = fetchTile(url);
            writeBytesToFile(fileName, bis.toByteArray());
        } else {
            System.out.println("File " + fileName + " already exists. Skipping download.");
        }
    }

    public static boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    private static void writeBytesToFile(String fileOutput, byte[] bytes) throws IOException {
        File file = new File(fileOutput);
        file.getParentFile().mkdirs(); // Create directories if they do not exist
        try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
            fos.write(bytes);
        }
    }

    public static ByteArrayOutputStream fetchTile(URL url) throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        byte[] bytebuff = new byte[4096];
        int n;

        while ((n = is.read(bytebuff)) > 0) {
            bis.write(bytebuff, 0, n);
        }
        return bis;
    }
}
