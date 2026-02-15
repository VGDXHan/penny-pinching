package com.example.voicebill.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.voicebill.data.local.VoiceBillDatabase
import com.example.voicebill.data.local.dao.CategoryDao
import com.example.voicebill.data.local.dao.TransactionDao
import com.example.voicebill.data.local.entity.CategoryEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        categoryDaoProvider: Provider<CategoryDao>
    ): VoiceBillDatabase {
        return Room.databaseBuilder(
            context,
            VoiceBillDatabase::class.java,
            VoiceBillDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(categoryDaoProvider.get())
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: VoiceBillDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: VoiceBillDatabase): TransactionDao {
        return database.transactionDao()
    }

    private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
        val defaultCategories = listOf(
            // Expense categories
            CategoryEntity(
                name = "餐饮",
                icon = "restaurant",
                color = "#FF6B6B",
                isIncome = false,
                isDefault = true
            ),
            CategoryEntity(
                name = "交通",
                icon = "directions_car",
                color = "#4ECDC4",
                isIncome = false,
                isDefault = true
            ),
            CategoryEntity(
                name = "购物",
                icon = "shopping_bag",
                color = "#FFE66D",
                isIncome = false,
                isDefault = true
            ),
            CategoryEntity(
                name = "其他",
                icon = "more_horiz",
                color = "#B8B8B8",
                isIncome = false,
                isDefault = true
            ),
            // Income categories
            CategoryEntity(
                name = "工资",
                icon = "payments",
                color = "#95E1D3",
                isIncome = true,
                isDefault = true
            ),
            CategoryEntity(
                name = "红包",
                icon = "redeem",
                color = "#FF8A5B",
                isIncome = true,
                isDefault = true
            ),
            CategoryEntity(
                name = "其他收入",
                icon = "attach_money",
                color = "#A8E6CF",
                isIncome = true,
                isDefault = true
            )
        )
        categoryDao.insertCategories(defaultCategories)
    }
}
