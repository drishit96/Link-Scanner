package com.app.linkscanner.network.models.scan_response

data class Task (

	val uuid : String,
	val time : String,
	val url : String,
	val visibility : String,
	val options : Options,
	val method : String,
	val source : String,
	val userAgent : String,
	val reportURL : String,
	val screenshotURL : String,
	val domURL : String
)