package com.example.voicebill;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.voicebill.data.local.VoiceBillDatabase;
import com.example.voicebill.data.local.dao.CategoryDao;
import com.example.voicebill.data.local.dao.TransactionDao;
import com.example.voicebill.data.remote.DeepSeekApi;
import com.example.voicebill.data.repository.BillParserRepositoryImpl;
import com.example.voicebill.data.repository.CategoryRepositoryImpl;
import com.example.voicebill.data.repository.TransactionRepositoryImpl;
import com.example.voicebill.di.ApiModule_ProvideDeepSeekApiFactory;
import com.example.voicebill.di.ApiModule_ProvideOkHttpClientFactory;
import com.example.voicebill.di.ApiModule_ProvideRetrofitFactory;
import com.example.voicebill.di.ApiModule_ProvideSecurePrefsFactory;
import com.example.voicebill.di.AppModule_ProvideClockFactory;
import com.example.voicebill.di.DatabaseModule_ProvideCategoryDaoFactory;
import com.example.voicebill.di.DatabaseModule_ProvideDatabaseFactory;
import com.example.voicebill.di.DatabaseModule_ProvideTransactionDaoFactory;
import com.example.voicebill.di.SecurePrefs;
import com.example.voicebill.domain.usecase.ExportImportUseCase;
import com.example.voicebill.domain.usecase.GetStatisticsUseCase;
import com.example.voicebill.ui.screens.categories.CategoriesViewModel;
import com.example.voicebill.ui.screens.categories.CategoriesViewModel_HiltModules;
import com.example.voicebill.ui.screens.home.HomeViewModel;
import com.example.voicebill.ui.screens.home.HomeViewModel_HiltModules;
import com.example.voicebill.ui.screens.records.RecordsViewModel;
import com.example.voicebill.ui.screens.records.RecordsViewModel_HiltModules;
import com.example.voicebill.ui.screens.settings.SettingsViewModel;
import com.example.voicebill.ui.screens.settings.SettingsViewModel_HiltModules;
import com.example.voicebill.ui.screens.statistics.StatisticsViewModel;
import com.example.voicebill.ui.screens.statistics.StatisticsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.time.Clock;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerVoiceBillApplication_HiltComponents_SingletonC {
  private DaggerVoiceBillApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public VoiceBillApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements VoiceBillApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements VoiceBillApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements VoiceBillApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements VoiceBillApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements VoiceBillApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements VoiceBillApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements VoiceBillApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public VoiceBillApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends VoiceBillApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends VoiceBillApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends VoiceBillApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends VoiceBillApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_categories_CategoriesViewModel, CategoriesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_records_RecordsViewModel, RecordsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_statistics_StatisticsViewModel, StatisticsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_voicebill_ui_screens_home_HomeViewModel = "com.example.voicebill.ui.screens.home.HomeViewModel";

      static String com_example_voicebill_ui_screens_statistics_StatisticsViewModel = "com.example.voicebill.ui.screens.statistics.StatisticsViewModel";

      static String com_example_voicebill_ui_screens_settings_SettingsViewModel = "com.example.voicebill.ui.screens.settings.SettingsViewModel";

      static String com_example_voicebill_ui_screens_categories_CategoriesViewModel = "com.example.voicebill.ui.screens.categories.CategoriesViewModel";

      static String com_example_voicebill_ui_screens_records_RecordsViewModel = "com.example.voicebill.ui.screens.records.RecordsViewModel";

      @KeepFieldType
      HomeViewModel com_example_voicebill_ui_screens_home_HomeViewModel2;

      @KeepFieldType
      StatisticsViewModel com_example_voicebill_ui_screens_statistics_StatisticsViewModel2;

      @KeepFieldType
      SettingsViewModel com_example_voicebill_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      CategoriesViewModel com_example_voicebill_ui_screens_categories_CategoriesViewModel2;

      @KeepFieldType
      RecordsViewModel com_example_voicebill_ui_screens_records_RecordsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends VoiceBillApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<CategoriesViewModel> categoriesViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<RecordsViewModel> recordsViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StatisticsViewModel> statisticsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private ExportImportUseCase exportImportUseCase() {
      return new ExportImportUseCase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideTransactionDaoProvider.get(), singletonCImpl.provideCategoryDaoProvider.get());
    }

    private GetStatisticsUseCase getStatisticsUseCase() {
      return new GetStatisticsUseCase(singletonCImpl.provideTransactionDaoProvider.get(), singletonCImpl.provideCategoryDaoProvider.get(), singletonCImpl.provideClockProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.categoriesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.recordsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.statisticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_categories_CategoriesViewModel, ((Provider) categoriesViewModelProvider)).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_records_RecordsViewModel, ((Provider) recordsViewModelProvider)).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_example_voicebill_ui_screens_statistics_StatisticsViewModel, ((Provider) statisticsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_voicebill_ui_screens_statistics_StatisticsViewModel = "com.example.voicebill.ui.screens.statistics.StatisticsViewModel";

      static String com_example_voicebill_ui_screens_records_RecordsViewModel = "com.example.voicebill.ui.screens.records.RecordsViewModel";

      static String com_example_voicebill_ui_screens_settings_SettingsViewModel = "com.example.voicebill.ui.screens.settings.SettingsViewModel";

      static String com_example_voicebill_ui_screens_categories_CategoriesViewModel = "com.example.voicebill.ui.screens.categories.CategoriesViewModel";

      static String com_example_voicebill_ui_screens_home_HomeViewModel = "com.example.voicebill.ui.screens.home.HomeViewModel";

      @KeepFieldType
      StatisticsViewModel com_example_voicebill_ui_screens_statistics_StatisticsViewModel2;

      @KeepFieldType
      RecordsViewModel com_example_voicebill_ui_screens_records_RecordsViewModel2;

      @KeepFieldType
      SettingsViewModel com_example_voicebill_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      CategoriesViewModel com_example_voicebill_ui_screens_categories_CategoriesViewModel2;

      @KeepFieldType
      HomeViewModel com_example_voicebill_ui_screens_home_HomeViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.voicebill.ui.screens.categories.CategoriesViewModel 
          return (T) new CategoriesViewModel(singletonCImpl.categoryRepositoryImplProvider.get());

          case 1: // com.example.voicebill.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.billParserRepositoryImplProvider.get(), singletonCImpl.transactionRepositoryImplProvider.get(), singletonCImpl.categoryRepositoryImplProvider.get(), singletonCImpl.provideSecurePrefsProvider.get());

          case 2: // com.example.voicebill.ui.screens.records.RecordsViewModel 
          return (T) new RecordsViewModel(singletonCImpl.transactionRepositoryImplProvider.get());

          case 3: // com.example.voicebill.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideSecurePrefsProvider.get(), viewModelCImpl.exportImportUseCase());

          case 4: // com.example.voicebill.ui.screens.statistics.StatisticsViewModel 
          return (T) new StatisticsViewModel(viewModelCImpl.getStatisticsUseCase());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends VoiceBillApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends VoiceBillApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends VoiceBillApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<CategoryDao> provideCategoryDaoProvider;

    private Provider<VoiceBillDatabase> provideDatabaseProvider;

    private Provider<CategoryRepositoryImpl> categoryRepositoryImplProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<DeepSeekApi> provideDeepSeekApiProvider;

    private Provider<SecurePrefs> provideSecurePrefsProvider;

    private Provider<Clock> provideClockProvider;

    private Provider<BillParserRepositoryImpl> billParserRepositoryImplProvider;

    private Provider<TransactionDao> provideTransactionDaoProvider;

    private Provider<TransactionRepositoryImpl> transactionRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideCategoryDaoProvider = new DelegateFactory<>();
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<VoiceBillDatabase>(singletonCImpl, 2));
      DelegateFactory.setDelegate(provideCategoryDaoProvider, DoubleCheck.provider(new SwitchingProvider<CategoryDao>(singletonCImpl, 1)));
      this.categoryRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<CategoryRepositoryImpl>(singletonCImpl, 0));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 6));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 5));
      this.provideDeepSeekApiProvider = DoubleCheck.provider(new SwitchingProvider<DeepSeekApi>(singletonCImpl, 4));
      this.provideSecurePrefsProvider = DoubleCheck.provider(new SwitchingProvider<SecurePrefs>(singletonCImpl, 7));
      this.provideClockProvider = DoubleCheck.provider(new SwitchingProvider<Clock>(singletonCImpl, 8));
      this.billParserRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<BillParserRepositoryImpl>(singletonCImpl, 3));
      this.provideTransactionDaoProvider = DoubleCheck.provider(new SwitchingProvider<TransactionDao>(singletonCImpl, 10));
      this.transactionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<TransactionRepositoryImpl>(singletonCImpl, 9));
    }

    @Override
    public void injectVoiceBillApplication(VoiceBillApplication voiceBillApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.voicebill.data.repository.CategoryRepositoryImpl 
          return (T) new CategoryRepositoryImpl(singletonCImpl.provideCategoryDaoProvider.get());

          case 1: // com.example.voicebill.data.local.dao.CategoryDao 
          return (T) DatabaseModule_ProvideCategoryDaoFactory.provideCategoryDao(singletonCImpl.provideDatabaseProvider.get());

          case 2: // com.example.voicebill.data.local.VoiceBillDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCategoryDaoProvider);

          case 3: // com.example.voicebill.data.repository.BillParserRepositoryImpl 
          return (T) new BillParserRepositoryImpl(singletonCImpl.provideDeepSeekApiProvider.get(), singletonCImpl.provideSecurePrefsProvider.get(), singletonCImpl.provideClockProvider.get());

          case 4: // com.example.voicebill.data.remote.DeepSeekApi 
          return (T) ApiModule_ProvideDeepSeekApiFactory.provideDeepSeekApi(singletonCImpl.provideRetrofitProvider.get());

          case 5: // retrofit2.Retrofit 
          return (T) ApiModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 6: // okhttp3.OkHttpClient 
          return (T) ApiModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 7: // com.example.voicebill.di.SecurePrefs 
          return (T) ApiModule_ProvideSecurePrefsFactory.provideSecurePrefs(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // java.time.Clock 
          return (T) AppModule_ProvideClockFactory.provideClock();

          case 9: // com.example.voicebill.data.repository.TransactionRepositoryImpl 
          return (T) new TransactionRepositoryImpl(singletonCImpl.provideTransactionDaoProvider.get());

          case 10: // com.example.voicebill.data.local.dao.TransactionDao 
          return (T) DatabaseModule_ProvideTransactionDaoFactory.provideTransactionDao(singletonCImpl.provideDatabaseProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
