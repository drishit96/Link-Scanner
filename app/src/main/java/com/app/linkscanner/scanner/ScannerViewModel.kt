package com.app.linkscanner.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.app.linkscanner.Event
import com.app.linkscanner.network.LinkScannerAPI
import com.app.linkscanner.network.models.scan_response.EngineVerdict
import com.app.linkscanner.network.models.search_response.Page
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.IDN
import java.util.*
import kotlin.collections.HashMap

class ScannerViewModel : ViewModel() {
    companion object {
        const val URL_SCAN_IO = "urlscan.io"
        const val GOOGLE_SAFE_BROWSING = "googlesafebrowsing"
        const val PHISHTANK = "phishtank"
        const val OPENPHISH = "openphish"
        const val CERT_STREAM_SUSPICIOUS = "certstream-suspicious"
    }

    private val _domain = MutableLiveData<String>()
    val domain: LiveData<String> get() = _domain

    private var enginesScanned = HashMap<String, EngineVerdict?>()

    private val _basicDetails = MutableLiveData<Page>()
    val basicDetails: LiveData<Page> get() = _basicDetails

    private val _basicDetailsFetched = MutableLiveData<Event<Boolean>>()
    val basicDetailsFetched: LiveData<Event<Boolean>> get() = _basicDetailsFetched

    private val _engineVerdict = MutableLiveData<Event<EngineVerdict>>()
    val engineVerdict: LiveData<Event<EngineVerdict>> get() = _engineVerdict

    private val _gsbVerdict = MutableLiveData<Event<EngineVerdict?>>()
    val gsbVerdict: LiveData<Event<EngineVerdict?>> get() = _gsbVerdict

    val countryName = Transformations.map(basicDetails) { details ->
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41

        val firstChar = Character.codePointAt(details.country, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(details.country, 1) - asciiOffset + flagOffset

        val flag = String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        "$flag ${Locale("", details.country).displayCountry}"
    }

    fun getRootDomain(url: String?) {
        doAsync {
            val client = OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(true)
                .build()

            if (url != null) {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isRedirect) {
                    val location = response.header("Location", null)
                    if (isValidURL(location)) getRootDomain(location)
                    else getRootDomain(getURLWithPathsRemoved(url) + location)
                } else {
                    uiThread {
                        _domain.value = IDN.toASCII(extractDomainName(url))
                    }
                }
            }
        }
    }

    private fun isValidURL(link: String?): Boolean {
        return if (link == null) false
        else {
            val validURLRegex =
                Regex("((([A-Za-z]{3,9}:(?://)?)(?:[\\-;:&=+\$,\\w]+@)?[A-Za-z0-9.\\-]+|(?:www\\.|[\\-;:&=+\$,\\w]+@)[A-Za-z0-9.\\-]+)((?:/[+~%/.\\w\\-_]*)?\\??(?:[\\-+=&;%@.\\w_]*)#?(?:[.!/\\\\\\w]*))?)")
            return validURLRegex.matches(link)
        }
    }

    private fun getURLWithPathsRemoved(url: String?): String? {
        if (url == null) return null
        val extractingDomainRegex = Regex("^https?://([^/:?#]+)(?:[/:?#]|$)")
        val urlWithPathsRemoved = extractingDomainRegex.find(url)?.groups?.get(0)?.value
        return urlWithPathsRemoved?.substring(0, urlWithPathsRemoved.length - 1)
    }

    private fun extractDomainName(url: String): String {
        val extractingDomainRegex = Regex("^https?://([^/:?#]+)(?:[/:?#]|$)")
        return extractingDomainRegex.find(url)?.groups?.get(1)?.value ?: ""
    }

