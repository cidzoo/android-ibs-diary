package ch.cidzoo.journalibs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.Meal;

/**
 * Designed following this proposed best practices for Adapters :
 * http://www.piwai.info/android-adapter-good-practices/
 * 
 * TODO: extends CursorAdapter (FIXME: header not update correctly when meal edited)
 * 
 * @author Romain Maffina
 *
 */
public class MealAdapter extends BaseAdapter {

	/**
	 * Comparator for meal, used to order them
	 * @author romain
	 *
	 */
	public class MealComparator implements Comparator<Meal> {
		@Override
	    public int compare(Meal o1, Meal o2) {
	        return o2.getDate().compareTo(o1.getDate());
		}
	}
	
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
		Toolbox.checkOnMainThread();
		this.meals = meals;
		Collections.sort(meals, new MealComparator());
		notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return meals.size();
	}

	@Override
	public Object getItem(int position) {
		return meals.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rootView = LayoutInflater.from(context)
				.inflate(android.R.layout.simple_list_item_activated_2, parent, false);

		TextView dateView = (TextView) rootView.findViewById(android.R.id.text1);
		TextView locationView = (TextView) rootView.findViewById(android.R.id.text2);

		// get the current meal
		Meal meal = (Meal) getItem(position);
		
		// show the date
		dateView.setText(rootView.getResources().getString(R.string.meal_prefix) +
				" " +
				Toolbox.date2String(meal.getDate()) + 
				" - " + 
				Toolbox.time2String(meal.getDate()));
		
		// show the location FIXME: it is slowing the display, use a workqueue?
		String buf;
		try {
			Address adr = Toolbox.reverseGeocoding(context, meal.getLatitude(), meal.getLongitude());
			buf = adr.getLocality() + ", " + adr.getAddressLine(0);
		} catch (Exception e) {
			buf = "Emplacement inconnu";
		}
		locationView.setText(buf);

		return rootView;
	}

}


