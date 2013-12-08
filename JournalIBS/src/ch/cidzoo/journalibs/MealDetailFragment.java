package ch.cidzoo.journalibs;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.DaoSession;
import ch.cidzoo.journalibs.db.Meal;
import ch.cidzoo.journalibs.db.MealDao;

/**
 * A fragment representing a single Meal detail screen.
 * This fragment is either contained in a {@link MealListActivity}
 * in two-pane mode (on tablets) or a {@link MealDetailActivity}
 * on handsets.
 */
public class MealDetailFragment extends Fragment implements OnClickListener{
 
	/**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

	/**
	 * Get reference on the system Location manager
	 */
	LocationManager locationManager;
	
	/**
	 * Access meals
	 */
	private MealDao mealDao;
	
    /**
     * The meal content this fragment is presenting.
     */
    private String mItem;
    
    private Meal mMeal;

	private Button mDatePicker, mTimePicker, mLocationPicker;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MealDetailFragment() {
    }
    
    private static final String[] COUNTRIES = new String[] {
        "Belgium", "France", "Italy", "Germany", "Spain"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // database stuff
        DaoSession daoSession = Toolbox.getDatabaseSession(getActivity());
        mealDao = daoSession.getMealDao();
        
        // init object with default values
        mMeal = new Meal(null, new Date(), null, null);
        
        
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getArguments().getString(ARG_ITEM_ID);
        }
        
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_meal_detail, container, false);
 
        mDatePicker = (Button) rootView.findViewById(R.id.datePicker);
        mDatePicker.setOnClickListener(this);
        mDatePicker.setText(Toolbox.date2String(mMeal.getDate()));
        mTimePicker = (Button) rootView.findViewById(R.id.timePicker);
        mTimePicker.setOnClickListener(this);
        mTimePicker.setText(Toolbox.time2String(mMeal.getDate()));
        mLocationPicker = (Button) rootView.findViewById(R.id.locationPicker);
        mLocationPicker.setEnabled(false);
        mLocationPicker.setText(getString(R.string.location_pending));
        
        // Get the location manager
     	locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new MyLocationListener(), null);
        		
        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            //((TextView) rootView.findViewById(R.id.meal_detail)).setText(mItem);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                rootView.findViewById(R.id.searchIngredient);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                    long id) {
                  Toast.makeText(getActivity()," selected " + ((TextView) arg1).getText().toString(), Toast.LENGTH_LONG).show();
                  ((TextView) arg1).setTextIsSelectable(true);
                  ((TextView) arg1).setText("");
                  ((TextView) arg1).clearFocus();
            }
            
            
        });
        return rootView;
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onClick(View v){
		Log.i(getTag(), "Button #" + v.getId() + " pressed");
		switch (v.getId()){
		case R.id.datePicker:
			DialogFragment newDateFragment = new DatePickerFragment();
			newDateFragment.show(getFragmentManager(), "datePicker");
			break;
		case R.id.timePicker:
			DialogFragment newTimeFragment = new TimePickerFragment();
			newTimeFragment.show(getFragmentManager(), "timePicker");
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.action_done:
			Log.i("onOptionsItemSelected", "button done clicked");

			// insert the meal into the DB
			mealDao.insert(mMeal);
			
			// finish and return
			getActivity().finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	/**
	 * Date picker inner class
	 * @author romain
	 *
	 */
    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                    // Use the current date as the default date in the picker
                    final Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);
                    
                    // Create a new instance of DatePickerDialog and return it
                    return new DatePickerDialog(getActivity(), this, year, month, day);
            }
            
            @SuppressWarnings("deprecation")
			public void onDateSet(DatePicker view, int year, int month, int day) {
                Date date = mMeal.getDate();
                date.setYear(year);
                date.setMonth(month);
                date.setDate(day);
                
            	mMeal.setDate(date); //TODO: really usefull?
            	mDatePicker.setText(Toolbox.date2String(date));
            }
    }
    
    /**
     * Time picker inner class
     * @author romain
     *
     */
    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
            
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                    // Use the current time as the default values for the picker
                    final Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);
                    
                    // Create a new instance of TimePickerDialog and return it
                    return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
            }
            
            @SuppressWarnings("deprecation")
			public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
            	Date date = mMeal.getDate();
                date.setHours(hourOfDay);
                date.setMinutes(minutes);
                
            	mMeal.setDate(date); //TODO: really usefull?
            	mTimePicker.setText(Toolbox.time2String(date));
            }
    }
    
    /**
     * LocationListener inner class
     * @author romain
     *
     */
    public class MyLocationListener implements LocationListener{
    	//
    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		// TODO Auto-generated method stub

    	}

    	@Override
    	public void onProviderEnabled(String provider) {
    		// TODO Auto-generated method stub

    	}

    	@Override
    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub

    	}

    	@Override
    	public void onLocationChanged(Location location) {
    		double lat = location.getLatitude();
    		double lon = location.getLongitude();

    		Log.i("onLocationChanged", "got a location update: lat=" + lat + " / lon=" + lon);

    		mMeal.setLatitude(lat);
    		mMeal.setLongitude(lon);
    		
    		mLocationPicker.setText(getString(R.string.location_prefix) + " " + 
    				Toolbox.reverseGeocoding(getActivity(), 
    				mMeal.getLatitude(), 
    				mMeal.getLongitude()).getAddressLine(0));

//    		Meal meal = new Meal(null, new Date(), loc.getId());
//    		mealDao.insert(meal);
//    		Log.d("onLocationChanged", "Inserted new meal, ID: " + meal.getId());
//    		((MealAdapter)getListAdapter()).updateMeals(mealDao.loadAll());
    	}
    }
    
}
