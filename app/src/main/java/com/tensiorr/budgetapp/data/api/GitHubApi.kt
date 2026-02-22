package com.tensiorr.budgetapp.data.api

import com.tensiorr.budgetapp.data.api.model.GitHubRelease
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * GitHub API interface for checking app updates.
 */
interface GitHubApi {

    /**
     * Fetches the latest release from GitHub repository.
     *
     * @param owner Repository owner (GitHub username)
     * @param repo Repository name
     * @return Latest release information
     */
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease

    companion object {
        const val BASE_URL = "https://api.github.com/"
        const val REPO_OWNER = "Tensiorr"
        const val REPO_NAME = "BudgetApp"
    }
}