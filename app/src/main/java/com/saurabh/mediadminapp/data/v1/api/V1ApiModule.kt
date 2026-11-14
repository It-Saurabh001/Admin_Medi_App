package com.saurabh.mediadminapp.data.v1.api

import com.saurabh.mediadminapp.data.v1.network.V1AuthRetrofit
import com.saurabh.mediadminapp.data.v1.network.V1Retrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** Provides every v1 Retrofit API. All on @V1Retrofit except HealthApi (@V1AuthRetrofit, no token). */
@Module
@InstallIn(SingletonComponent::class)
object V1ApiModule {
    @Provides @Singleton fun metaApi(@V1Retrofit r: Retrofit): MetaApi = r.create(MetaApi::class.java)
    @Provides @Singleton fun healthApi(@V1AuthRetrofit r: Retrofit): HealthApi = r.create(HealthApi::class.java)
    @Provides @Singleton fun authApi(@V1Retrofit r: Retrofit): AuthApi = r.create(AuthApi::class.java)
    @Provides @Singleton fun usersApi(@V1Retrofit r: Retrofit): UsersApi = r.create(UsersApi::class.java)
    @Provides @Singleton fun adminsApi(@V1Retrofit r: Retrofit): AdminsApi = r.create(AdminsApi::class.java)
    @Provides @Singleton fun devicesApi(@V1Retrofit r: Retrofit): DevicesApi = r.create(DevicesApi::class.java)
    @Provides @Singleton fun productsApi(@V1Retrofit r: Retrofit): ProductsApi = r.create(ProductsApi::class.java)
    @Provides @Singleton fun inventoryApi(@V1Retrofit r: Retrofit): InventoryApi = r.create(InventoryApi::class.java)
    @Provides @Singleton fun ordersApi(@V1Retrofit r: Retrofit): OrdersApi = r.create(OrdersApi::class.java)
    @Provides @Singleton fun salesApi(@V1Retrofit r: Retrofit): SalesApi = r.create(SalesApi::class.java)
    @Provides @Singleton fun suppliersApi(@V1Retrofit r: Retrofit): SuppliersApi = r.create(SuppliersApi::class.java)
    @Provides @Singleton fun reportsApi(@V1Retrofit r: Retrofit): ReportsApi = r.create(ReportsApi::class.java)
    @Provides @Singleton fun auditApi(@V1Retrofit r: Retrofit): AuditApi = r.create(AuditApi::class.java)
}
