package com.advent.group69.tradetracker.model

import io.reactivex.Observable
import com.advent.group69.tradetracker.model.StockRestService.CryptoModel
import com.advent.group69.tradetracker.stockRestService

class DataModel : IDataModel {
    override fun getCryptoPrice(tickerName: String) = stockRestService.cryptoPrice(tickerName)

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