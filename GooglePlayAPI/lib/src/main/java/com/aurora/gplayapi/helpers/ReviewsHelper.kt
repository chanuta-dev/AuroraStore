/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.ReviewResponse
import com.aurora.gplayapi.data.builders.ReviewBuilder
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.Review
import com.aurora.gplayapi.data.models.ReviewCluster
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.bytesOrThrow

class ReviewsHelper(authData: AuthData) : NativeHelper(authData) {

    companion object {
        const val DEFAULT_SIZE = 20
        var OFFSET = 0
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    @Throws(Exception::class)
    private fun getReviewResponse(
        url: String,
        params: Map<String, String>,
        headers: Map<String, String>
    ): ReviewResponse? {
        val responseBody = getResponse(url, params, headers)
        val payload = getPayLoadFromBytes(responseBody.bytesOrThrow())

        return if (payload.hasReviewResponse()) {
            payload.reviewResponse
        } else {
            null
        }
    }

    @Throws(Exception::class)
    private fun getReviewSummaryResponse(
        url: String,
        params: Map<String, String>,
        headers: Map<String, String>
    ): ReviewResponse? {
        val responseBody = getResponse(url, params, headers)
        val payload = getPayLoadFromBytes(responseBody.responseBytes)
        return if (payload.hasReviewSummaryResponse()) {
            payload.reviewSummaryResponse
        } else {
            null
        }
    }

    @Throws(Exception::class)
    private fun postReviewResponse(
        params: Map<String, String>,
        headers: Map<String, String>
    ): ReviewResponse? {
        val playResponse = httpClient.post(GooglePlayApi.URL_REVIEW_ADD_EDIT, headers, params)
        val payload = getPayLoadFromBytes(playResponse.bytesOrThrow())

        return if (payload.hasReviewResponse()) payload.reviewResponse else null
    }

    private fun getReviewCluster(reviewResponse: ReviewResponse?): ReviewCluster {
        return reviewResponse?.let {
            val reviewList: MutableList<Review> = ArrayList()
            for (reviewProto in it.userReviewsResponse.reviewList) {
                reviewProto?.let { review ->
                    reviewList.add(ReviewBuilder.build(review))
                }
            }
            return@let ReviewCluster(
                reviewList = reviewList,
                nextPageUrl = it.nextPageUrl
            )
        } ?: ReviewCluster()
    }

    @Throws(Exception::class)
    fun getReviews(
        packageName: String,
        filter: Review.Filter,
        resultNum: Int = DEFAULT_SIZE
    ): ReviewCluster {
        val params: MutableMap<String, String> = HashMap()
        params["doc"] = packageName
        params["n"] = resultNum.toString()

        when (filter) {
            Review.Filter.NEWEST -> params["sort"] = filter.value
            Review.Filter.ALL -> params["sfilter"] = filter.value
            Review.Filter.POSITIVE, Review.Filter.CRITICAL -> params["sent"] = filter.value
            else -> params["rating"] = filter.value
        }

        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val reviewResponse = getReviewResponse(GooglePlayApi.URL_REVIEWS, params, headers)
        return getReviewCluster(reviewResponse)
    }

    @Throws(Exception::class)
    fun getReviewSummary(
        packageName: String
    ): List<Review> {
        val params: MutableMap<String, String> = HashMap()
        params["doc"] = packageName

        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val reviewResponse =
            getReviewSummaryResponse("${GooglePlayApi.URL_FDFE}/reviewSummary", params, headers)
        return getReviewCluster(reviewResponse).reviewList
    }

    @Throws(Exception::class)
    fun getUserReview(packageName: String, testing: Boolean): Review? {
        val params: MutableMap<String, String> = HashMap()
        params["doc"] = packageName
        params["itpr"] = if (testing) "true" else "false"
        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val reviewResponse = getReviewResponse(GooglePlayApi.URL_REVIEW_USER, params, headers)

        reviewResponse?.let {
            if (it.userReviewsResponse.reviewCount > 0) {
                val review = it.userReviewsResponse.getReview(0)
                return ReviewBuilder.build(review)
            }
        }

        return null
    }

    @Throws(Exception::class)
    fun addOrEditReview(
        packageName: String,
        title: String,
        content: String,
        rating: Int,
        isBeta: Boolean
    ): Review? {
        val params: MutableMap<String, String> = HashMap()
        params["doc"] = packageName
        params["title"] = title
        params["content"] = content
        params["rating"] = rating.toString()
        params["rst"] = "3"
        params["itpr"] = if (isBeta) "true" else "false"

        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val reviewResponse = postReviewResponse(params, headers)

        reviewResponse?.let {
            it.userReview?.let { review ->
                return ReviewBuilder.build(review)
            }
        }

        return null
    }

    @Throws(Exception::class)
    fun deleteReview(packageName: String, isBeta: Boolean = false) {
        val params: MutableMap<String, String> = HashMap()
        params["doc"] = packageName
        params["itpr"] = if (isBeta) "true" else "false"

        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        httpClient.post(GooglePlayApi.URL_REVIEW_DELETE, headers, params)
    }

    fun next(nextPageUrl: String): ReviewCluster {
        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val reviewResponse =
            getReviewResponse("${GooglePlayApi.URL_FDFE}/$nextPageUrl", mapOf(), headers)
        return getReviewCluster(reviewResponse)
    }
}
