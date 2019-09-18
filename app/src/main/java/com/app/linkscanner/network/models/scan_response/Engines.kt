package com.app.linkscanner.network.models.scan_response

data class Engines(

    val score: Int,
    val malicious: List<String>,
    val benign: List<String>,
    val maliciousTotal: Int,
    val benignTotal: Int,
    val verdicts: List<EngineVerdict>,
    val enginesTotal: Int
)