package com.mihai.dailyhabit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActiveDayProfileResolverTest {
    private val resolver = ActiveDayProfileResolver()

    @Test
    fun `resolve TRAINING returns training profile`() {
        val plan = DietPlan(days = listOf(
            DailyMeals("Giorno senza allenamento", emptyList(), DayProfileType.REST),
            DailyMeals("Giorno con allenamento", emptyList(), DayProfileType.TRAINING)
        ))
        val resolved = resolver.resolve(plan, DayProfileType.TRAINING)
        assertEquals("Giorno con allenamento", resolved?.day)
    }

    @Test
    fun `resolve REST returns rest profile`() {
        val plan = DietPlan(days = listOf(
            DailyMeals("Giorno senza allenamento", emptyList(), DayProfileType.REST),
            DailyMeals("Giorno con allenamento", emptyList(), DayProfileType.TRAINING)
        ))
        val resolved = resolver.resolve(plan, DayProfileType.REST)
        assertEquals("Giorno senza allenamento", resolved?.day)
    }

    @Test
    fun `resolve WEEKDAY uses fallback string if semantic missing`() {
        val plan = DietPlan(days = listOf(
            DailyMeals("Lunedì", emptyList(), DayProfileType.WEEKDAY)
        ))
        val resolved = resolver.resolve(plan, DayProfileType.WEEKDAY, "Lunedì")
        assertEquals("Lunedì", resolved?.day)
    }

    @Test
    fun `resolve returns null if missing profile`() {
        val plan = DietPlan(days = listOf(
            DailyMeals("Lunedì", emptyList(), DayProfileType.WEEKDAY)
        ))
        val resolved = resolver.resolve(plan, DayProfileType.TRAINING)
        assertNull(resolved)
    }

    @Test
    fun `resolve returns null if only profile is semantically incompatible`() {
        val plan = DietPlan(days = listOf(
            DailyMeals("Giorno unico", emptyList(), DayProfileType.UNKNOWN)
        ))
        val resolved = resolver.resolve(plan, DayProfileType.TRAINING)
        assertNull(resolved)
    }
}
