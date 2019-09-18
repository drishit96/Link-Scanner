package com.app.linkscanner.network.models.scan_response

data class Overall(

    val score: Int,
    val categories: List<String>,
    val brands: List<String>,
    val tags: List<String>,
    val malicious: Boolean,
    val hasVerdicts: Int
)