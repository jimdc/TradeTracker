package com.advent.tradetracker.model

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.UnknownHostException
import com.advent.tradetracker.ErrorHandlingActivity
import timber.log.Timber


interface StockRestService {

    /**
     * Equivalent to https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD
     */

    @GET("price")
    fun cryptoPrice(
            @Query("fsym") cryptoTicker: String,
            @Query("tsyms") realLifeCurrency: String = "USD"
    ):
            Single<CryptoModel.Result>

    /**
     * {"USD":65.59}
     */
    object CryptoModel {
        data class Result(val USD: Double)
    }

    companion object {
        fun create(): StockRestService {

            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(Interceptor { chain ->
                        val request = chain.request()
                        try {
                            val response = chain.proceed(request)

                            when(response.code()) {
                                301 -> Timber.d("Server returned 301 URL moved; we need to update!")
                                400 -> Timber.d("Server says our request is malformed? (HTTP 400)")
                                403 -> Timber.d("Server is forbidding us from seeing content (403)")
                                404 -> Timber.d("Server giving us 404 not found")
                                429 -> Timber.d("Server is throttling (429) our frequent requests!")
                                500 -> Timber.d("Server returned 500 internal error, use backup?")
                            }


                                /**
                                 * This isn't called from a context, so a bit complicated.
                                 *
                                val intent = Intent(applicationContext, ErrorHandlingActivity::class.java)
                                intent.putExtra("errorCode", 500)
                                startActivityForResult(intent)
                                */

                            response

                        } catch (e: UnknownHostException) {
                            return@Interceptor null //I actually don't know the consequence of doing this.
                        }
                    })
                    .build()

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .client(okHttpClient)
                    .baseUrl(TradeTrackerConstants.CRYPTO_BASE_URL)
                    .build()

            return retrofit.create(StockRestService::class.java)
        }
    }
}