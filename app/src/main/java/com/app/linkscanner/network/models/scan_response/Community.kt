package com.app.linkscanner.network.models.scan_response

data class Community (

	val score : Int,
	val votes : List<String>,
	val votesTotal : Int,
	val votesMalicious : Int,
	val votesBenign : Int,
	val tags : List<String>,
	val categories : List<String>
)