# GPlayAPI

Google Play Store Protobuf API wrapper in Kotlin

**For Educational & Research purpose only**

## Disclaimer

I'm not resposible for anything that may go wrong with:
1. You 
2. Your Google Account.
3. Socio-economic Life, especially the nudes.
4. Girlfriend.
5. Crypto Wallet.

**Hold your own beer!**

## Download

GPlayAPI is available on the [mavencentral](https://central.sonatype.com/artifact/com.auroraoss/gplayapi).

```kotlin
implementation("com.aurora:gplayapi:3.6.4")
```

You can get unsigned release builds for testing latest changes from our [GitLab Package Registry](https://gitlab.com/AuroraOSS/gplayapi/-/packages).
Jitpack is also available but not recommended due to unforeseen sync errors.

## Build

To build the library locally, run the following commands:

```shell
git clone https://gitlab.com/AuroraOSS/gplayapi.git && cd gplayapi
./gradlew publishReleasePublicationToLocalRepository
```

This will generate an unsigned release build 
([AAR](https://developer.android.com/studio/projects/android-library.html#aar-contents)) in the
`lib/build/repo` directory.

## Work Flow

1. Obtain AASToken from (Email, Password) pair.
2. Obtain AuthData from (Email, AASToken) pair.
3. Use AuthData to access data.

### AASToken
Use one of the following tools
* [Authenticator](https://github.com/whyorean/Authenticator)
* [AASTokenGrabber](https://github.com/whyorean/AASTokenGrabber)

## Usage

You need to build an instance of `AuthData` to call any method related with the API. Building an
instance requires Email and AASToken fetched from the above-mentioned workflow.

### AuthData 

```kotlin
val authData = AuthHelper.build(email, aastoken)
```

### Fetch App Details

```kotlin
val app = AppDetailsHelper(authData).getAppByPackageName(packageName)
```

### Fetch Bulk App Details

```kotlin
val appList = AppDetailsHelper.getAppByPackageName(packageNameList)
```

### Fetch APKs/OBBs/Patches

```kotlin
val files = PurchaseHelper(authData).purchase(
    app.packageName,
    app.versionCode,
    app.offerType
)
```

### Fetch All Categories

```kotlin
val categoryList = CategoryHelper(authData).getAllCategories(type) //type = GAME or APPLICATION
```

### Fetch Search Suggestions

```kotlin
val entries = SearchHelper(authData).searchSuggestions(query)
```

### Search Apps & Games

```kotlin
var helper = SearchHelper(authData)
var searchBundle = helper.searchResults(query) 

val clusters = searchBundle.clusters // List of AppClusters

clusters.forEach {
    var cluster = it
    cluster.apps.forEach { app ->
        // Do something with each app
    }

    while (cluster.nextPageUrl != null) {
        val nextCluster = helper.nextStream(cluster.nextPageUrl)
        nextCluster.apps.forEach { app ->
            // Do something with each app
        }

        cluster = cluster.copy(
            clusterNextPageUrl = nextCluster.nextPageUrl,
        )
    }
}

// If searchBundle has nextPageUrl, you can fetch next page in similar way
```

### App Reviews

```kotlin
var helper = ReviewsHelper(authData)
var reviewCluster = helper.getReviews(packageName, filter) //filter = ALL, POSITIVE, CRITICAL

// To fetch next list    
reviewCluster = helper.next(reviewCluster.nextPageUrl)
```

### User Reviews

```kotlin
var helper = ReviewsHelper(authData)

//Submit or Edit review
val review = helper.addOrEditReview(packageName, title, content, rating, isBeta)

//Retrive review
val review = helper.getUserReview(packageName, isBeta)
```

## Credits

1. [googleplay-api](https://github.com/egirault/googleplay-api)
2. [google-play-crawler](https://github.com/Akdeniz/google-play-crawler)
3. [play-store-api](https://github.com/yeriomin/play-store-api)
4. [raccon4](https://github.com/onyxbits/raccoon4)
