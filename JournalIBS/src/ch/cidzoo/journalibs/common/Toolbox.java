package ch.cidzoo.journalibs.common;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ch.cidzoo.journalibs.BuildConfig;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;

public class Toolbox {

	/**
	 * Get address from lat/long coordinates.
	 * @param context
	 * @param lat
	 * @param lon
	 * @return a new address object
	 */
	public static Address reverseGeocoding(Context context, double lat, double lon) {

		Geocoder gc = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = gc.getFromLocation(lat, lon, 1);
			return addresses.get(0);
		} catch (IOException e) {
			return new Address(null);
		}
	}
	
	/**
	 * Check that we are on the main thread. Can be used to avoid
	 * using a lock to made a call thread safety.
	 */
	public static void checkOnMainThread() {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("This method should be called from the Main Thread");
            }
        }
    }
	
}
