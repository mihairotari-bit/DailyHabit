package com.mihai.dailyhabit

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "diet_plans")
data class DietPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String = PlanType.UNKNOWN.name,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val day: String,
    val mealType: String,
    val groupId: String,
    val name: String,
    val quantity: String,
    val calories: Int?,
    val proteinGrams: Float?,
    val carbsGrams: Float?,
    val fatGrams: Float?,
)

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: String,
    val planId: Long,
    val trained: Boolean,
    val logJson: String
)

@Dao
interface DietDao {
    @Insert suspend fun insertPlan(plan: DietPlanEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFoods(items: List<FoodItemEntity>)
    @Query("SELECT * FROM diet_plans ORDER BY createdAtEpochMillis DESC") suspend fun plans(): List<DietPlanEntity>
    @Query("SELECT * FROM food_items WHERE planId = :planId") suspend fun foods(planId: Long): List<FoodItemEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertDailyLog(log: DailyLogEntity)
    @Query("SELECT * FROM daily_logs ORDER BY date DESC") suspend fun dailyLogs(): List<DailyLogEntity>
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1") suspend fun dailyLog(date: String): DailyLogEntity?
    @Query("DELETE FROM daily_logs") suspend fun clearLogs()
    @Query("DELETE FROM food_items") suspend fun clearFoods()
    @Query("DELETE FROM diet_plans") suspend fun clearPlans()
}

@Database(entities = [DietPlanEntity::class, FoodItemEntity::class, DailyLogEntity::class], version = 3, exportSchema = false)
abstract class DietDatabase : RoomDatabase() { abstract fun dietDao(): DietDao }

@Singleton
class DietPlanRepository @Inject constructor(private val database: DietDatabase) {
    suspend fun save(plan: DietPlan): Long {
        return database.withTransaction {
            val id = database.dietDao().insertPlan(DietPlanEntity(title = plan.title, type = plan.type.name))
            val foods = plan.days.flatMap { day -> day.meals.flatMap { meal -> meal.groups.flatMap { group -> group.alternatives.map { food ->
                FoodItemEntity(planId = id, day = day.day, mealType = meal.type.name, groupId = group.id, name = food.name,
                    quantity = food.quantity, calories = food.calories, proteinGrams = food.proteinGrams,
                    carbsGrams = food.carbsGrams, fatGrams = food.fatGrams)
            } } } }
            if (foods.isNotEmpty()) database.dietDao().insertFoods(foods)
            id
        }
    }
    
    suspend fun getPlan(planId: Long): DietPlan? {
        val planEntity = database.dietDao().plans().firstOrNull { it.id == planId } ?: return null
        val foods = database.dietDao().foods(planId)
        val days = foods.groupBy { it.day }.map { (day, dayFoods) ->
            val meals = dayFoods.groupBy { it.mealType }.map { (mealType, mealFoods) ->
                val groups = mealFoods.groupBy { it.groupId }.map { (groupId, groupFoods) ->
                    OptionGroup(groupId, groupFoods.map { FoodItem(name = it.name, quantity = it.quantity, calories = it.calories, proteinGrams = it.proteinGrams, carbsGrams = it.carbsGrams, fatGrams = it.fatGrams) })
                }
                Meal(MealType.valueOf(mealType), groups)
            }
            DailyMeals(day, meals)
        }
        val planType = runCatching { PlanType.valueOf(planEntity.type) }.getOrDefault(PlanType.UNKNOWN)
        return DietPlan(planEntity.title, planType, days)
    }
    
    suspend fun latestPlan(): DietPlan? = database.dietDao().plans().firstOrNull()?.id?.let { getPlan(it) }
    suspend fun latestPlanId(): Long? = database.dietDao().plans().firstOrNull()?.id
    suspend fun saveDailyLog(log: DailyLogEntity) = database.dietDao().insertDailyLog(log)
    suspend fun getDailyLogs(): List<DailyLogEntity> = database.dietDao().dailyLogs()
    suspend fun getDailyLog(date: String): DailyLogEntity? = database.dietDao().dailyLog(date)
    suspend fun clearAll() = database.withTransaction {
        database.dietDao().clearLogs(); database.dietDao().clearFoods(); database.dietDao().clearPlans()
    }
}
