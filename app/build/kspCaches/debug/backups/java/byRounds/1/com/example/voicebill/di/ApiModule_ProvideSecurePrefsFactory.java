package com.example.voicebill.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ApiModule_ProvideSecurePrefsFactory implements Factory<SecurePrefs> {
  private final Provider<Context> contextProvider;

  public ApiModule_ProvideSecurePrefsFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecurePrefs get() {
    return provideSecurePrefs(contextProvider.get());
  }

  public static ApiModule_ProvideSecurePrefsFactory create(Provider<Context> contextProvider) {
    return new ApiModule_ProvideSecurePrefsFactory(contextProvider);
  }

  public static SecurePrefs provideSecurePrefs(Context context) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideSecurePrefs(context));
  }
}
