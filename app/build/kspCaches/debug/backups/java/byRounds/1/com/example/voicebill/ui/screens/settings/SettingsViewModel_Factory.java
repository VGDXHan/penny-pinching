package com.example.voicebill.ui.screens.settings;

import com.example.voicebill.di.SecurePrefs;
import com.example.voicebill.domain.usecase.ExportImportUseCase;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SecurePrefs> securePrefsProvider;

  private final Provider<ExportImportUseCase> exportImportUseCaseProvider;

  public SettingsViewModel_Factory(Provider<SecurePrefs> securePrefsProvider,
      Provider<ExportImportUseCase> exportImportUseCaseProvider) {
    this.securePrefsProvider = securePrefsProvider;
    this.exportImportUseCaseProvider = exportImportUseCaseProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(securePrefsProvider.get(), exportImportUseCaseProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SecurePrefs> securePrefsProvider,
      Provider<ExportImportUseCase> exportImportUseCaseProvider) {
    return new SettingsViewModel_Factory(securePrefsProvider, exportImportUseCaseProvider);
  }

  public static SettingsViewModel newInstance(SecurePrefs securePrefs,
      ExportImportUseCase exportImportUseCase) {
    return new SettingsViewModel(securePrefs, exportImportUseCase);
  }
}
