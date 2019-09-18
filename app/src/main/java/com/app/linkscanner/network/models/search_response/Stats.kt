package com.app.linkscanner.network.models.search_response

data class Stats (

	val uniqIPs : Int,
	val consoleMsgs : Int,
	val dataLength : Int,
	val encodedDataLength : Int,
	val requests : Int
)