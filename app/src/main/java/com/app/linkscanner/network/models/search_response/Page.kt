package com.app.linkscanner.network.models.search_response

data class Page (

	val country : String,
	val server : String,
	val city : String,
	val domain : String,
	val ip : String,
	val asnname : String,
	val asn : String,
	val url : String,
	val ptr : String
)