package com.advent.tradetracker.model

import com.advent.tradetracker.alphaVantageService
import com.advent.tradetracker.nASDAQService
import com.advent.tradetracker.stockRestService

class DataModel : IDataModel {
    override fun getCryptoPrice(tickerName: String) = stockRestService.cryptoPrice(tickerName)
    override fun getStockPrice(tickerName: String) = nASDAQService.stockPrice(tickerName)

    override fun getCryptoPriceAV(tickerName: String) = alphaVantageService.cryptoPrice()
    override fun batchStockPriceAV(tickerName: String) = alphaVantageService.batchStockPrice(tickerName)

    /**
     * for fresher data
     *
     *  https://riggaroo.co.za/introduction-android-testing-part3/
     *     @Override
    public Observable<List<User>> searchUsers(final String searchTerm) {
    return Observable.defer(() -> githubUserRestService.searchGithubUsers(searchTerm).concatMap(
    usersList -> Observable.from(usersList.getItems())
    .concatMap(user -> githubUserRestService.getUser(user.getLogin())).toList()))
    .retryWhen(observable -> observable.flatMap(o -> {
    if (o instanceof IOException) {
    return Observable.just(null);
    }
    return Observable.error(o);
    }));
    }
     */
}