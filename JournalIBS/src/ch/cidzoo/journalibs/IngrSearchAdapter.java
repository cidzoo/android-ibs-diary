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
import ch.cidzoo.journalibs.db.IngrDao;

public class IngrSearchAdapter extends CursorAdapter {
	
	private SQLiteDatabase mDb;
	private IngrDao mIngredientDao;

	public IngrSearchAdapter(Context context) {
		super(context, null, 0);
		
		DaoSession daoSession = Toolbox.getDatabaseSession(context);
		mDb = daoSession.getDatabase();
		mIngredientDao = daoSession.getIngrDao();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        
        String item = cursor.getString(cursor.getColumnIndex(IngrDao.Properties.Name.columnName));      
        ((TextView) view).setText(item);     
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        
        return view;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }
        
        constraint = ((String) constraint).trim();
        
        //FIXME: would be great to be able to use greenDAO QueryBuilder but no Cursor returned
//      QueryBuilder<Ingredient> qb = ingredientDao.queryBuilder();
//		qb.where(IngredientDao.Properties.Name.eq(constraint != null ? constraint.toString() : null))
//    		.orderAsc(IngredientDao.Properties.Name);
        
        Cursor cursor = getIngredientCursor(constraint != null ? constraint.toString() : null);

        return cursor;
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		
		//return cursor.getString(IngredientDao.Properties.Name.ordinal);
		
		//we actually don't want to display the selected item into the TextView
		return null; 
	}
	
	public Cursor getIngredientCursor(String args) {       
		
		String textColumn = IngrDao.Properties.Name.columnName;
        String orderBy = textColumn + " COLLATE LOCALIZED ASC";
        args  = textColumn + " LIKE '%" + args + "%'";
		
        Cursor c = mDb.query(
				mIngredientDao.getTablename(), 
				mIngredientDao.getAllColumns(), 
				args, null, null, null, orderBy);
		
		return c;
	}
	
}
