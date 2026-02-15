package com.example.voicebill.ui.screens.records;

import com.example.voicebill.domain.repository.TransactionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class RecordsViewModel_Factory implements Factory<RecordsViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public RecordsViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public RecordsViewModel get() {
    return newInstance(transactionRepositoryProvider.get());
  }

  public static RecordsViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new RecordsViewModel_Factory(transactionRepositoryProvider);
  }

  public static RecordsViewModel newInstance(TransactionRepository transactionRepository) {
    return new RecordsViewModel(transactionRepository);
  }
}