    fun getManualOrAPIScan(domain: String) {
        doAsync {
            val searchResponse = LinkScannerAPI.searchService.getSearchResults(
                "domain:$domain",
                1
            ).execute()

            if (searchResponse.isSuccessful) {
                val searchResults = searchResponse.body()?.results
                if (searchResults != null && searchResults.isNotEmpty() && searchResults[0].page.domain == domain) {
                    uiThread {
                        _basicDetails.value = searchResults[0].page
                        _basicDetailsFetched.value = Event(true)
                    }

                    if (!enginesScanned.containsKey(URL_SCAN_IO)) {
                        val uuid = searchResults[0]._id
                        val scanResponse = LinkScannerAPI.scanService.getScanResult(uuid).execute()

                        if (scanResponse.isSuccessful) {
                            val scanResult = scanResponse.body()
                            if (scanResult != null) {
                                uiThread {
                                    val urlScan = scanResult.verdicts.urlscan
                                    val urlScanVerdict = EngineVerdict(
                                        null,
                                        URL_SCAN_IO,
                                        urlScan.malicious,
                                        urlScan.score,
                                        urlScan.categories,
                                        null
                                    )
                                    _engineVerdict.value = Event(urlScanVerdict)
                                    enginesScanned[URL_SCAN_IO] = urlScanVerdict

                                    if (_gsbVerdict.value == null &&
                                        scanResult.verdicts.engines.enginesTotal > 0 &&
                                        !enginesScanned.containsKey(GOOGLE_SAFE_BROWSING)
                                    ) {
                                        val gsbVerdict =
                                            scanResult.verdicts.engines.verdicts.firstOrNull { verdict -> verdict.engine == GOOGLE_SAFE_BROWSING }
                                        _gsbVerdict.value = Event(gsbVerdict)
                                        enginesScanned[GOOGLE_SAFE_BROWSING] = gsbVerdict
                                    } else {
                                        _gsbVerdict.value = Event(enginesScanned[GOOGLE_SAFE_BROWSING])
                                    }
                                }
                            }
                        }
                    } else {
                        uiThread {
                            _engineVerdict.value = Event(enginesScanned[URL_SCAN_IO])
                        }
                    }
                } else {
                    uiThread { _basicDetailsFetched.value = Event(false) }
                }
            } else {
                uiThread { _basicDetailsFetched.value = Event(false) }
            }
        }
    }

    fun getAutomaticSourceScan(domain: String) {
        doAsync {
            val sources = arrayListOf(PHISHTANK, OPENPHISH, CERT_STREAM_SUSPICIOUS)
            for (source in sources) {
                if (!enginesScanned.containsKey(source)) {
                    val searchResponse = LinkScannerAPI.searchService.getSearchResults(
                        "domain:$domain AND task.source:$source",
                        1
                    ).execute()

                    if (searchResponse.isSuccessful) {
                        val searchResult = searchResponse.body()
                        if (searchResult != null) {
                            if (searchResult.results.isNotEmpty() &&
                                searchResult.results[0].page.domain == domain
                            ) {
                                val uuid = searchResult.results[0]._id
                                val scanResponse = LinkScannerAPI.scanService.getScanResult(uuid).execute()

                                if (scanResponse.isSuccessful) {
                                    val scanResult = scanResponse.body()
                                    if (scanResult != null) {
                                        uiThread {
                                            val sourceVerdict =
                                                scanResult.verdicts.engines.verdicts.firstOrNull { verdict -> verdict.engine == source }
                                            _engineVerdict.value = Event(sourceVerdict)
                                            enginesScanned[source] = sourceVerdict

                                            if (_gsbVerdict.value == null &&
                                                scanResult.verdicts.engines.enginesTotal > 1 &&
                                                !enginesScanned.containsKey(GOOGLE_SAFE_BROWSING)
                                            ) {
                                                val gsbVerdict =
                                                    scanResult.verdicts.engines.verdicts.firstOrNull { verdict -> verdict.engine == GOOGLE_SAFE_BROWSING }
                                                _gsbVerdict.value = Event(gsbVerdict)
                                                enginesScanned[GOOGLE_SAFE_BROWSING] = gsbVerdict
                                            } else {
                                                _gsbVerdict.value = Event(enginesScanned[GOOGLE_SAFE_BROWSING])
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    uiThread {
                        _engineVerdict.value = Event(enginesScanned[source])
                    }
                }
            }
        }
    }
}