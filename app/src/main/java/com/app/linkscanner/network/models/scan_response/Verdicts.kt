package com.app.linkscanner.network.models.scan_response

data class Verdicts (

	val overall : Overall,
	val urlscan : Urlscan,
	val engines : Engines,
	val community : Community
)