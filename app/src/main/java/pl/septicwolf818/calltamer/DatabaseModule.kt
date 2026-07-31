package pl.septicwolf818.calltamer

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.septicwolf818.calltamer.data.local.CallTamerDatabase
import pl.septicwolf818.calltamer.data.local.dao.BlockHistoryDao
import pl.septicwolf818.calltamer.data.local.dao.BlockRuleDao
import pl.septicwolf818.calltamer.data.repository.BlockRepository
import pl.septicwolf818.calltamer.data.repository.BlockRepositoryImpl
import pl.septicwolf818.calltamer.data.repository.HistoryRepository
import pl.septicwolf818.calltamer.data.repository.HistoryRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindBlockRepository(impl: BlockRepositoryImpl): BlockRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): CallTamerDatabase {
            return Room.databaseBuilder(
                context,
                CallTamerDatabase::class.java,
                "calltamer.db"
            ).build()
        }

        @Provides
        fun provideBlockRuleDao(database: CallTamerDatabase): BlockRuleDao = database.blockRuleDao()

        @Provides
        fun provideBlockHistoryDao(database: CallTamerDatabase): BlockHistoryDao = database.blockHistoryDao()
    }
}
