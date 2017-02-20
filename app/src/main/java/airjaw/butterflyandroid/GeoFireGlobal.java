package airjaw.butterflyandroid;

import android.app.Application;
import android.location.Location;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DatabaseError;

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


    public static void getUserLocation(){
        Constants.geoFireUsers.getLocation(Constants.userID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                    GeoFireGlobal.getInstance().setLastLocation(location);
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }
}
