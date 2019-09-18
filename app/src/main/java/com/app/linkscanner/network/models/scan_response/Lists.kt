package com.app.linkscanner.network.models.scan_response

data class Lists (

	val ips : List<String>,
	val countries : List<String>,
	val asns : List<Int>,
	val domains : List<String>,
	val servers : List<String>,
	val urls : List<String>,
	val linkDomains : List<String>,
	val certificates : List<Certificates>,
	val hashes : List<String>
)