package ch.cidzoo.journalibs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.location.Address;
import android.os.Handler;
import android.os.Message;
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
public class MealListAdapter extends BaseAdapter {

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
	 * Worker used to compute reverse geocoding given some lat and lon coordinates
	 */
	final static ExecutorService mReverseGeocodingWorker = Executors.newSingleThreadExecutor();
	
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
	public MealListAdapter(Context context) {
		this.context = context;
	}
	
	/**
	 * MealAdapter alternate constructor
	 * @param context
	 * @param meals
	 */
	public MealListAdapter(Context context, List<Meal> meals) {
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

	/**
	 * Group the view and the associated string.
	 * Used with messages to update the UI. 
	 * @author romain
	 *
	 */
	class TextViewStringPair {
		
		public TextView view;
		public String string;
		
		public TextViewStringPair(TextView v, String buf) {
			view = v;
			string = buf;
		}
	}
	
	/**
	 * Handler to update the UI of a meal row with the computed location
	 */
	final private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			((TextViewStringPair)msg.obj).view.setText(
					((TextViewStringPair)msg.obj).string);
		}
	};
		
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rootView = LayoutInflater.from(context)
				.inflate(android.R.layout.simple_list_item_activated_2, parent, false);

		TextView dateView = (TextView) rootView.findViewById(android.R.id.text1);
		final TextView locationView = (TextView) rootView.findViewById(android.R.id.text2);

		// get the current meal
		final Meal meal = (Meal) getItem(position);
		
		// show the date
		dateView.setText(
				Toolbox.date2String(meal.getDate()) + 
				" - " + 
				Toolbox.time2String(meal.getDate()) );

		// show the location 
		// TODO: use a proper queue to add location to reverseGeocode. Or add a computed string value in DB
		mReverseGeocodingWorker.submit(new Runnable() {
			@Override
			public void run() {
				
				// get the textual location based on lat and lon
				String buf;
				try {
					Address adr = Toolbox.reverseGeocoding(context, meal.getLatitude(), meal.getLongitude());
					buf = adr.getLocality() + ", " + adr.getAddressLine(0);
				} catch (Exception e) {
					buf = "Emplacement inconnu";
				}
				
				// update UI
				Message msg = mHandler.obtainMessage();
				msg.obj = new TextViewStringPair(locationView, buf);
				mHandler.sendMessage(msg);
			}
		});

		return rootView;
	}

}


