# 🌳 מבנה עץ הפרויקט (Project Tree)
> מתעדכן אוטומטית בכל שינוי עבור AI Context.

```text
.
├── .github
│   └── workflows
│       ├── ai_agent.yml
│       ├── ai_tracker.yml
│       └── release.yml
├── .gitlab
│   ├── issue_templates
│   │   ├── Bug-Report.md
│   │   └── Feature-Request.md
│   └── merge_request_templates
│       └── Merge-Request.md
├── GooglePlayAPI
│   ├── LICENSES
│   │   ├── Apache-2.0.txt
│   │   └── GPL-3.0-or-later.txt
│   ├── gradle
│   │   ├── wrapper
│   │   │   ├── gradle-wrapper.jar
│   │   │   └── gradle-wrapper.properties
│   │   └── libs.versions.toml
│   ├── lib
│   │   ├── src
│   │   │   └── main
│   │   │       ├── java
│   │   │       │   └── com
│   │   │       │       └── aurora
│   │   │       │           └── gplayapi
│   │   │       │               ├── data
│   │   │       │               │   ├── builders
│   │   │       │               │   │   ├── rpc
│   │   │       │               │   │   │   ├── CategoryBuilder.kt
│   │   │       │               │   │   │   ├── DataSafetyBuilder.kt
│   │   │       │               │   │   │   ├── FeaturedStreamBuilder.kt
│   │   │       │               │   │   │   ├── MetadataBuilder.kt
│   │   │       │               │   │   │   ├── NextBundleBuilder.kt
│   │   │       │               │   │   │   ├── NextClusterBuilder.kt
│   │   │       │               │   │   │   ├── RelatedAppsBuilder.kt
│   │   │       │               │   │   │   ├── RpcBuilder.kt
│   │   │       │               │   │   │   ├── SearchQueryBuilder.kt
│   │   │       │               │   │   │   ├── SearchSuggestionQueryBuilder.kt
│   │   │       │               │   │   │   ├── TokenRepository.kt
│   │   │       │               │   │   │   └── TopChartsBuilder.kt
│   │   │       │               │   │   ├── AppBuilder.kt
│   │   │       │               │   │   ├── ArtworkBuilder.kt
│   │   │       │               │   │   ├── BadgeBuilder.kt
│   │   │       │               │   │   ├── RatingBuilder.kt
│   │   │       │               │   │   ├── ReviewBuilder.kt
│   │   │       │               │   │   ├── TestingProgramBuilder.kt
│   │   │       │               │   │   ├── UserProfileBuilder.kt
│   │   │       │               │   │   └── WebAppBuilder.kt
│   │   │       │               │   ├── models
│   │   │       │               │   │   ├── datasafety
│   │   │       │               │   │   │   ├── Data.kt
│   │   │       │               │   │   │   ├── Entry.kt
│   │   │       │               │   │   │   ├── EntryType.kt
│   │   │       │               │   │   │   └── Report.kt
│   │   │       │               │   │   ├── details
│   │   │       │               │   │   │   ├── AppInfo.kt
│   │   │       │               │   │   │   ├── Badge.kt
│   │   │       │               │   │   │   ├── Chip.kt
│   │   │       │               │   │   │   ├── Dependencies.kt
│   │   │       │               │   │   │   ├── DevStream.kt
│   │   │       │               │   │   │   └── TestingProgram.kt
│   │   │       │               │   │   ├── editor
│   │   │       │               │   │   │   ├── EditorChoiceBundle.kt
│   │   │       │               │   │   │   ├── EditorChoiceCluster.kt
│   │   │       │               │   │   │   └── EditorChoiceReason.kt
│   │   │       │               │   │   ├── ActiveDevice.kt
│   │   │       │               │   │   ├── AndroidCheckInResponse.kt
│   │   │       │               │   │   ├── App.kt
│   │   │       │               │   │   ├── Artwork.kt
│   │   │       │               │   │   ├── AuthData.kt
│   │   │       │               │   │   ├── Category.kt
│   │   │       │               │   │   ├── ContentRating.kt
│   │   │       │               │   │   ├── DeveloperInfo.kt
│   │   │       │               │   │   ├── EncodedCertificateSet.kt
│   │   │       │               │   │   ├── PlayFile.kt
│   │   │       │               │   │   ├── PlayResponse.kt
│   │   │       │               │   │   ├── Rating.kt
│   │   │       │               │   │   ├── Review.kt
│   │   │       │               │   │   ├── ReviewCluster.kt
│   │   │       │               │   │   ├── StreamBundle.kt
│   │   │       │               │   │   ├── StreamCluster.kt
│   │   │       │               │   │   ├── Support.kt
│   │   │       │               │   │   ├── Tag.kt
│   │   │       │               │   │   └── UserProfile.kt
│   │   │       │               │   ├── providers
│   │   │       │               │   │   ├── BaseDeviceInfoProvider.kt
│   │   │       │               │   │   ├── DeviceInfoProvider.kt
│   │   │       │               │   │   ├── HeaderProvider.kt
│   │   │       │               │   │   └── ParamProvider.kt
│   │   │       │               │   ├── serializers
│   │   │       │               │   │   ├── LocaleSerializer.kt
│   │   │       │               │   │   └── PropertiesSerializer.kt
│   │   │       │               │   └── verifier
│   │   │       │               │       └── DfeResponseVerifier.kt
│   │   │       │               ├── exceptions
│   │   │       │               │   └── GooglePlayException.kt
│   │   │       │               ├── helpers
│   │   │       │               │   ├── contracts
│   │   │       │               │   │   ├── AppDetailsContract.kt
│   │   │       │               │   │   ├── CategoryContract.kt
│   │   │       │               │   │   ├── CategoryStreamContract.kt
│   │   │       │               │   │   ├── SearchContract.kt
│   │   │       │               │   │   ├── StreamContract.kt
│   │   │       │               │   │   └── TopChartsContract.kt
│   │   │       │               │   ├── web
│   │   │       │               │   │   ├── BaseWebHelper.kt
│   │   │       │               │   │   ├── WebAppDetailsHelper.kt
│   │   │       │               │   │   ├── WebCategoryHelper.kt
│   │   │       │               │   │   ├── WebCategoryStreamHelper.kt
│   │   │       │               │   │   ├── WebClient.kt
│   │   │       │               │   │   ├── WebDataSafetyHelper.kt
│   │   │       │               │   │   ├── WebSearchHelper.kt
│   │   │       │               │   │   ├── WebStreamHelper.kt
│   │   │       │               │   │   └── WebTopChartsHelper.kt
│   │   │       │               │   ├── AppDetailsHelper.kt
│   │   │       │               │   ├── AuthHelper.kt
│   │   │       │               │   ├── BaseHelper.kt
│   │   │       │               │   ├── CategoryHelper.kt
│   │   │       │               │   ├── CategoryStreamHelper.kt
│   │   │       │               │   ├── ClusterHelper.kt
│   │   │       │               │   ├── ExpandedBrowseHelper.kt
│   │   │       │               │   ├── LibraryHelper.kt
│   │   │       │               │   ├── NativeHelper.kt
│   │   │       │               │   ├── PurchaseHelper.kt
│   │   │       │               │   ├── ReviewsHelper.kt
│   │   │       │               │   ├── SearchHelper.kt
│   │   │       │               │   ├── StreamHelper.kt
│   │   │       │               │   ├── TopChartsHelper.kt
│   │   │       │               │   ├── UserProfileHelper.kt
│   │   │       │               │   └── WebAliases.kt
│   │   │       │               ├── network
│   │   │       │               │   ├── DefaultHttpClient.kt
│   │   │       │               │   └── IHttpClient.kt
│   │   │       │               ├── utils
│   │   │       │               │   ├── CategoryUtil.kt
│   │   │       │               │   ├── CertUtil.kt
│   │   │       │               │   ├── Extensions.kt
│   │   │       │               │   └── Util.java
│   │   │       │               ├── Constants.kt
│   │   │       │               ├── DeviceManager.kt
│   │   │       │               ├── GooglePlayApi.kt
│   │   │       │               ├── PatchedAppManager.kt
│   │   │       │               ├── SelfUpdateManager.kt
│   │   │       │               └── WhitelistManager.kt
│   │   │       ├── proto
│   │   │       │   ├── AcquireApp.proto
│   │   │       │   └── GooglePlay.proto
│   │   │       ├── res
│   │   │       │   └── raw
│   │   │       │       ├── gplayapi_bravia_vu2.properties
│   │   │       │       ├── gplayapi_google_kiwi_x86_64.properties
│   │   │       │       ├── gplayapi_hw_mate20.properties
│   │   │       │       ├── gplayapi_mi_a1.properties
│   │   │       │       ├── gplayapi_nk_drx.properties
│   │   │       │       ├── gplayapi_nothing_p1.properties
│   │   │       │       ├── gplayapi_op_8_pro.properties
│   │   │       │       ├── gplayapi_oppo_r17.properties
│   │   │       │       ├── gplayapi_poco_f1.properties
│   │   │       │       ├── gplayapi_px_9_fold.properties
│   │   │       │       ├── gplayapi_px_9a.properties
│   │   │       │       ├── gplayapi_px_tablet.properties
│   │   │       │       ├── gplayapi_rm_5_pro.properties
│   │   │       │       ├── gplayapi_rm_5i.properties
│   │   │       │       ├── gplayapi_rm_7.properties
│   │   │       │       ├── gplayapi_rm_note_12_4g.properties
│   │   │       │       ├── gplayapi_sm_a13_5g.properties
│   │   │       │       ├── gplayapi_sm_f34_5g.properties
│   │   │       │       ├── gplayapi_sm_j5_prime.properties
│   │   │       │       ├── gplayapi_sm_s20_plus.properties
│   │   │       │       ├── gplayapi_sm_s25u.properties
│   │   │       │       ├── gplayapi_xm_11a.properties
│   │   │       │       ├── gplayapi_xp_5_dual.properties
│   │   │       │       └── keep.xml
│   │   │       └── AndroidManifest.xml
│   │   ├── .gitignore
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   ├── sampleapp
│   │   ├── src
│   │   │   └── main
│   │   │       ├── java
│   │   │       │   └── com
│   │   │       │       └── aurora
│   │   │       │           └── sampleapp
│   │   │       │               ├── ui
│   │   │       │               │   └── theme
│   │   │       │               │       └── Theme.kt
│   │   │       │               ├── MainActivity.kt
│   │   │       │               └── MainActivityViewModel.kt
│   │   │       ├── res
│   │   │       │   ├── drawable
│   │   │       │   │   └── ic_launcher_foreground.xml
│   │   │       │   ├── mipmap-anydpi
│   │   │       │   │   └── ic_launcher.xml
│   │   │       │   └── values
│   │   │       │       ├── colors.xml
│   │   │       │       ├── strings.xml
│   │   │       │       └── themes.xml
│   │   │       └── AndroidManifest.xml
│   │   ├── .gitignore
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   ├── .gitignore
│   ├── .gitlab-ci.yml
│   ├── CHANGELOG
│   ├── LICENSE
│   ├── README.md
│   ├── REUSE.toml
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── jitpack.yml
│   └── settings.gradle.kts
├── LICENSES
│   ├── Apache-2.0.txt
│   ├── CC0-1.0.txt
│   └── GPL-3.0-or-later.txt
├── app
│   ├── schemas
│   │   └── com.aurora.store.data.room.AuroraDatabase
│   │       ├── 10.json
│   │       ├── 11.json
│   │       ├── 12.json
│   │       ├── 5.json
│   │       ├── 6.json
│   │       ├── 7.json
│   │       ├── 8.json
│   │       └── 9.json
│   ├── src
│   │   ├── main
│   │   │   ├── aidl
│   │   │   │   └── com
│   │   │   │       └── aurora
│   │   │   │           └── services
│   │   │   │               ├── IPrivilegedCallback.aidl
│   │   │   │               └── IPrivilegedService.aidl
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── aurora
│   │   │   │           ├── extensions
│   │   │   │           │   ├── Any.kt
│   │   │   │           │   ├── App.kt
│   │   │   │           │   ├── Collection.kt
│   │   │   │           │   ├── Context.kt
│   │   │   │           │   ├── Dialog.kt
│   │   │   │           │   ├── InputStream.kt
│   │   │   │           │   ├── Intent.kt
│   │   │   │           │   ├── Number.kt
│   │   │   │           │   ├── PackageInfo.kt
│   │   │   │           │   ├── PackageManager.kt
│   │   │   │           │   ├── Paging.kt
│   │   │   │           │   ├── Platform.kt
│   │   │   │           │   ├── SharedPreferences.kt
│   │   │   │           │   ├── Shimmer.kt
│   │   │   │           │   ├── Signature.kt
│   │   │   │           │   ├── ThemeEngine.kt
│   │   │   │           │   ├── Threading.kt
│   │   │   │           │   ├── Toast.kt
│   │   │   │           │   ├── View.kt
│   │   │   │           │   └── WindowAdaptiveInfo.kt
│   │   │   │           ├── store
│   │   │   │           │   ├── compose
│   │   │   │           │   │   ├── composable
│   │   │   │           │   │   │   ├── app
│   │   │   │           │   │   │   │   ├── AnimatedAppIcon.kt
│   │   │   │           │   │   │   │   ├── AppListItem.kt
│   │   │   │           │   │   │   │   ├── AppUpdateItem.kt
│   │   │   │           │   │   │   │   ├── InstalledAppListItem.kt
│   │   │   │           │   │   │   │   ├── LargeAppListItem.kt
│   │   │   │           │   │   │   │   └── TagListItem.kt
│   │   │   │           │   │   │   ├── details
│   │   │   │           │   │   │   │   ├── ExodusListItem.kt
│   │   │   │           │   │   │   │   ├── RatingListItem.kt
│   │   │   │           │   │   │   │   ├── ReviewListItem.kt
│   │   │   │           │   │   │   │   └── ScreenshotListItem.kt
│   │   │   │           │   │   │   ├── AccountListItem.kt
│   │   │   │           │   │   │   ├── AuroraListItem.kt
│   │   │   │           │   │   │   ├── BlackListItem.kt
│   │   │   │           │   │   │   ├── CategoryItem.kt
│   │   │   │           │   │   │   ├── ContainedLoadingIndicator.kt
│   │   │   │           │   │   │   ├── DeviceListItem.kt
│   │   │   │           │   │   │   ├── DispenserListItem.kt
│   │   │   │           │   │   │   ├── DownloadListItem.kt
│   │   │   │           │   │   │   ├── FavouriteListItem.kt
│   │   │   │           │   │   │   ├── Info.kt
│   │   │   │           │   │   │   ├── InstallerListItem.kt
│   │   │   │           │   │   │   ├── LinkListItem.kt
│   │   │   │           │   │   │   ├── LocaleListItem.kt
│   │   │   │           │   │   │   ├── Logo.kt
│   │   │   │           │   │   │   ├── MicroG.kt
│   │   │   │           │   │   │   ├── PageIndicator.kt
│   │   │   │           │   │   │   ├── PermissionList.kt
│   │   │   │           │   │   │   ├── PermissionListItem.kt
│   │   │   │           │   │   │   ├── Placeholder.kt
│   │   │   │           │   │   │   ├── RemovableListItem.kt
│   │   │   │           │   │   │   ├── ScrollHint.kt
│   │   │   │           │   │   │   ├── SearchSuggestionListItem.kt
│   │   │   │           │   │   │   ├── SectionHeader.kt
│   │   │   │           │   │   │   ├── Shimmer.kt
│   │   │   │           │   │   │   ├── StreamCarousel.kt
│   │   │   │           │   │   │   ├── TextDividerComposable.kt
│   │   │   │           │   │   │   ├── TopAppBar.kt
│   │   │   │           │   │   │   └── TrackerUpdateWarningDialog.kt
│   │   │   │           │   │   ├── composition
│   │   │   │           │   │   │   ├── LocalNetworkStatus.kt
│   │   │   │           │   │   │   └── LocalUI.kt
│   │   │   │           │   │   ├── navigation
│   │   │   │           │   │   │   ├── Destination.kt
│   │   │   │           │   │   │   ├── NavDisplay.kt
│   │   │   │           │   │   │   └── Screen.kt
│   │   │   │           │   │   ├── preview
│   │   │   │           │   │   │   ├── AppPreviewProvider.kt
│   │   │   │           │   │   │   ├── CoilPreviewProvider.kt
│   │   │   │           │   │   │   ├── FavouritePreviewProvider.kt
│   │   │   │           │   │   │   ├── ReviewPreviewProvider.kt
│   │   │   │           │   │   │   └── ThemePreviewProvider.kt
│   │   │   │           │   │   ├── theme
│   │   │   │           │   │   │   ├── Color.kt
│   │   │   │           │   │   │   └── Theme.kt
│   │   │   │           │   │   └── ui
│   │   │   │           │   │       ├── about
│   │   │   │           │   │       │   ├── AboutDialog.kt
│   │   │   │           │   │       │   └── AboutScreen.kt
│   │   │   │           │   │       ├── accounts
│   │   │   │           │   │       │   ├── AccountsScreen.kt
│   │   │   │           │   │       │   └── GoogleLoginScreen.kt
│   │   │   │           │   │       ├── apps
│   │   │   │           │   │       │   ├── AppsGamesScreen.kt
│   │   │   │           │   │       │   ├── CategoriesPage.kt
│   │   │   │           │   │       │   ├── ForYouPage.kt
│   │   │   │           │   │       │   └── TopChartsPage.kt
│   │   │   │           │   │       ├── blacklist
│   │   │   │           │   │       │   ├── menu
│   │   │   │           │   │       │   │   ├── BlacklistMenu.kt
│   │   │   │           │   │       │   │   └── MenuItem.kt
│   │   │   │           │   │       │   └── BlacklistScreen.kt
│   │   │   │           │   │       ├── commons
│   │   │   │           │   │       │   ├── CategoryBrowseScreen.kt
│   │   │   │           │   │       │   ├── ExpandedStreamBrowseScreen.kt
│   │   │   │           │   │       │   ├── ForceRestartDialog.kt
│   │   │   │           │   │       │   ├── InstallFavouritesDialog.kt
│   │   │   │           │   │       │   ├── LoadingDialog.kt
│   │   │   │           │   │       │   ├── MicroGInstallerPrerequisiteDialog.kt
│   │   │   │           │   │       │   ├── MoreSheet.kt
│   │   │   │           │   │       │   ├── NetworkScreen.kt
│   │   │   │           │   │       │   ├── PermissionRationaleScreen.kt
│   │   │   │           │   │       │   ├── SortFilter.kt
│   │   │   │           │   │       │   ├── SortFilterSheet.kt
│   │   │   │           │   │       │   └── StreamBrowseScreen.kt
│   │   │   │           │   │       ├── details
│   │   │   │           │   │       │   ├── composable
│   │   │   │           │   │       │   │   ├── Actions.kt
│   │   │   │           │   │       │   │   ├── Changelog.kt
│   │   │   │           │   │       │   │   ├── Compatibility.kt
│   │   │   │           │   │       │   │   ├── DataSafety.kt
│   │   │   │           │   │       │   │   ├── Details.kt
│   │   │   │           │   │       │   │   ├── DeveloperDetails.kt
│   │   │   │           │   │       │   │   ├── Privacy.kt
│   │   │   │           │   │       │   │   ├── RatingAndReviews.kt
│   │   │   │           │   │       │   │   ├── Screenshots.kt
│   │   │   │           │   │       │   │   ├── Tags.kt
│   │   │   │           │   │       │   │   ├── Testing.kt
│   │   │   │           │   │       │   │   └── UserReview.kt
│   │   │   │           │   │       │   ├── menu
│   │   │   │           │   │       │   │   ├── AppDetailsMenu.kt
│   │   │   │           │   │       │   │   └── MenuItem.kt
│   │   │   │           │   │       │   ├── navigation
│   │   │   │           │   │       │   │   └── ExtraScreen.kt
│   │   │   │           │   │       │   ├── AppDetailsScreen.kt
│   │   │   │           │   │       │   ├── ExodusScreen.kt
│   │   │   │           │   │       │   ├── ManualDownloadScreen.kt
│   │   │   │           │   │       │   ├── MicroGScreen.kt
│   │   │   │           │   │       │   ├── MoreScreen.kt
│   │   │   │           │   │       │   ├── PermissionScreen.kt
│   │   │   │           │   │       │   ├── ReviewScreen.kt
│   │   │   │           │   │       │   └── ScreenshotScreen.kt
│   │   │   │           │   │       ├── dev
│   │   │   │           │   │       │   └── DevProfileScreen.kt
│   │   │   │           │   │       ├── dialog
│   │   │   │           │   │       │   └── UpdateAvailableDialog.kt
│   │   │   │           │   │       ├── dispenser
│   │   │   │           │   │       │   ├── DispenserScreen.kt
│   │   │   │           │   │       │   ├── InputDispenserDialog.kt
│   │   │   │           │   │       │   └── RemoveDispenserDialog.kt
│   │   │   │           │   │       ├── downloads
│   │   │   │           │   │       │   ├── menu
│   │   │   │           │   │       │   │   ├── DownloadsMenu.kt
│   │   │   │           │   │       │   │   └── MenuItem.kt
│   │   │   │           │   │       │   └── DownloadsScreen.kt
│   │   │   │           │   │       ├── favourite
│   │   │   │           │   │       │   ├── menu
│   │   │   │           │   │       │   │   ├── FavouriteMenu.kt
│   │   │   │           │   │       │   │   └── MenuItem.kt
│   │   │   │           │   │       │   └── FavouriteScreen.kt
│   │   │   │           │   │       ├── installed
│   │   │   │           │   │       │   └── InstalledScreen.kt
│   │   │   │           │   │       ├── lock
│   │   │   │           │   │       │   └── AppLockScreen.kt
│   │   │   │           │   │       ├── main
│   │   │   │           │   │       │   └── MainScreen.kt
│   │   │   │           │   │       ├── onboarding
│   │   │   │           │   │       │   ├── navigation
│   │   │   │           │   │       │   │   └── OnboardingPage.kt
│   │   │   │           │   │       │   ├── MicroGPage.kt
│   │   │   │           │   │       │   ├── OnboardingScreen.kt
│   │   │   │           │   │       │   ├── PermissionsPage.kt
│   │   │   │           │   │       │   └── WelcomePage.kt
│   │   │   │           │   │       ├── preferences
│   │   │   │           │   │       │   ├── installation
│   │   │   │           │   │       │   │   ├── InstallationPreferenceScreen.kt
│   │   │   │           │   │       │   │   └── InstallerScreen.kt
│   │   │   │           │   │       │   ├── network
│   │   │   │           │   │       │   │   └── NetworkPreferenceScreen.kt
│   │   │   │           │   │       │   ├── security
│   │   │   │           │   │       │   │   └── SecurityPreferenceScreen.kt
│   │   │   │           │   │       │   ├── updates
│   │   │   │           │   │       │   │   ├── SourceFiltersScreen.kt
│   │   │   │           │   │       │   │   └── UpdatesPreferenceScreen.kt
│   │   │   │           │   │       │   ├── NotificationPreferenceScreen.kt
│   │   │   │           │   │       │   ├── SettingsScreen.kt
│   │   │   │           │   │       │   └── UIPreferenceScreen.kt
│   │   │   │           │   │       ├── search
│   │   │   │           │   │       │   └── SearchScreen.kt
│   │   │   │           │   │       ├── sheets
│   │   │   │           │   │       │   ├── AccountActionsSheet.kt
│   │   │   │           │   │       │   ├── AccountPickerSheet.kt
│   │   │   │           │   │       │   ├── AppUpdateSheet.kt
│   │   │   │           │   │       │   ├── DeepLinkConfirmSheet.kt
│   │   │   │           │   │       │   ├── DownloadActionsSheet.kt
│   │   │   │           │   │       │   ├── InstallErrorSheet.kt
│   │   │   │           │   │       │   └── VersionPickerSheet.kt
│   │   │   │           │   │       ├── splash
│   │   │   │           │   │       │   └── SplashScreen.kt
│   │   │   │           │   │       ├── spoof
│   │   │   │           │   │       │   ├── menu
│   │   │   │           │   │       │   │   ├── MenuItem.kt
│   │   │   │           │   │       │   │   └── SpoofMenu.kt
│   │   │   │           │   │       │   ├── navigation
│   │   │   │           │   │       │   │   └── SpoofPage.kt
│   │   │   │           │   │       │   ├── DevicePage.kt
│   │   │   │           │   │       │   ├── LocalePage.kt
│   │   │   │           │   │       │   └── SpoofScreen.kt
│   │   │   │           │   │       └── updates
│   │   │   │           │   │           └── UpdatesScreen.kt
│   │   │   │           │   ├── data
│   │   │   │           │   │   ├── activity
│   │   │   │           │   │   │   ├── InstallActivity.kt
│   │   │   │           │   │   │   └── MicroGInstallerActivity.kt
│   │   │   │           │   │   ├── event
│   │   │   │           │   │   │   ├── BusEvent.kt
│   │   │   │           │   │   │   └── EventFlow.kt
│   │   │   │           │   │   ├── helper
│   │   │   │           │   │   │   ├── DownloadHelper.kt
│   │   │   │           │   │   │   └── UpdateHelper.kt
│   │   │   │           │   │   ├── installer
│   │   │   │           │   │   │   ├── base
│   │   │   │           │   │   │   │   ├── IInstaller.kt
│   │   │   │           │   │   │   │   └── InstallerBase.kt
│   │   │   │           │   │   │   ├── AMInstaller.kt
│   │   │   │           │   │   │   ├── AppInstaller.kt
│   │   │   │           │   │   │   ├── MicroGInstaller.kt
│   │   │   │           │   │   │   ├── NativeInstaller.kt
│   │   │   │           │   │   │   ├── RootInstaller.kt
│   │   │   │           │   │   │   ├── ServiceInstaller.kt
│   │   │   │           │   │   │   ├── SessionInstaller.kt
│   │   │   │           │   │   │   └── ShizukuInstaller.kt
│   │   │   │           │   │   ├── model
│   │   │   │           │   │   │   ├── AccountType.kt
│   │   │   │           │   │   │   ├── Algorithm.kt
│   │   │   │           │   │   │   ├── Auth.kt
│   │   │   │           │   │   │   ├── Black.kt
│   │   │   │           │   │   │   ├── BlacklistAppItem.kt
│   │   │   │           │   │   │   ├── BuildType.kt
│   │   │   │           │   │   │   ├── DownloadInfo.kt
│   │   │   │           │   │   │   ├── DownloadStatus.kt
│   │   │   │           │   │   │   ├── Exodus.kt
│   │   │   │           │   │   │   ├── ExternalItem.kt
│   │   │   │           │   │   │   ├── InstallStatus.kt
│   │   │   │           │   │   │   ├── Installer.kt
│   │   │   │           │   │   │   ├── InstallerInfo.kt
│   │   │   │           │   │   │   ├── Link.kt
│   │   │   │           │   │   │   ├── NetworkStatus.kt
│   │   │   │           │   │   │   ├── PaginatedAppList.kt
│   │   │   │           │   │   │   ├── Permission.kt
│   │   │   │           │   │   │   ├── PermissionType.kt
│   │   │   │           │   │   │   ├── Plexus.kt
│   │   │   │           │   │   │   ├── ProxyInfo.kt
│   │   │   │           │   │   │   ├── SearchFilter.kt
│   │   │   │           │   │   │   ├── SelfUpdate.kt
│   │   │   │           │   │   │   ├── SessionInfo.kt
│   │   │   │           │   │   │   ├── State.kt
│   │   │   │           │   │   │   └── UpdateMode.kt
│   │   │   │           │   │   ├── network
│   │   │   │           │   │   │   ├── HttpClient.kt
│   │   │   │           │   │   │   ├── IHttpClientModule.kt
│   │   │   │           │   │   │   └── OkHttpClientModule.kt
│   │   │   │           │   │   ├── paging
│   │   │   │           │   │   │   └── GenericPagingSource.kt
│   │   │   │           │   │   ├── providers
│   │   │   │           │   │   │   ├── AccountProvider.kt
│   │   │   │           │   │   │   ├── AuthProvider.kt
│   │   │   │           │   │   │   ├── BlacklistProvider.kt
│   │   │   │           │   │   │   ├── EglExtensionProvider.kt
│   │   │   │           │   │   │   ├── GoogleAccountTokenProvider.kt
│   │   │   │           │   │   │   ├── NativeDeviceInfoProvider.kt
│   │   │   │           │   │   │   ├── NativeGsfVersionProvider.kt
│   │   │   │           │   │   │   ├── NetworkProvider.kt
│   │   │   │           │   │   │   ├── PermissionProvider.kt
│   │   │   │           │   │   │   ├── SpoofDeviceProvider.kt
│   │   │   │           │   │   │   └── SpoofProvider.kt
│   │   │   │           │   │   ├── receiver
│   │   │   │           │   │   │   ├── BaseInstallerStatusReceiver.kt
│   │   │   │           │   │   │   ├── DeviceOwnerReceiver.kt
│   │   │   │           │   │   │   ├── DownloadCancelReceiver.kt
│   │   │   │           │   │   │   ├── DownloadRetryReceiver.kt
│   │   │   │           │   │   │   ├── MigrationReceiver.kt
│   │   │   │           │   │   │   ├── NetworkBroadcastReceiver.kt
│   │   │   │           │   │   │   ├── PackageManagerReceiver.kt
│   │   │   │           │   │   │   └── UnarchivePackageReceiver.kt
│   │   │   │           │   │   ├── room
│   │   │   │           │   │   │   ├── account
│   │   │   │           │   │   │   │   ├── Account.kt
│   │   │   │           │   │   │   │   ├── AccountConverter.kt
│   │   │   │           │   │   │   │   ├── AccountDao.kt
│   │   │   │           │   │   │   │   ├── AppAccountBinding.kt
│   │   │   │           │   │   │   │   └── AppAccountBindingDao.kt
│   │   │   │           │   │   │   ├── download
│   │   │   │           │   │   │   │   ├── Download.kt
│   │   │   │           │   │   │   │   ├── DownloadConverter.kt
│   │   │   │           │   │   │   │   ├── DownloadDao.kt
│   │   │   │           │   │   │   │   └── SharedLib.kt
│   │   │   │           │   │   │   ├── exodus
│   │   │   │           │   │   │   │   ├── TrackerDao.kt
│   │   │   │           │   │   │   │   └── TrackerEntity.kt
│   │   │   │           │   │   │   ├── favourite
│   │   │   │           │   │   │   │   ├── Favourite.kt
│   │   │   │           │   │   │   │   ├── FavouriteDao.kt
│   │   │   │           │   │   │   │   └── ImportExport.kt
│   │   │   │           │   │   │   ├── review
│   │   │   │           │   │   │   │   ├── LocalReview.kt
│   │   │   │           │   │   │   │   └── ReviewDao.kt
│   │   │   │           │   │   │   ├── suite
│   │   │   │           │   │   │   │   └── ExternalApk.kt
│   │   │   │           │   │   │   ├── update
│   │   │   │           │   │   │   │   ├── IgnoredUpdate.kt
│   │   │   │           │   │   │   │   ├── IgnoredUpdateDao.kt
│   │   │   │           │   │   │   │   ├── Update.kt
│   │   │   │           │   │   │   │   └── UpdateDao.kt
│   │   │   │           │   │   │   ├── AuroraDatabase.kt
│   │   │   │           │   │   │   ├── MigrationHelper.kt
│   │   │   │           │   │   │   └── RoomModule.kt
│   │   │   │           │   │   ├── work
│   │   │   │           │   │   │   ├── AuthWorker.kt
│   │   │   │           │   │   │   ├── CacheWorker.kt
│   │   │   │           │   │   │   ├── DownloadWorker.kt
│   │   │   │           │   │   │   ├── ExodusTrackerWorker.kt
│   │   │   │           │   │   │   ├── ExportWorker.kt
│   │   │   │           │   │   │   └── UpdateWorker.kt
│   │   │   │           │   │   ├── AccountRepository.kt
│   │   │   │           │   │   ├── AppLockManager.kt
│   │   │   │           │   │   ├── ExodusRepository.kt
│   │   │   │           │   │   └── PageResult.kt
│   │   │   │           │   ├── module
│   │   │   │           │   │   ├── CommonModule.kt
│   │   │   │           │   │   └── HelperModule.kt
│   │   │   │           │   ├── util
│   │   │   │           │   │   ├── AC2DMTask.kt
│   │   │   │           │   │   ├── AC2DMUtil.java
│   │   │   │           │   │   ├── AppLockAuthenticator.kt
│   │   │   │           │   │   ├── AppSelfUpdater.kt
│   │   │   │           │   │   ├── CertUtil.kt
│   │   │   │           │   │   ├── CommonUtil.kt
│   │   │   │           │   │   ├── IFlavouredUtil.kt
│   │   │   │           │   │   ├── NotificationUtil.kt
│   │   │   │           │   │   ├── PackageUtil.kt
│   │   │   │           │   │   ├── PathUtil.kt
│   │   │   │           │   │   ├── Preferences.kt
│   │   │   │           │   │   ├── RestartUtil.kt
│   │   │   │           │   │   └── ShortcutManagerUtil.kt
│   │   │   │           │   ├── viewmodel
│   │   │   │           │   │   ├── accounts
│   │   │   │           │   │   │   └── AccountsViewModel.kt
│   │   │   │           │   │   ├── all
│   │   │   │           │   │   │   ├── FavouriteViewModel.kt
│   │   │   │           │   │   │   ├── InstalledViewModel.kt
│   │   │   │           │   │   │   └── UpdatesViewModel.kt
│   │   │   │           │   │   ├── apps
│   │   │   │           │   │   │   └── AppsContainerViewModel.kt
│   │   │   │           │   │   ├── auth
│   │   │   │           │   │   │   └── AuthViewModel.kt
│   │   │   │           │   │   ├── blacklist
│   │   │   │           │   │   │   └── BlacklistViewModel.kt
│   │   │   │           │   │   ├── browse
│   │   │   │           │   │   │   ├── ExpandedStreamBrowseViewModel.kt
│   │   │   │           │   │   │   └── StreamBrowseViewModel.kt
│   │   │   │           │   │   ├── category
│   │   │   │           │   │   │   └── CategoryViewModel.kt
│   │   │   │           │   │   ├── commons
│   │   │   │           │   │   │   ├── MoreViewModel.kt
│   │   │   │           │   │   │   └── PermissionRationaleViewModel.kt
│   │   │   │           │   │   ├── details
│   │   │   │           │   │   │   ├── AppDetailsViewModel.kt
│   │   │   │           │   │   │   ├── DevProfileViewModel.kt
│   │   │   │           │   │   │   ├── ExodusViewModel.kt
│   │   │   │           │   │   │   ├── MoreViewModel.kt
│   │   │   │           │   │   │   ├── PermissionViewModel.kt
│   │   │   │           │   │   │   └── ReviewViewModel.kt
│   │   │   │           │   │   ├── dispenser
│   │   │   │           │   │   │   └── DispenserViewModel.kt
│   │   │   │           │   │   ├── downloads
│   │   │   │           │   │   │   └── DownloadsViewModel.kt
│   │   │   │           │   │   ├── games
│   │   │   │           │   │   │   └── GamesContainerViewModel.kt
│   │   │   │           │   │   ├── homestream
│   │   │   │           │   │   │   └── StreamViewModel.kt
│   │   │   │           │   │   ├── onboarding
│   │   │   │           │   │   │   ├── MicroGViewModel.kt
│   │   │   │           │   │   │   └── OnboardingViewModel.kt
│   │   │   │           │   │   ├── preferences
│   │   │   │           │   │   │   ├── InstallerViewModel.kt
│   │   │   │           │   │   │   ├── ProxyURLViewModel.kt
│   │   │   │           │   │   │   └── UpdatesRestrictionsViewModel.kt
│   │   │   │           │   │   ├── search
│   │   │   │           │   │   │   └── SearchViewModel.kt
│   │   │   │           │   │   ├── sheets
│   │   │   │           │   │   │   └── AppUpdateViewModel.kt
│   │   │   │           │   │   ├── spoof
│   │   │   │           │   │   │   └── SpoofViewModel.kt
│   │   │   │           │   │   ├── subcategory
│   │   │   │           │   │   │   └── CategoryStreamViewModel.kt
│   │   │   │           │   │   └── topchart
│   │   │   │           │   │       └── TopChartViewModel.kt
│   │   │   │           │   ├── Aliases.kt
│   │   │   │           │   ├── AuroraApp.kt
│   │   │   │           │   ├── ComposeActivity.kt
│   │   │   │           │   ├── DeepLinkConfirmActivity.kt
│   │   │   │           │   └── MainViewModel.kt
│   │   │   │           └── Constants.kt
│   │   │   ├── res
│   │   │   │   ├── drawable
│   │   │   │   │   ├── bg_changelog.xml
│   │   │   │   │   ├── bg_placeholder.xml
│   │   │   │   │   ├── bg_rounded.xml
│   │   │   │   │   ├── divider.xml
│   │   │   │   │   ├── divider_line.xml
│   │   │   │   │   ├── ic_about.xml
│   │   │   │   │   ├── ic_account.xml
│   │   │   │   │   ├── ic_account_manager.xml
│   │   │   │   │   ├── ic_add.xml
│   │   │   │   │   ├── ic_android.xml
│   │   │   │   │   ├── ic_anonymous.xml
│   │   │   │   │   ├── ic_apk_install.xml
│   │   │   │   │   ├── ic_apps.xml
│   │   │   │   │   ├── ic_apps_outage.xml
│   │   │   │   │   ├── ic_arrow_back.xml
│   │   │   │   │   ├── ic_arrow_down.xml
│   │   │   │   │   ├── ic_arrow_download.xml
│   │   │   │   │   ├── ic_arrow_drop_down.xml
│   │   │   │   │   ├── ic_arrow_forward.xml
│   │   │   │   │   ├── ic_arrow_left.xml
│   │   │   │   │   ├── ic_arrow_right.xml
│   │   │   │   │   ├── ic_arrow_up.xml
│   │   │   │   │   ├── ic_auto.xml
│   │   │   │   │   ├── ic_bhim.xml
│   │   │   │   │   ├── ic_bitcoin_bch.xml
│   │   │   │   │   ├── ic_bitcoin_btc.xml
│   │   │   │   │   ├── ic_blacklist.xml
│   │   │   │   │   ├── ic_campaign.xml
│   │   │   │   │   ├── ic_cancel.xml
│   │   │   │   │   ├── ic_check.xml
│   │   │   │   │   ├── ic_child.xml
│   │   │   │   │   ├── ic_cloud_upload.xml
│   │   │   │   │   ├── ic_code.xml
│   │   │   │   │   ├── ic_dark.xml
│   │   │   │   │   ├── ic_dashboard_black_24dp.xml
│   │   │   │   │   ├── ic_delete_forever.xml
│   │   │   │   │   ├── ic_disclaimer.xml
│   │   │   │   │   ├── ic_disk.xml
│   │   │   │   │   ├── ic_download.xml
│   │   │   │   │   ├── ic_download_cancel.png
│   │   │   │   │   ├── ic_download_fail.png
│   │   │   │   │   ├── ic_download_manager.xml
│   │   │   │   │   ├── ic_download_pause.png
│   │   │   │   │   ├── ic_ethereum_eth.xml
│   │   │   │   │   ├── ic_expand.xml
│   │   │   │   │   ├── ic_experiment.xml
│   │   │   │   │   ├── ic_faq.xml
│   │   │   │   │   ├── ic_favorite_checked.xml
│   │   │   │   │   ├── ic_favorite_unchecked.xml
│   │   │   │   │   ├── ic_fdroid.xml
│   │   │   │   │   ├── ic_file_copy.xml
│   │   │   │   │   ├── ic_filter.xml
│   │   │   │   │   ├── ic_games.xml
│   │   │   │   │   ├── ic_gitlab.xml
│   │   │   │   │   ├── ic_google.xml
│   │   │   │   │   ├── ic_help.xml
│   │   │   │   │   ├── ic_home_black_24dp.xml
│   │   │   │   │   ├── ic_install.xml
│   │   │   │   │   ├── ic_installation.xml
│   │   │   │   │   ├── ic_keyboard_arrow_down.xml
│   │   │   │   │   ├── ic_keyboard_arrow_up.xml
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   ├── ic_launcher_monochrome.xml
│   │   │   │   │   ├── ic_libera_pay.xml
│   │   │   │   │   ├── ic_license.xml
│   │   │   │   │   ├── ic_light.xml
│   │   │   │   │   ├── ic_list_check.xml
│   │   │   │   │   ├── ic_lock.xml
│   │   │   │   │   ├── ic_logo.xml
│   │   │   │   │   ├── ic_logo_alt.xml
│   │   │   │   │   ├── ic_logout.xml
│   │   │   │   │   ├── ic_mail.xml
│   │   │   │   │   ├── ic_map_marker.xml
│   │   │   │   │   ├── ic_menu_about.xml
│   │   │   │   │   ├── ic_menu_settings.xml
│   │   │   │   │   ├── ic_menu_unfold.xml
│   │   │   │   │   ├── ic_more_vert.xml
│   │   │   │   │   ├── ic_network.xml
│   │   │   │   │   ├── ic_notification_outlined.xml
│   │   │   │   │   ├── ic_notification_settings.xml
│   │   │   │   │   ├── ic_notifications_black_24dp.xml
│   │   │   │   │   ├── ic_paid.xml
│   │   │   │   │   ├── ic_paypal.xml
│   │   │   │   │   ├── ic_permission_android.xml
│   │   │   │   │   ├── ic_permission_google.xml
│   │   │   │   │   ├── ic_permission_unknown.xml
│   │   │   │   │   ├── ic_person_location.xml
│   │   │   │   │   ├── ic_privacy.xml
│   │   │   │   │   ├── ic_problem.xml
│   │   │   │   │   ├── ic_refresh.xml
│   │   │   │   │   ├── ic_round_search.xml
│   │   │   │   │   ├── ic_sale.xml
│   │   │   │   │   ├── ic_scan.xml
│   │   │   │   │   ├── ic_search_append.xml
│   │   │   │   │   ├── ic_search_suggestion.xml
│   │   │   │   │   ├── ic_send.xml
│   │   │   │   │   ├── ic_server.xml
│   │   │   │   │   ├── ic_settings_account.xml
│   │   │   │   │   ├── ic_settings_suggest.xml
│   │   │   │   │   ├── ic_share.xml
│   │   │   │   │   ├── ic_shield.xml
│   │   │   │   │   ├── ic_size.xml
│   │   │   │   │   ├── ic_spoof.xml
│   │   │   │   │   ├── ic_star.xml
│   │   │   │   │   ├── ic_suggestions.xml
│   │   │   │   │   ├── ic_telegram.xml
│   │   │   │   │   ├── ic_transparent.xml
│   │   │   │   │   ├── ic_tune.xml
│   │   │   │   │   ├── ic_tv_banner.xml
│   │   │   │   │   ├── ic_ui.xml
│   │   │   │   │   ├── ic_updates.xml
│   │   │   │   │   ├── ic_visibility.xml
│   │   │   │   │   ├── ic_xda.xml
│   │   │   │   │   ├── ic_xiaomi_logo.xml
│   │   │   │   │   ├── messages.xml
│   │   │   │   │   ├── sync.xml
│   │   │   │   │   ├── tab_default_onboarding.xml
│   │   │   │   │   ├── tab_indicator.xml
│   │   │   │   │   ├── tab_selected_onboarding.xml
│   │   │   │   │   └── tab_selector_onboarding.xml
│   │   │   │   ├── drawable-hdpi
│   │   │   │   │   ├── ic_install.png
│   │   │   │   │   └── ic_notification_outlined.png
│   │   │   │   ├── drawable-mdpi
│   │   │   │   │   ├── ic_install.png
│   │   │   │   │   └── ic_notification_outlined.png
│   │   │   │   ├── drawable-v24
│   │   │   │   │   ├── ic_arrow_left.xml
│   │   │   │   │   └── ic_launcher_foreground.xml
│   │   │   │   ├── drawable-xhdpi
│   │   │   │   │   ├── ic_install.png
│   │   │   │   │   └── ic_notification_outlined.png
│   │   │   │   ├── drawable-xxhdpi
│   │   │   │   │   ├── ic_install.png
│   │   │   │   │   └── ic_notification_outlined.png
│   │   │   │   ├── mipmap-anydpi-v26
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   └── ic_launcher_round.xml
│   │   │   │   ├── mipmap-hdpi
│   │   │   │   │   ├── ic_launcher.png
│   │   │   │   │   └── ic_launcher_round.png
│   │   │   │   ├── mipmap-mdpi
│   │   │   │   │   ├── ic_launcher.png
│   │   │   │   │   └── ic_launcher_round.png
│   │   │   │   ├── mipmap-xhdpi
│   │   │   │   │   ├── ic_launcher.png
│   │   │   │   │   └── ic_launcher_round.png
│   │   │   │   ├── mipmap-xxhdpi
│   │   │   │   │   ├── ic_launcher.png
│   │   │   │   │   └── ic_launcher_round.png
│   │   │   │   ├── mipmap-xxxhdpi
│   │   │   │   │   ├── ic_launcher.png
│   │   │   │   │   └── ic_launcher_round.png
│   │   │   │   ├── raw
│   │   │   │   │   └── google_roots_ca.pem
│   │   │   │   ├── values
│   │   │   │   │   ├── arrays.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-ar
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ast
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-az
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-be
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-bg
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ca
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-cs
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-da
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-de
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-el
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-eo
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-es
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-et
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-eu
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-fa
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-fi
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-fr
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-gl
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-hi
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-hr
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-hu
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-in
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-it
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-iw
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ja
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-kab
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ko
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-kw
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-lo
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-lt
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-lv
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ms
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-night
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-nl
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-pa
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-pl
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-pt
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-pt-rBR
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ro
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ru
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-si
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sk
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sl
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sq
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sr
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sv
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ta
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-tr
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-uk
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ur
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-vi
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-yue
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-zh-rCN
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-zh-rTW
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── xml
│   │   │   │   │   ├── backup_rules.xml
│   │   │   │   │   ├── backup_rules_legacy.xml
│   │   │   │   │   ├── device_owner_receiver.xml
│   │   │   │   │   ├── paths.xml
│   │   │   │   │   └── self_update_file_paths.xml
│   │   │   │   └── resources.properties
│   │   │   ├── AndroidManifest.xml
│   │   │   └── ic_launcher-playstore.png
│   │   └── vanilla
│   │       └── java
│   │           └── com
│   │               └── aurora
│   │                   └── store
│   │                       ├── data
│   │                       │   └── receiver
│   │                       │       └── InstallerStatusReceiver.kt
│   │                       └── util
│   │                           └── FlavouredUtil.kt
│   ├── .gitignore
│   ├── build.gradle.kts
│   ├── lint.xml
│   ├── proguard-rules.pro
│   └── testkey.jks
├── fastlane
│   └── metadata
│       └── android
│           ├── ar
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── az-AZ
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ca
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── cs-CZ
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── da-DK
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── de-DE
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── el-GR
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── en-US
│           │   ├── images
│           │   │   ├── phoneScreenshots
│           │   │   │   ├── screenshot-01.png
│           │   │   │   ├── screenshot-02.png
│           │   │   │   ├── screenshot-03.png
│           │   │   │   ├── screenshot-04.png
│           │   │   │   ├── screenshot-05.png
│           │   │   │   ├── screenshot-06.png
│           │   │   │   ├── screenshot-07.png
│           │   │   │   └── screenshot-08.png
│           │   │   └── icon.png
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── es-ES
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── et
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── fi-FI
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── fr-FR
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── hi
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── hr
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── hu-HU
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── id
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── it-IT
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ja-JP
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ko-KR
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── kw
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── lo-LA
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── lv
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ms-MY
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── pa
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── pl-PL
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── pt
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── pt-BR
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ro
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ru-RU
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── sk
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── sq
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── sr
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── sv-SE
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── ta-IN
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── tr-TR
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── uk
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           ├── zh-CN
│           │   ├── full_description.txt
│           │   └── short_description.txt
│           └── zh-TW
│               ├── full_description.txt
│               └── short_description.txt
├── gradle
│   ├── wrapper
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   ├── gradle-daemon-jvm.properties
│   └── libs.versions.toml
├── .editorconfig
├── .gitignore
├── .gitlab-ci.yml
├── AI_CHANGES.md
├── CHANGELOG
├── DISCLAIMER.md
├── LICENSE
├── POLICY.md
├── PROJECT_TREE.md
├── README.md
├── REUSE.toml
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── summery_for_AI.md
└── updates.json

273 directories, 817 files
```
