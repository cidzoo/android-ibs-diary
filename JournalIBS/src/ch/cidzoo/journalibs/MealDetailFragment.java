package ch.cidzoo.journalibs;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.DaoSession;
import ch.cidzoo.journalibs.db.Ingr;
import ch.cidzoo.journalibs.db.IngrDao;
import ch.cidzoo.journalibs.db.Meal;
import ch.cidzoo.journalibs.db.MealDao;
import ch.cidzoo.journalibs.db.MealIngr;
import ch.cidzoo.journalibs.db.MealIngrDao;

/**
 * A fragment representing a single Meal detail screen.
 * This fragment is either contained in a {@link MealListActivity}
 * in two-pane mode (on tablets) or a {@link MealDetailActivity}
 * on handsets.
 * 
 * FIXME: as the meal is created and inserted immediately into the db (greendao limitation),
 * if the user press the back button instead of the "done" button the meal appears into the 
 * global list.
 */
public class MealDetailFragment extends Fragment implements OnClickListener, OnCheckedChangeListener {
 
	/**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

	/**
	 * Get reference on the system Location manager
	 */
	LocationManager mLocManager;
	
	/**
	 * Access meals
	 */
	private MealDao mMealDao;
	
	/**
	 * Access ingredients
	 */
	private IngrDao mIngrDao;
	
	/**
	 * Access join entity
	 */
	private MealIngrDao mMealIngrDao;
	
	/**
	 * Ingredient adapter for the autoCompleteTextView
	 */
	private IngrSearchAdapter mIngrSearchAdapter;
	
	/**
	 * Ingredient adapter for list or meal's ingredients
	 */
	private IngrListAdapter mIngrListAdapter;
	
    /**
     * The meal content this fragment is presenting.
     */
    private Meal mMeal;

	private AutoCompleteTextView mIngrSearch;
	private ListView mIngrList;
	private Switch mSymptomNausea, mSymptomColic, mSymptomDiarrhea;
	private Button mDatePicker, mTimePicker, mLocationPicker;
	
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MealDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // database stuff
        DaoSession daoSession = Toolbox.getDatabaseSession(getActivity());
        mMealDao = daoSession.getMealDao();
        mIngrDao = daoSession.getIngrDao();
        mMealIngrDao = daoSession.getMealIngrDao();
        
        // TODO: handle edit a meal
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            //mItem = getArguments().getString(ARG_ITEM_ID);
        	long selectedMealId = (long) Integer.parseInt(getArguments().getString(ARG_ITEM_ID));

