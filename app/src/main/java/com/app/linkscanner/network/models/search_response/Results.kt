package com.app.linkscanner.network.models.search_response

data class Results (

	val task : Task,
	val stats : Stats,
	val page : Page,
	val uniq_countries : Int,
	val _id : String,
	val result : String
)