package com.mihai.dailyhabit

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductionEngineBindingTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var engine: DietInferenceEngine

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun productionGraphBindsLegacyEngineNotFake() {
        // Assert that the injected engine is indeed the real Deterministic one, 
        // not the Fake used for UI preview testing.
        assertTrue("Engine should be LegacyDeterministicDietInferenceEngine", engine is LegacyDeterministicDietInferenceEngine)
        // Ensure it's not FakeDietInferenceEngine (which shouldn't even be on the classpath, but just to be sure)
        assertFalse("Engine MUST NOT be FakeDietInferenceEngine", engine.javaClass.simpleName == "FakeDietInferenceEngine")
    }
}
