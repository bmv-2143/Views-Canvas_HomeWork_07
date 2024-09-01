package otus.homework.customview.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun provideDispatcherIO() : CoroutineDispatcher = Dispatchers.IO

}