import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;


/**
 * Generates entities and DAOs for the JournalIBS Android project.
 * 
 * Run it as a Java application.
 * 
 * @author Romain Maffina
 *
 */
public class MealsDiaryGenerator {

	public static void main(String[] args) {
		Schema schema = new Schema(2, "ch.cidzoo.journalibs.db");
		
		/* Ingredient entity */
		Entity ingr = schema.addEntity("Ingr");
		ingr.addIdProperty();
		ingr.addStringProperty("name").notNull();
		
		/* Meal entity */
		Entity meal = schema.addEntity("Meal");
		meal.addIdProperty();
		meal.addDateProperty("date");
		meal.addDoubleProperty("latitude");
		meal.addDoubleProperty("longitude");
		
		/* Join entity for Meals and Ingredients */
		Entity mealIngr = schema.addEntity("MealIngr");
		mealIngr.addIdProperty();
		Property mealId = mealIngr.addLongProperty("mealId").notNull().getProperty();
		ToMany mealToIngrs = meal.addToMany(mealIngr, mealId);
		mealToIngrs.setName("mealToIngrs"); // Optional
		Property ingrId = mealIngr.addLongProperty("ingrId").notNull().getProperty();
		ToMany ingrToMeals = ingr.addToMany(mealIngr, ingrId);
		ingrToMeals.setName("ingrToMeals"); // Optional
		
		try {
			new DaoGenerator().generateAll(schema, "../JournalIBS/src");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
