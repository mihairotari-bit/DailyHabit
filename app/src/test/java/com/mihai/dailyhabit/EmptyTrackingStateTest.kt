package com.mihai.dailyhabit

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class EmptyTrackingStateTest {

    @Test
    fun `empty state logic works`() {
        val fakeRepository = mockk<DietPlanRepository>(relaxed = true)
        val resolver = ActiveDayProfileResolver()
        val viewModel = DailyTrackingViewModel(fakeRepository, resolver)
        
        val plan = DietPlan(days = emptyList())
        val resolved = resolver.resolve(plan, DayProfileType.TRAINING)
        assertEquals(null, resolved)
    }
}
