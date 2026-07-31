package pl.septicwolf818.calltamer

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import pl.septicwolf818.calltamer.worker.ExpiryReconciliationWorker
import javax.inject.Inject

@HiltAndroidApp
class CallTamerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        ExpiryReconciliationWorker.enqueue(this)
    }
}
