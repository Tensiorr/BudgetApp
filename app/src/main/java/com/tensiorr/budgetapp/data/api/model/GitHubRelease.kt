package com.tensiorr.budgetapp.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub Release API response model.
 */
data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("published_at")
    val publishedAt: String,

    @SerializedName("assets")
    val assets: List<ReleaseAsset>,

    @SerializedName("prerelease")
    val prerelease: Boolean = false
)

/**
 * GitHub Release Asset (APK file).
 */
data class ReleaseAsset(
    @SerializedName("name")
    val name: String,

    @SerializedName("browser_download_url")
    val browserDownloadUrl: String,

    @SerializedName("size")
    val size: Int
)