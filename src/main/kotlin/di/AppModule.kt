package di

import api.RedditApi
import data.Database
import org.koin.dsl.module
import vm.MainViewModel

val appModule = module {
    single { Database() }
    single { MainViewModel(get(), get()) }
    single { RedditApi(get()) }
}