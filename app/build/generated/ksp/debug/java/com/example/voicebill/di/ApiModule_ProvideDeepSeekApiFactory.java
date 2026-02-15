package com.example.voicebill.di;

import com.example.voicebill.data.remote.DeepSeekApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class ApiModule_ProvideDeepSeekApiFactory implements Factory<DeepSeekApi> {
  private final Provider<Retrofit> retrofitProvider;

  public ApiModule_ProvideDeepSeekApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public DeepSeekApi get() {
    return provideDeepSeekApi(retrofitProvider.get());
  }

  public static ApiModule_ProvideDeepSeekApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new ApiModule_ProvideDeepSeekApiFactory(retrofitProvider);
  }

  public static DeepSeekApi provideDeepSeekApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ApiModule.INSTANCE.provideDeepSeekApi(retrofit));
  }
}
