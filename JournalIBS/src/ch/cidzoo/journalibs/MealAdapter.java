package ch.cidzoo.journalibs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.LocationCoords;
import ch.cidzoo.journalibs.db.Meal;

/**
 * Designed following this proposed best practices for Adapters :
 * http://www.piwai.info/android-adapter-good-practices/
 * 
 * @author Romain Maffina
 *
 */
public class MealAdapter extends BaseAdapter {

	/**
	 * The list of meals
	 */
	private List<Meal> meals = new ArrayList<Meal>();
	
	/**
	 * context
	 */
	private final Context context;
	
	/**
	 * MealAdapter default constructor
	 * @param context
	 */
	public MealAdapter(Context context) {
		this.context = context;
	}
	
	/**
	 * MealAdapter alternate constructor
	 * @param context
	 * @param meals
	 */
	public MealAdapter(Context context, List<Meal> meals) {
		this(context);
		updateMeals(meals);
	}

	public void updateMeals(List<Meal> meals) {
		Log.i("updateMeals", "entry");
		Toolbox.checkOnMainThread();
		this.meals = meals;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		Log.i("getCount", "entry");
		return meals.size();
	}

	@Override
	public Object getItem(int position) {
		Log.i("getItem", "entry");
		return meals.get(position);
	}

	@Override
	public long getItemId(int position) {
		Log.i("getItemId", "entry");
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i("getView", "entry");
		View rootView = LayoutInflater.from(context)
				.inflate(android.R.layout.simple_list_item_2, parent, false);

		TextView dateView = (TextView) rootView.findViewById(android.R.id.text1);
		TextView locationView = (TextView) rootView.findViewById(android.R.id.text2);

		// get the current meal
		Meal meal = (Meal) getItem(position);
		
		// show the date
		dateView.setText(meal.getDate().toString());
		
		// show the location
		String buf;
		try {
			LocationCoords coord = meal.getLocationCoords();
			Address adr = Toolbox.reverseGeocoding(context, coord.getLatitude(), coord.getLongitude());
			buf = adr.getLocality() + ", " + adr.getAddressLine(0);
		} catch (Exception e) {
			buf = "Emplacement inconnu";
		}
		locationView.setText(buf);

		return rootView;
	}

}
