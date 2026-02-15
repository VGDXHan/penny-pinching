package com.example.voicebill.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.time.Clock;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideClockFactory implements Factory<Clock> {
  @Override
  public Clock get() {
    return provideClock();
  }

  public static AppModule_ProvideClockFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Clock provideClock() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideClock());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideClockFactory INSTANCE = new AppModule_ProvideClockFactory();
  }
}
