package com.app.linkscanner.network.models.scan_response

data class Brands (

	val key : String,
	val name : String,
	val country : List<String>,
	val vertical : List<String>
)