package uz.uni_team.bluetooth_sample.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.uni_team.bluetooth_sample.data.chat.AndroidBluetoothControllerImpl
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @[Provides Singleton]
    fun provideBluetooth(@ApplicationContext context: Context):BluetoothController{
        return AndroidBluetoothControllerImpl(context)
    }

}