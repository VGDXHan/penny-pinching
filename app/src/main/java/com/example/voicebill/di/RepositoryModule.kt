package com.example.voicebill.di

import com.example.voicebill.data.repository.BillParserRepositoryImpl
import com.example.voicebill.data.repository.CategoryRepositoryImpl
import com.example.voicebill.data.repository.TransactionRepositoryImpl
import com.example.voicebill.domain.repository.BillParserRepository
import com.example.voicebill.domain.repository.CategoryRepository
import com.example.voicebill.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBillParserRepository(
        impl: BillParserRepositoryImpl
    ): BillParserRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
}
