package ch.cidzoo.journalibs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.DaoSession;
import ch.cidzoo.journalibs.db.Ingr;
import ch.cidzoo.journalibs.db.IngrDao;
import ch.cidzoo.journalibs.db.Meal;
import ch.cidzoo.journalibs.db.MealIngrDao;

public class IngrListAdapter extends CursorAdapter {

	private Meal mMeal;
	private SQLiteDatabase mDb;
	private IngrDao mIngrDao;
	
	public IngrListAdapter(Context context, Meal meal) {
		super(context, null, 0);
		
		mMeal = meal;
		mDb = Toolbox.getDatabase(context);
		DaoSession daoSession = Toolbox.getDatabaseSession(context);
		mIngrDao = daoSession.getIngrDao();
		
		this.swapCursor(getIngrListCursor());
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        
        String item = cursor.getString(cursor.getColumnIndex(IngrDao.Properties.Name.columnName));
        ((TextView) view).setText(item);     
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
        
        return view;
	}

	@Override
	public Ingr getItem(int position) {
		Cursor c = (Cursor) super.getItem(position);
		return mIngrDao.load(c.getLong(2));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void notifyDataSetChanged() {
		getCursor().requery();
		super.notifyDataSetChanged();
	}
	
	public Cursor getIngrListCursor() {       
		
		String select = "SELECT * FROM ";
		String from = MealIngrDao.TABLENAME + " INNER JOIN " + IngrDao.TABLENAME;
		String on = " ON " + MealIngrDao.TABLENAME + "." + MealIngrDao.Properties.IngrId.columnName +
				" = " +
				IngrDao.TABLENAME + "." + IngrDao.Properties.Id.columnName;
        String where = " WHERE " + MealIngrDao.Properties.MealId.columnName + "=" + mMeal.getId();

        // get ingredients for the current meal (using a JOIN)
		Cursor c = mDb.rawQuery(select + from + on + where, null);
	
		return c;
	}
	
}
