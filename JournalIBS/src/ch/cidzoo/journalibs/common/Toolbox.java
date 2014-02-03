package ch.cidzoo.journalibs.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;
import ch.cidzoo.journalibs.BuildConfig;
import ch.cidzoo.journalibs.R;
import ch.cidzoo.journalibs.db.DaoMaster;
import ch.cidzoo.journalibs.db.DaoMaster.DevOpenHelper;
import ch.cidzoo.journalibs.db.DaoSession;

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

	/**
	 * Get a new database object to access the 'meals_diary' database. Uses greenDAO.
	 * @param context
	 * @return a new  SQLiteDatabase instance
	 */
	public static SQLiteDatabase getDatabase(Context context) {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, context.getString(R.string.db_name), null);
		return helper.getWritableDatabase();
	}
	
	/**
	 * Get a new session to access the 'meals_diary' database. Uses greenDAO.
	 * @param context needed context
	 * @return a new DaoSession instance for the corresponding database and context
	 */
	public static DaoSession getDatabaseSession(Context context) {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, context.getString(R.string.db_name), null);
		SQLiteDatabase db = helper.getWritableDatabase();
		//helper.onUpgrade(db, db.getVersion(), db.getVersion()+1); //FIXME: devel
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}
	
	public static void exportDB(Context context, SQLiteDatabase db) {
	    try {
	        File sd = Environment.getExternalStorageDirectory();
	        File data = Environment.getDataDirectory();

	        if (sd.canWrite()) {
	        	
	        	String currentDBPath = db.getPath();
                String backupDBPath = context.getExternalFilesDir(null).getAbsolutePath();
                File currentDB = new File(currentDBPath);
                File backupDB = new File(backupDBPath, currentDB.getName());

	            @SuppressWarnings("resource")
				FileChannel src = new FileInputStream(currentDB).getChannel();
	            @SuppressWarnings("resource")
				FileChannel dst = new FileOutputStream(backupDB).getChannel();
	            dst.transferFrom(src, 0, src.size());
	            src.close();
	            dst.close();
	            Toast.makeText(context.getApplicationContext(), "Backup Successful!",
	                    Toast.LENGTH_SHORT).show();

	        }
	    } catch (Exception e) {

	        Toast.makeText(context.getApplicationContext(), "Backup Failed!", Toast.LENGTH_SHORT)
	                .show();

	    }
	}
	
	public static void importDB(Context context, SQLiteDatabase db) {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();
			if (sd.canWrite()) {
	        	String currentDBPath = db.getPath();
                String backupDBPath = context.getExternalFilesDir(null).getAbsolutePath();
                File backupDB = new File(currentDBPath);
                File currentDB = new File(backupDBPath, backupDB.getName());

				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				Toast.makeText(context.getApplicationContext(), "Import Successful!",
						Toast.LENGTH_SHORT).show();

			}
		} catch (Exception e) {

			Toast.makeText(context.getApplicationContext(), "Import Failed!", Toast.LENGTH_SHORT)
			.show();

		}
	}
	
	public static String date2String(Date date) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return df.format(date);
	}
	
	public static String time2String(Date date) {
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		return df.format(date);
	}
	
}
