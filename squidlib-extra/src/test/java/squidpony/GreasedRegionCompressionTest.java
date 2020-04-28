package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.GreasedRegion;

import java.util.Arrays;

/**
 * Expected output:
 * <br>
 * <pre>
 * Earth Map
 * Base size   : 18839
 * LZS size    : 3794
 * Custom size : 2376
 * Both size   : 1142
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * Australia Map
 * Base size   : 8517
 * LZS size    : 1622
 * Custom size : 806
 * Both size   : 434
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * </pre>
 * <br>
 * I'm quite surprised that re-compressing the GreasedRegions (shown on the "Both" rows) works so well.
 * <br>
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class GreasedRegionCompressionTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        WorldMapGenerator.MimicMap mimicMap = new WorldMapGenerator.MimicMap();
        GreasedRegion earth = mimicMap.earthOriginal.copy();
        String baseString = earth.serializeToString();
        String lz = LZSEncoding.compressToUTF16(baseString);
        String me = earth.toCompressedString();
        String both = LZSEncoding.compressToUTF16(me);
        System.out.println("Earth Map");
        System.out.println("Base size   : " + baseString.length());
        System.out.println("LZS size    : " + lz.length());
        System.out.println("Custom size : " + me.length());
        System.out.println("Both size   : " + both.length());
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(lz)).equals(earth)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(me).equals(earth)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(both)).equals(earth)));
        Gdx.files.local("Earth.txt").writeString(baseString, false, "UTF-16");
        Gdx.files.local("Earth_Comp.txt").writeString(me, false, "UTF-16");
        Gdx.files.local("Earth_LZS.txt").writeString(lz, false, "UTF-16");
        Gdx.files.local("Earth_Both.txt").writeString(both, false, "UTF-16");
        System.out.println("Reading in written Earth files...");
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(Gdx.files.local("Earth_LZS.txt").readString("UTF-16"))).equals(earth)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(Gdx.files.local("Earth_Comp.txt").readString("UTF-16")).equals(earth)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Earth_Both.txt").readString("UTF-16"))).equals(earth)));

        System.out.println();

        mimicMap.generate(123L);
        double min = 10000.0, max = -10000.0;
        for (int x = 0; x < mimicMap.width; x++) {
            for (int y = 0; y < mimicMap.height; y++) {
                min = Math.min(min, mimicMap.heatData[x][y]);
                max = Math.max(max, mimicMap.heatData[x][y]);
            }
        }
        System.out.println("Initial min heat: " + min);
        System.out.println("Initial max heat: " + max);
        String gcomp = GridCompression.compress(mimicMap.heatData);
        double[][] heatData = GridCompression.byteToDoubleGrid(GridCompression.decompress(gcomp));

        min = 10000.0;
        max = -10000.0;
        for (int x = 0; x < mimicMap.width; x++) {
            for (int y = 0; y < mimicMap.height; y++) {
                min = Math.min(min, heatData[x][y]);
                max = Math.max(max, heatData[x][y]);
            }
        }
        System.out.println("Post min heat: " + min);
        System.out.println("Post max heat: " + max);

        me = GridCompression.compress(heatData);
        baseString = Converters.convertArrayDouble2D.stringify(heatData);
        lz = LZSEncoding.compressToUTF16(baseString);
        both = LZSEncoding.compressToUTF16(me);
        System.out.println("Heat Map");
        System.out.println("Base size   : " + baseString.length());
        System.out.println("LZS size    : " + lz.length());
        System.out.println("Custom size : " + me.length());
        System.out.println("Both size   : " + both.length());
        System.out.println("LZ correct? " + (Arrays.deepEquals(Converters.convertArrayDouble2D.restore(LZSEncoding.decompressFromUTF16(lz)), heatData)));
        System.out.println("Custom correct? " + (Arrays.deepEquals(GridCompression.byteToDoubleGrid(GridCompression.decompress(me)), heatData)));
        System.out.println("Both correct? " + (Arrays.deepEquals(GridCompression.byteToDoubleGrid(GridCompression.decompress(LZSEncoding.decompressFromUTF16(both))), heatData)));
        Gdx.files.local("Heat.txt").writeString(baseString, false, "UTF-16");
        Gdx.files.local("Heat_Comp.txt").writeString(me, false, "UTF-16");
        Gdx.files.local("Heat_LZS.txt").writeString(lz, false, "UTF-16");
        Gdx.files.local("Heat_Both.txt").writeString(both, false, "UTF-16");
        System.out.println("Reading in written Heat files...");
        System.out.println("LZ correct? " + (Arrays.deepEquals(Converters.convertArrayDouble2D.restore(LZSEncoding.decompressFromUTF16((Gdx.files.local("Heat_LZS.txt").readString("UTF-16")))), heatData)));
        System.out.println("Custom correct? " + (Arrays.deepEquals(GridCompression.byteToDoubleGrid(GridCompression.decompress((Gdx.files.local("Heat_Comp.txt").readString("UTF-16")))), heatData)));
        System.out.println("Both correct? " + (Arrays.deepEquals(GridCompression.byteToDoubleGrid(GridCompression.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Heat_Both.txt").readString("UTF-16")))), heatData)));
        
        System.out.println();
        WorldMapGenerator.LocalMimicMap localMimicMap = new WorldMapGenerator.LocalMimicMap();
        GreasedRegion australia = localMimicMap.earthOriginal.copy();
        baseString = australia.serializeToString();
        lz = LZSEncoding.compressToUTF16(baseString);
        me = australia.toCompressedString();
        both = LZSEncoding.compressToUTF16(me);
        System.out.println("Australia Map");
        System.out.println("Base size   : " + baseString.length());
        System.out.println("LZS size    : " + lz.length());
        System.out.println("Custom size : " + me.length());
        System.out.println("Both size   : " + both.length());
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(lz)).equals(australia)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(me).equals(australia)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(both)).equals(australia)));
        Gdx.files.local("Australia.txt").writeString(baseString, false, "UTF-16");
        Gdx.files.local("Australia_Comp.txt").writeString(me, false, "UTF-16");
        Gdx.files.local("Australia_LZS.txt").writeString(lz, false, "UTF-16");
        Gdx.files.local("Australia_Both.txt").writeString(both, false, "UTF-16");
        System.out.println("Reading in written Australia files...");
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(Gdx.files.local("Australia_LZS.txt").readString("UTF-16"))).equals(australia)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(Gdx.files.local("Australia_Comp.txt").readString("UTF-16")).equals(australia)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Australia_Both.txt").readString("UTF-16"))).equals(australia)));

        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new GreasedRegionCompressionTest(), config);
    }
}
