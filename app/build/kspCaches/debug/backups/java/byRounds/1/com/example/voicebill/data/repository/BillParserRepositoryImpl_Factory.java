package com.example.voicebill.data.repository;

import com.example.voicebill.data.remote.DeepSeekApi;
import com.example.voicebill.di.SecurePrefs;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.time.Clock;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BillParserRepositoryImpl_Factory implements Factory<BillParserRepositoryImpl> {
  private final Provider<DeepSeekApi> deepSeekApiProvider;

  private final Provider<SecurePrefs> securePrefsProvider;

  private final Provider<Clock> clockProvider;

  public BillParserRepositoryImpl_Factory(Provider<DeepSeekApi> deepSeekApiProvider,
      Provider<SecurePrefs> securePrefsProvider, Provider<Clock> clockProvider) {
    this.deepSeekApiProvider = deepSeekApiProvider;
    this.securePrefsProvider = securePrefsProvider;
    this.clockProvider = clockProvider;
  }

  @Override
  public BillParserRepositoryImpl get() {
    return newInstance(deepSeekApiProvider.get(), securePrefsProvider.get(), clockProvider.get());
  }

  public static BillParserRepositoryImpl_Factory create(Provider<DeepSeekApi> deepSeekApiProvider,
      Provider<SecurePrefs> securePrefsProvider, Provider<Clock> clockProvider) {
    return new BillParserRepositoryImpl_Factory(deepSeekApiProvider, securePrefsProvider, clockProvider);
  }

  public static BillParserRepositoryImpl newInstance(DeepSeekApi deepSeekApi,
      SecurePrefs securePrefs, Clock clock) {
    return new BillParserRepositoryImpl(deepSeekApi, securePrefs, clock);
  }
}
