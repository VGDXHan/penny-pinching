package com.example.voicebill.domain.usecase;

import com.example.voicebill.data.local.dao.CategoryDao;
import com.example.voicebill.data.local.dao.TransactionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.time.Clock;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class GetStatisticsUseCase_Factory implements Factory<GetStatisticsUseCase> {
  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<Clock> clockProvider;

  public GetStatisticsUseCase_Factory(Provider<TransactionDao> transactionDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<Clock> clockProvider) {
    this.transactionDaoProvider = transactionDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.clockProvider = clockProvider;
  }

  @Override
  public GetStatisticsUseCase get() {
    return newInstance(transactionDaoProvider.get(), categoryDaoProvider.get(), clockProvider.get());
  }

  public static GetStatisticsUseCase_Factory create(Provider<TransactionDao> transactionDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<Clock> clockProvider) {
    return new GetStatisticsUseCase_Factory(transactionDaoProvider, categoryDaoProvider, clockProvider);
  }

  public static GetStatisticsUseCase newInstance(TransactionDao transactionDao,
      CategoryDao categoryDao, Clock clock) {
    return new GetStatisticsUseCase(transactionDao, categoryDao, clock);
  }
}
