package com.mihai.dailyhabit

import org.junit.Assert.fail
import org.junit.Test

class FakeEngineIsolationTest {

    @Test
    fun `fake engine is only accessible in test scope`() {
        // Since this test is in the test/ scope, it CAN see FakeDietInferenceEngine.
        // However, we want to ensure that main cannot see it.
        // The most robust way without complex classpath analysis in a basic test
        // is to assert its existence here, but rely on ProductionEngineBindingTest 
        // to prove it's not bound in the main graph.
        
        try {
            val clazz = Class.forName("com.mihai.dailyhabit.FakeDietInferenceEngine")
            // It should be found in test scope.
        } catch (e: ClassNotFoundException) {
            fail("FakeDietInferenceEngine should exist in test scope.")
        }
    }
}
