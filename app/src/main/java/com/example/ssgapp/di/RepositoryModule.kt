package com.example.ssgapp.di

import com.example.ssgapp.common.data.repository.AuthenticationRepositoryImpl
import com.example.ssgapp.common.data.repository.FakeAuthenticationRepositoryImpl
import com.example.ssgapp.common.data.repository.FakeWorkerRepositoryImpl
import com.example.ssgapp.common.data.repository.WorkerRepositoryImpl
import com.example.ssgapp.common.domain.repository.AuthenticationRepository
import com.example.ssgapp.common.domain.repository.WorkerRepository
import com.example.ssgapp.groundworker.data.repository.ActiveMachineryRepositoryImpl
import com.example.ssgapp.groundworker.data.repository.FakeActiveMachineryRepositoryImpl
import com.example.ssgapp.groundworker.domain.repository.ActiveMachineryRepository
import com.example.ssgapp.operator.data.repository.FakeMachineryRepositoryImpl
import com.example.ssgapp.operator.data.repository.MachineryRepositoryImpl
import com.example.ssgapp.operator.domain.repository.MachineryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMachineryRepository(impl: MachineryRepositoryImpl): MachineryRepository
    //abstract fun bindMachineryRepository(impl: FakeMachineryRepositoryImpl): MachineryRepository

    @Binds
    @Singleton
    abstract fun bindActiveMachineryRepository(impl: ActiveMachineryRepositoryImpl): ActiveMachineryRepository

    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(impl: AuthenticationRepositoryImpl): AuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindWorkerRepository(impl: WorkerRepositoryImpl): WorkerRepository
}