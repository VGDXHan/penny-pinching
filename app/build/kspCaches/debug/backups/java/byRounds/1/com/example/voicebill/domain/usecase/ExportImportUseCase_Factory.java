package com.example.voicebill.domain.usecase;

import android.content.Context;
import com.example.voicebill.data.local.dao.CategoryDao;
import com.example.voicebill.data.local.dao.TransactionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ExportImportUseCase_Factory implements Factory<ExportImportUseCase> {
  private final Provider<Context> contextProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  public ExportImportUseCase_Factory(Provider<Context> contextProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<CategoryDao> categoryDaoProvider) {
    this.contextProvider = contextProvider;
    this.transactionDaoProvider = transactionDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
  }

  @Override
  public ExportImportUseCase get() {
    return newInstance(contextProvider.get(), transactionDaoProvider.get(), categoryDaoProvider.get());
  }

  public static ExportImportUseCase_Factory create(Provider<Context> contextProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<CategoryDao> categoryDaoProvider) {
    return new ExportImportUseCase_Factory(contextProvider, transactionDaoProvider, categoryDaoProvider);
  }

  public static ExportImportUseCase newInstance(Context context, TransactionDao transactionDao,
      CategoryDao categoryDao) {
    return new ExportImportUseCase(context, transactionDao, categoryDao);
  }
}
