package com.app.linkscanner.network.models.scan_response

data class Urlscan(

    val score: Int,
    val categories: List<String>,
    val brands: List<Brands>?,
    val tags: List<String>?,
    val detectionDetails: List<String>?,
    val malicious: Boolean
)