        	if (selectedMealId != 0)
        		mMeal = mMealDao.load(selectedMealId);
        	else { // init object with default values
        		mMeal = new Meal(null, new Date(), null, null, false, false, false); 
        		mMealDao.insert(mMeal);
        	}
        }
        
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
    	// get root view
    	View rootView = inflater.inflate(R.layout.fragment_meal_detail, container, false);
 
    	// ingredient search and add
        mIngrSearch = (AutoCompleteTextView) rootView.findViewById(R.id.searchIngredient);
        mIngrList = (ListView) rootView.findViewById(R.id.listIngredients);
        
        // symptoms
        mSymptomNausea = (Switch) rootView.findViewById(R.id.switchNausea);
        mSymptomNausea.setChecked(mMeal.getNausea());
        mSymptomNausea.setOnCheckedChangeListener(this);
        mSymptomColic = (Switch) rootView.findViewById(R.id.switchColic);
        mSymptomColic.setChecked(mMeal.getColic());
        mSymptomColic.setOnCheckedChangeListener(this);
        mSymptomDiarrhea = (Switch) rootView.findViewById(R.id.switchDiarrhea);
        mSymptomDiarrhea.setChecked(mMeal.getDiarrhea());
        mSymptomDiarrhea.setOnCheckedChangeListener(this);
        
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
        try {
        	mLocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        	mLocManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new MyLocationListener(), null);
        } catch (Exception e) {

        }
        		
        // prepare auto complete ingredient dropdown list (appears while typing)
        mIngrSearchAdapter = new IngrSearchAdapter(getActivity());
        mIngrSearch.setAdapter(mIngrSearchAdapter);
        mIngrSearch.setOnItemClickListener(new IngredientSelectListener());
        mIngrSearch.setOnKeyListener(new IngredientEnterKeyListener());
        
        // prepare meal's ingredients list
        mIngrListAdapter = new IngrListAdapter(getActivity(), mMeal);
        mIngrList.setAdapter(mIngrListAdapter);
        
        return rootView;
    }
    
    public void addIngrToMeal(TextView v, Ingr ingr, boolean create) {
    	StringBuilder text = new StringBuilder(v.getText() + " ");
    	
    	if (create) {
    		text.append(v.getResources().getString(R.string.ingredient_created_postfix));
    	} else {
    		text.append(v.getResources().getString(R.string.ingredient_added_postfix));
    	}
    	
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
		
    	((TextView) v).setText("");
    	
    	mMealIngrDao.insert(new MealIngr(null, mMeal.getId(), ingr.getId()));
    	
    	// notify that data changed to force refresh
    	mIngrSearchAdapter.notifyDataSetChanged();
    	mIngrListAdapter.notifyDataSetChanged();
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
			DialogFragment newDateFragment = new DatePickerFragment(mMeal.getDate());
			newDateFragment.show(getFragmentManager(), "datePicker");
			break;
		case R.id.timePicker:
			DialogFragment newTimeFragment = new TimePickerFragment(mMeal.getDate());
			newTimeFragment.show(getFragmentManager(), "timePicker");
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		Log.i(getTag(), "Switch #" + v.getId() + " changed");
		switch (v.getId()){
		case R.id.switchNausea:
			mMeal.setNausea(isChecked);
			break;
		case R.id.switchColic:
			mMeal.setColic(isChecked);
			break;
		case R.id.switchDiarrhea:
			mMeal.setDiarrhea(isChecked);
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.action_done:
			Log.i("onOptionsItemSelected", "button done clicked");
			
			// cannot save with no ingredients
			if (mMeal.getMealToIngrs().isEmpty()) {
				Toast.makeText(getActivity(), getString(R.string.action_done_empty), Toast.LENGTH_LONG).show();
				return true;
			}
			
			// insert the meal into the DB
			mMealDao.insertOrReplace(mMeal);
			
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
	class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		
		final Calendar c;

		public DatePickerFragment() {
			super();
			c = Calendar.getInstance();
		}
		
		public DatePickerFragment(Date date) {
			this();
			c.setTime(date);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
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
	class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		final Calendar c;
		
		public TimePickerFragment() {
			super();
			c = Calendar.getInstance();
		}
		
		public TimePickerFragment(Date date) {
			this();
			c.setTime(date);
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
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
    class MyLocationListener implements LocationListener {

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
    	}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
    
    /**
     * Listener for adding an ingredient from {@link AutoCompleteTextView}
     * @author romain
     *
     */
    class IngredientSelectListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView <? > parent, View view, int position, long id) {
			Ingr ingr = mIngrDao.load(
					((SQLiteCursor) parent.getItemAtPosition(position))
					.getLong(IngrDao.Properties.Id.ordinal));
			addIngrToMeal((TextView) view, ingr, false);
		}
    	
    }

    /**
     * Listener for creating and adding a {@link Ingredient} from {@link AutoCompleteTextView}
     * @author romain
     *
     */
    class IngredientEnterKeyListener implements OnKeyListener {

    	@Override
    	public boolean onKey(View v, int keyCode, KeyEvent event) {
    		Log.i(getTag(),"onKey");
    		if (event.getAction() == KeyEvent.ACTION_DOWN)
    		{
    			switch (keyCode)
    			{
    			case KeyEvent.KEYCODE_DPAD_CENTER:
    			case KeyEvent.KEYCODE_ENTER:
    				// insert new ingredient
    				Ingr ingr = new Ingr(null, ((TextView) v).getText().toString());
    				mIngrDao.insert(ingr);
    				addIngrToMeal((TextView) v, ingr, true);
    				return true;
    			
    			default:
    				break;
    			}
    		}

    		return false;
    	}
    }
    
}
