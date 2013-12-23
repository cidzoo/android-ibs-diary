package ch.cidzoo.journalibs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import ch.cidzoo.journalibs.common.Toolbox;
import ch.cidzoo.journalibs.db.DaoSession;
import ch.cidzoo.journalibs.db.Ingr;
import ch.cidzoo.journalibs.db.IngrDao;
import ch.cidzoo.journalibs.db.Meal;
import ch.cidzoo.journalibs.db.MealDao;
import ch.cidzoo.journalibs.db.MealIngr;

public class ChartsActivity extends Activity {

	GraphicalView mChartView = null;
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private CategorySeries mSeries = new CategorySeries("Discomfort");
	private static int[] COLORS = new int[] {
		Color.parseColor("#33B5E5"),
		Color.parseColor("#AA66CC"),
		Color.parseColor("#99CC00"),
		Color.parseColor("#FFBB33"),
		Color.parseColor("#FF4444"),
		Color.parseColor("#0099CC"),
		Color.parseColor("#9933CC"),
		Color.parseColor("#669900"),
		Color.parseColor("#FF8800"),
		Color.parseColor("#CC0000")
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charts);
		
		SimpleSeriesRenderer serieRenderer;
		mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
		
		// get the ingredients discomfort indices
		Hashtable<String, Integer> IngrWellness = getIngrDiscomfortIndices();
		Log.i("Wellness", IngrWellness.toString());
		
		// iterate over the list returned above to fill-in the piechart
		Iterator<Entry<String, Integer>> it = IngrWellness.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Integer> ingr = it.next();
			
			// not interested in "comfortable" ingredients
			if (ingr.getValue() <= 0) 
				continue;
				
			mSeries.add(ingr.getKey(), ingr.getValue());
			serieRenderer = new SimpleSeriesRenderer();
			serieRenderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
			serieRenderer.setDisplayChartValues(true);
			mRenderer.addSeriesRenderer(serieRenderer);
			mChartView.repaint();
		}
		
		// visual tuning
		mRenderer.setShowLegend(false);
		mRenderer.setChartTitle(getString(R.string.title_discomfort_chart));
		mRenderer.setChartTitleTextSize(48);
		mRenderer.setLabelsTextSize(24);
		mRenderer.setLabelsColor(Color.DKGRAY);
		mRenderer.setAntialiasing(true);
		mRenderer.setScale((float) 0.75);
		
		// add to layout
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		mChartView.setLayoutParams(params);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_charts);
		layout.addView(mChartView);

	}
	
	/**
	 * Get discomfort indices for each ingredient in the DB.
	 * The rule is the following : each time a ingredient causes discomfort,
	 * his index is increased by 1. Each time a ingredient causes no disturbs,
	 * his index is decreased by 1.
	 * @return a hashtable with each ingredient as the key and the index as the value
	 */
	private Hashtable<String, Integer> getIngrDiscomfortIndices() {
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
				} else {
					ingrMap.put(ingr.getName(), ingrMap.get(ingr.getName()) - 1);
				}
			}

		}
		
		return ingrMap;
	}

}
