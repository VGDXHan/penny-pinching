package com.example.voicebill.ui.screens.home;

import com.example.voicebill.di.SecurePrefs;
import com.example.voicebill.domain.repository.BillParserRepository;
import com.example.voicebill.domain.repository.CategoryRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<BillParserRepository> billParserRepositoryProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<SecurePrefs> securePrefsProvider;

  public HomeViewModel_Factory(Provider<BillParserRepository> billParserRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<SecurePrefs> securePrefsProvider) {
    this.billParserRepositoryProvider = billParserRepositoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.securePrefsProvider = securePrefsProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(billParserRepositoryProvider.get(), transactionRepositoryProvider.get(), categoryRepositoryProvider.get(), securePrefsProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<BillParserRepository> billParserRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<SecurePrefs> securePrefsProvider) {
    return new HomeViewModel_Factory(billParserRepositoryProvider, transactionRepositoryProvider, categoryRepositoryProvider, securePrefsProvider);
  }

  public static HomeViewModel newInstance(BillParserRepository billParserRepository,
      TransactionRepository transactionRepository, CategoryRepository categoryRepository,
      SecurePrefs securePrefs) {
    return new HomeViewModel(billParserRepository, transactionRepository, categoryRepository, securePrefs);
  }
}
