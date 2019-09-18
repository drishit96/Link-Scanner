package com.app.linkscanner.scanner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.core.view.setMargins
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.linkscanner.R
import androidx.databinding.DataBindingUtil
import com.app.linkscanner.databinding.ActivityScannerBinding
import com.app.linkscanner.network.models.scan_response.EngineVerdict
import com.app.linkscanner.scanner.ScannerViewModel.Companion.GOOGLE_SAFE_BROWSING
import com.app.linkscanner.scanner.ScannerViewModel.Companion.OPENPHISH
import com.app.linkscanner.scanner.ScannerViewModel.Companion.PHISHTANK
import com.app.linkscanner.scanner.ScannerViewModel.Companion.URL_SCAN_IO
import org.jetbrains.anko.dimen
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor

class ScannerActivity : AppCompatActivity() {

    private var originalLink: String? = ""
    private lateinit var scannerViewModel: ScannerViewModel
    private lateinit var binding: ActivityScannerBinding
    private var row: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner)
        scannerViewModel = ViewModelProviders.of(this).get(ScannerViewModel::class.java)
        binding.scannerViewModel = scannerViewModel
        binding.lifecycleOwner = this

        if (intent.data != null) {
            originalLink = intent?.data?.toString()
            scannerViewModel.getRootDomain(intent?.data?.toString())
            intent.data = null
        } else {
            originalLink = null
        }

        scannerViewModel.domain.observe(this, Observer { domain ->
            scannerViewModel.getManualOrAPIScan(domain)
        })

        scannerViewModel.basicDetailsFetched.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {basicDetailsFetched ->
                if (basicDetailsFetched) scannerViewModel.getAutomaticSourceScan(scannerViewModel.domain.value!!)
                binding.fabContinueToSite.show()
            }
        })

        scannerViewModel.gsbVerdict.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { gsbVerdict ->
                addNewResultRow(gsbVerdict)
                scannerViewModel.gsbVerdict.removeObservers(this)
            }
        })

        scannerViewModel.engineVerdict.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { engineVerdict -> addNewResultRow(engineVerdict) }
        })
    }

    fun openLinkInBrowser(view: View) {
        if (originalLink != null) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(originalLink)))
        finish()
    }

    private fun addNewResultRow(engineVerdict: EngineVerdict) {
        val scanResultsGrid = binding.scanResultsGrid
        val engineIdentityContainer = LinearLayout(applicationContext)
        engineIdentityContainer.orientation = LinearLayout.HORIZONTAL
        engineIdentityContainer.setPadding(
            dimen(R.dimen.activity_horizontal_width),
            dimen(R.dimen.activity_vertical_height),
            dimen(R.dimen.activity_horizontal_width),
            dimen(R.dimen.activity_half_vertical_height)
        )

        val engineLogo = ImageView(applicationContext)
        engineLogo.layoutParams =
            ViewGroup.LayoutParams(dimen(R.dimen.engine_icon_size), dimen(R.dimen.engine_icon_size))
        engineLogo.contentDescription = engineVerdict.engine + "logo"
        engineLogo.setImageDrawable(
            when (engineVerdict.engine) {
                "urlscan.io" -> getDrawable(R.drawable.urlscan_logo)
                else -> getDrawable(R.drawable.ic_search)
            }
        )

        val engineName = TextView(applicationContext)
        val layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(dimen(R.dimen.activity_half_horizontal_width), 0, 0, 0)
        engineName.layoutParams = layoutParams
        engineName.text = when (engineVerdict.engine) {
            URL_SCAN_IO -> "urlscan.io"
            PHISHTANK -> "PhishTank"
            OPENPHISH -> "OpenPhish"
            GOOGLE_SAFE_BROWSING -> "Google Safe Browsing"
            else -> "Engine"
        }
        engineName.textColor = ContextCompat.getColor(applicationContext, R.color.textPrimary)

        engineIdentityContainer.addView(engineLogo)
        engineIdentityContainer.addView(engineName)

        val engineIdentityParams = GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(0, 1))
        engineIdentityContainer.layoutParams = engineIdentityParams
        scanResultsGrid.addView(engineIdentityContainer)

        val result = TextView(applicationContext)
        if (engineVerdict.malicious) {
            result.text = getString(R.string.malicious)
            result.textColor = ContextCompat.getColor(applicationContext, R.color.red)
        } else {
            result.text = getString(R.string.safe)
            result.textColor = ContextCompat.getColor(applicationContext, R.color.colorSecondaryAccent)
        }

        val resultLayoutParams = GridLayout.LayoutParams(GridLayout.spec(row, 1), GridLayout.spec(1, 1))
        scanResultsGrid.addView(result)
        resultLayoutParams.setGravity(Gravity.CENTER)
        result.layoutParams = resultLayoutParams

        row = row.plus(1)
    }
}
