package com.example.voicebill.di;

import android.content.Context;
import com.example.voicebill.data.local.VoiceBillDatabase;
import com.example.voicebill.data.local.dao.CategoryDao;
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
public final class DatabaseModule_ProvideDatabaseFactory implements Factory<VoiceBillDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  public DatabaseModule_ProvideDatabaseFactory(Provider<Context> contextProvider,
      Provider<CategoryDao> categoryDaoProvider) {
    this.contextProvider = contextProvider;
    this.categoryDaoProvider = categoryDaoProvider;
  }

  @Override
  public VoiceBillDatabase get() {
    return provideDatabase(contextProvider.get(), categoryDaoProvider);
  }

  public static DatabaseModule_ProvideDatabaseFactory create(Provider<Context> contextProvider,
      Provider<CategoryDao> categoryDaoProvider) {
    return new DatabaseModule_ProvideDatabaseFactory(contextProvider, categoryDaoProvider);
  }

  public static VoiceBillDatabase provideDatabase(Context context,
      Provider<CategoryDao> categoryDaoProvider) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDatabase(context, categoryDaoProvider));
  }
}
