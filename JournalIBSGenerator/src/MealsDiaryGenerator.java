import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;


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
		
		addIngredient(schema);
		addMeal(schema);
		
		try {
			new DaoGenerator().generateAll(schema, "../JournalIBS/src");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void addIngredient(Schema schema) {
		Entity ingr = schema.addEntity("Ingredient");
		ingr.addIdProperty();
		ingr.addStringProperty("name").notNull();
	}
	
	private static void addMeal(Schema schema) {	
		Entity meal = schema.addEntity("Meal");
		meal.addIdProperty();
		meal.addDateProperty("date");
		meal.addDoubleProperty("latitude");
		meal.addDoubleProperty("longitude");
	}

}
