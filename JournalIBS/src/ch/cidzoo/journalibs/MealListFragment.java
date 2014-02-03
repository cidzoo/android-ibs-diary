package ch.cidzoo.journalibs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.DaoSession;
import ch.cidzoo.journalibs.db.IngrDao;
import ch.cidzoo.journalibs.db.Meal;
import ch.cidzoo.journalibs.db.MealDao;
import ch.cidzoo.journalibs.db.MealIngr;
import ch.cidzoo.journalibs.db.MealIngrDao;

/**
 * A list fragment representing a list of Meals. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link MealDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MealListFragment extends ListFragment {

	/**
	 * Database stuff - generated by greenDAO
	 */
	private DaoSession mDaoSession;
	private MealDao mMealDao;
	private MealIngrDao mMealIngrDao;
	private IngrDao mIngrDao;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sOwnCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sOwnCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MealListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDaoSession = Toolbox.getDatabaseSession(getActivity());
		mMealDao = mDaoSession.getMealDao();
		mMealIngrDao = mDaoSession.getMealIngrDao();
		mIngrDao = mDaoSession.getIngrDao();
		
		MealListAdapter adapter = new MealListAdapter(this.getActivity(), mMealDao.loadAll());
		setListAdapter(adapter);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {		
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.i("onCreateContextMenu", "called");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
		
		((MealListAdapter) getListAdapter()).updateMeals(mMealDao.loadAll());
		registerForContextMenu(getView());
		
		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MealSelectListener());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
			case R.id.action_reset_meals:
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            // let's delete all meals in the db
				        	mMealDao.deleteAll();
				        	mMealIngrDao.deleteAll();
				        	((MealListAdapter) getListAdapter()).updateMeals(mMealDao.loadAll());
				            break;
				        }
				    }
				};
				AlertDialog.Builder ab = new AlertDialog.Builder(this.getActivity());
			    ab.setMessage(getString(R.string.action_reset_meals_desc))
			    	.setPositiveButton(getString(R.string.action_reset_meals_yes), dialogClickListener)
			        .setNegativeButton(getString(R.string.action_reset_meals_no), dialogClickListener).show();
				break;
				
			case R.id.action_reset_all:
				dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            // let's delete all meals in the db
				        	mMealDao.deleteAll();
				        	mMealIngrDao.deleteAll();
				        	mIngrDao.deleteAll();
				        	((MealListAdapter) getListAdapter()).updateMeals(mMealDao.loadAll());
				            break;
				        }
				    }
				};
				ab = new AlertDialog.Builder(this.getActivity());
			    ab.setMessage(getString(R.string.action_reset_all_desc))
			    	.setPositiveButton(getString(R.string.action_reset_all_yes), dialogClickListener)
			        .setNegativeButton(getString(R.string.action_reset_all_no), dialogClickListener).show();
				break;
			
			case R.id.action_backup:
				Toolbox.exportDB(getActivity(), mDaoSession.getDatabase());
				break;
				
			case R.id.action_restore:
				Toolbox.importDB(getActivity(), mDaoSession.getDatabase());
				((MealListAdapter) getListAdapter()).updateMeals(mMealDao.loadAll());
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		((MealListAdapter) getListAdapter()).updateMeals(mMealDao.loadAll());
		super.onResume();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sOwnCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected( ((Meal) getListAdapter().getItem(position)).getId().toString() );
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick
				? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}  
	
	class MealSelectListener implements MultiChoiceModeListener {

		Queue<Integer> selectedItems = new LinkedList<Integer>();
	    @Override
	    public void onItemCheckedStateChanged(ActionMode mode, int position,
	                                          long id, boolean checked) {
	        
	    	Log.i("onItemCheckedStateChanged", "item " + position + " is selected ? " + checked);
	    	if (checked) {
	    		selectedItems.add(position);
	    	} else {
	    		selectedItems.remove(position);
	    	}
            
	    	mode.setTitle(String.valueOf(selectedItems.size())+" "+getString(R.string.action_context_sel));
	    	
            /* hide or show edit action if more then one item is selected */
            if(selectedItems.size() > 1)
                mode.getMenu().findItem(R.id.action_edit).setVisible(false);
            else
                mode.getMenu().findItem(R.id.action_edit).setVisible(true);
	    }

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        // Respond to clicks on the actions in the CAB
	        switch (item.getItemId()) {
	            case R.id.action_delete:
	                Log.i("onActionItemClicked", "item(s) deleted");
	                Integer index;
	                while ((index = selectedItems.poll()) != null) {
	                	Meal m = (Meal) getListAdapter().getItem(index);
	                	
	                	// delete all rows in joint table that concerns this meal
	                	List<MealIngr> ingrs = m.getMealToIngrs();
	                	Iterator<MealIngr> i = ingrs.iterator();
	                	while (i.hasNext()) {
	                		MealIngr ingr = i.next();
	                		mMealIngrDao.delete(ingr);
	                	}
	                	
	                	// delete the meal itself
	                	mMealDao.delete(m);
	                }
	                	
	                ((MealListAdapter) getListAdapter()).updateMeals(mMealDao.loadAll());
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate the menu for the CAB
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_menu_edit, menu);
	        return true;
	    }

	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        // Here you can make any necessary updates to the activity when
	        // the CAB is removed. By default, selected items are deselected/unchecked.
	    	selectedItems.clear();
	    }

	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        // Here you can perform updates to the CAB due to
	        // an invalidate() request
	        return false;
	    }
	}
	
}
