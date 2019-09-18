package com.app.linkscanner.network.models.scan_response

data class Certificates (

	val subjectName : String,
	val issuer : String,
	val validFrom : Int,
	val validTo : Int
)