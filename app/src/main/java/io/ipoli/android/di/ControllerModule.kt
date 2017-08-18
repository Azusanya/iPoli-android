package io.ipoli.android.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.navigation.Navigator

/**
* Created by Venelin Valkov <venelin@curiousily.com>
* on 8/1/17.
*/
@Module
class ControllerModule(private val navigator: Navigator) {

    @Provides
    @ControllerScope
    fun provideNavigator() = navigator
}