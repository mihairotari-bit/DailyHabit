package com.mihai.android17helloworld

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HelloRepository @Inject constructor() {
    fun greeting() = "Hello Android 17"
}
