package com.example.voicebill.ui.screens.statistics;

import com.example.voicebill.domain.usecase.GetStatisticsUseCase;
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
public final class StatisticsViewModel_Factory implements Factory<StatisticsViewModel> {
  private final Provider<GetStatisticsUseCase> getStatisticsUseCaseProvider;

  public StatisticsViewModel_Factory(Provider<GetStatisticsUseCase> getStatisticsUseCaseProvider) {
    this.getStatisticsUseCaseProvider = getStatisticsUseCaseProvider;
  }

  @Override
  public StatisticsViewModel get() {
    return newInstance(getStatisticsUseCaseProvider.get());
  }

  public static StatisticsViewModel_Factory create(
      Provider<GetStatisticsUseCase> getStatisticsUseCaseProvider) {
    return new StatisticsViewModel_Factory(getStatisticsUseCaseProvider);
  }

  public static StatisticsViewModel newInstance(GetStatisticsUseCase getStatisticsUseCase) {
    return new StatisticsViewModel(getStatisticsUseCase);
  }
}
