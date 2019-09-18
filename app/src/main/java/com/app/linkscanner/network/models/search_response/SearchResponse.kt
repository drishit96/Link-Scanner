package com.app.linkscanner.network.models.search_response

data class SearchResponse (

	val results : List<Results>,
	val total : Int
)