package airjaw.butterflyandroid;

import android.app.Application;
import android.location.Location;

import com.firebase.geofire.GeoLocation;

/**
 * Created by airjaw on 2/11/17.
 */

public class GeoFireGlobal {
    private GeoLocation lastLocation;

    public GeoLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(GeoLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    private static final GeoFireGlobal geoGlobal = new GeoFireGlobal();
    public static GeoFireGlobal getInstance() { return geoGlobal;}
}
