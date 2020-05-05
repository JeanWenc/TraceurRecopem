package jean.wencelius.traceurrecopem.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.overlay.FolderOverlay;

import java.io.File;
import java.io.IOException;

import jean.wencelius.traceurrecopem.R;

/**
 * Created by Jean Wencélius on 05/05/2020.
 */
public class BeaconOverlay {

    private KmlDocument mKmlDocument;
    private Style mStyle;

    public BeaconOverlay(String beaconType, Context ctx){
        KmlDocument kmlDocument = new KmlDocument();
        File thisBeacon =null;
        try {
            thisBeacon = MapTileProvider.getFileFromAssets(beaconType+".kml", ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(null != thisBeacon){
            kmlDocument.parseKMLFile(thisBeacon);
            this.setKmlDocument(kmlDocument);
        }

        Style style = new Style(pickBitmap(beaconType,ctx), 0x901010AA, 3.0f, 0x20AA1010);
        //Style style = new Style(pickBitmap(beaconType, ctx), 0x901010AA, 3.0f, 0x20AA1010);
        this.setStyle(style);
    }

    public Bitmap pickBitmap(String beaconType, Context ctx){
        Bitmap bitmap = null;
        switch (beaconType){
            case "port":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_port);
                break;
            case "starboard":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_starboard);
                break;
            case "west":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_west);
                break;
            case "east":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_east);
                break;
            case "north":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_north);
                break;
            case "south":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_south);
                break;
            case "other":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_other);
                break;
            case "nav":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_beacon_nav);
                break;
                    }
        return bitmap;
    }

    public Style getStyle() {
        return mStyle;
    }

    public void setStyle(Style style) {
        mStyle = style;
    }

    public KmlDocument getKmlDocument() {
        return mKmlDocument;
    }

    public void setKmlDocument(KmlDocument kmlDocument) {
        mKmlDocument = kmlDocument;
    }
}
