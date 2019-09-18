package com.app.linkscanner.network.models.scan_response

data class EngineVerdict(

    val createdAt: String?,
    val engine: String,
    val malicious: Boolean,
    val score: Int,
    val categories: List<String>,
    val comment: String?
)