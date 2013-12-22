package ch.cidzoo.journalibs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.DaoSession;
import ch.cidzoo.journalibs.db.Ingr;
import ch.cidzoo.journalibs.db.IngrDao;
import ch.cidzoo.journalibs.db.Meal;
import ch.cidzoo.journalibs.db.MealDao;
import ch.cidzoo.journalibs.db.MealIngr;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;

public class WellnessChartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charts);
		
//		// init example series data
//		GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
//		      new GraphViewData(1, 1)
//		      , new GraphViewData(2, -3)
//		      , new GraphViewData(3, 2)
//		      , new GraphViewData(4, 4)
//		});
//		 
//		LineGraphView graphView = new LineGraphView(
//		      this // context
//		      , "GraphViewDemo" // heading
//		);
//		graphView.addSeries(exampleSeries); // data
//		graphView.setHorizontalLabels(new String[] {"2 days ago", "yesterday", "today", "tomorrow"});
//		//graphView.setDrawValuesOnTop(true);
//		 
//		LinearLayout layout = (LinearLayout) findViewById(R.id.wellness_chart);
//		layout.addView(graphView);
		
		PieGraph pg = (PieGraph)findViewById(R.id.wellness_chart);
		PieSlice slice;
		
		Hashtable<String, Integer> IngrWellness = getWellnessIndices();
		Log.i("Wellness", IngrWellness.toString());
		
		Iterator<Entry<String, Integer>> i = IngrWellness.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, Integer> ingr = i.next();
			Log.i("Wellness", ingr.getKey());
			slice = new PieSlice();
			Random color = new Random();
			slice.setColor(Color.rgb(color.nextInt(256),color.nextInt(256),color.nextInt(256)));
			slice.setTitle(ingr.getKey());
			slice.setValue(ingr.getValue());
			pg.addSlice(slice);
		}
	}
	
	private Hashtable<String, Integer> getWellnessIndices() {
		Hashtable<String, Integer> ingrMap = new Hashtable<String, Integer>();
		
		DaoSession daoSession = Toolbox.getDatabaseSession(this);
		MealDao mealDao = daoSession.getMealDao();
		IngrDao ingrDao = daoSession.getIngrDao();
		
		List<Meal> meals = mealDao.loadAll();
		
		Iterator<Meal> i = meals.iterator();
		while (i.hasNext()) {
			Meal meal = i.next();
			List<MealIngr> ingrList = meal.getMealToIngrs();

			Iterator<MealIngr> j = ingrList.iterator();
			while (j.hasNext()) {
				MealIngr ingrIndex = j.next();
				Ingr ingr = ingrDao.load(ingrIndex.getIngrId());
				
				Log.i("WellnessChartActivity", "ingr=" + ingr.getName() + ", indice=" + meal.getNausea());
				// insert ingredient into hashtable if don't already exists
				if (!ingrMap.containsKey(ingr.getName()))
					ingrMap.put(ingr.getName(), 0);
				
				// update the wellness index value for the current ingredient
				if (meal.getNausea() || meal.getColic() || meal.getDiarrhea()) {
					ingrMap.put(ingr.getName(), ingrMap.get(ingr.getName()) + 1);
				} else if (ingrMap.get(ingr.getName()) > 0) {
					ingrMap.put(ingr.getName(), ingrMap.get(ingr.getName()) - 1);
				}
			}

		}
		
		return ingrMap;
	}

}
