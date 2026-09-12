package com.carters.health

import android.app.Application
import com.carters.health.data.repo.HealthRepository
import com.carters.health.data.repo.InMemoryHealthRepository

/** Process-scoped holder for the repository until DI + Room land. */
class CartersHealthApplication : Application() {
    val repository: HealthRepository by lazy { InMemoryHealthRepository() }
}
