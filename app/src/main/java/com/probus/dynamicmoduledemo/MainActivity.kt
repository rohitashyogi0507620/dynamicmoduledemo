package com.probus.dynamicmoduledemo

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus


class MainActivity : AppCompatActivity() {

    lateinit var splitInstallManager: SplitInstallManager
    private var mySessionID = 0
    private val TAG = "OnDemandWork"

    lateinit var btnDownload: Button
    lateinit var progresss: ProgressBar

    var splitInstallStateUpdatedListener =
        SplitInstallStateUpdatedListener { state: SplitInstallSessionState ->
            if (state.sessionId() == mySessionID) {
                when (state.status()) {
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION ->                             // Large module that has size greater than 10 MB requires user permission
                        try {
                            splitInstallManager.startConfirmationDialogForResult(state, this, 110)
                        } catch (ex: SendIntentException) {
                            // Request failed
                        }
                    SplitInstallSessionStatus.DOWNLOADING -> {
                        Log.i(TAG, "Downloading")
                        progresss.visibility = View.VISIBLE
                        // The module is being downloaded
                        val totalBytes = state.totalBytesToDownload().toInt()
                        val progress = state.bytesDownloaded().toInt()
                    }
                    SplitInstallSessionStatus.INSTALLING -> Log.i(TAG, "Installing")
                    SplitInstallSessionStatus.DOWNLOADED -> {
                        Log.i(TAG, "Downloaded")
                        Toast.makeText(this, "Module Downloaded", Toast.LENGTH_SHORT).show()
                    }
                    SplitInstallSessionStatus.INSTALLED -> {
                        Log.i(TAG, "Installed")
                        // Use the below line to call your feature module activity
                        Toast.makeText(this, "Module Installed", Toast.LENGTH_SHORT).show()
                        progresss.visibility = View.GONE

                        startModuleClass()
                    }
                    SplitInstallSessionStatus.CANCELED -> Log.i(TAG, "Canceled")
                    SplitInstallSessionStatus.PENDING -> Log.i(TAG, "Pending")
                    SplitInstallSessionStatus.FAILED -> Log.i(TAG, "Failed")
                }
            }
        }

    private fun startModuleClass() {
        val intent = Intent()
        intent.setClassName(applicationContext, "com.probus.dynamicposp.BecomePospActivity")
        intent.putExtra("id", "12354")
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        splitInstallManager = SplitInstallManagerFactory.create(this)
        splitInstallManager.registerListener(splitInstallStateUpdatedListener)

        btnDownload = findViewById(R.id.btnbecomeposp)
        progresss = findViewById(R.id.progress_circular)
        btnDownload.setOnClickListener { v -> onClickDownloadFeatureModule() } // Using JAVA_8


    }

    fun onClickDownloadFeatureModule() {
        if (!splitInstallManager.installedModules.contains("dynamicposp")) {
            val splitInstallRequest = SplitInstallRequest.newBuilder()
                .addModule("dynamicposp")
                .build()
            splitInstallManager.startInstall(splitInstallRequest)
                .addOnSuccessListener { result: Int? ->
                    mySessionID = result!!
                }
                .addOnFailureListener { e: Exception ->
                    Log.i(TAG, "installManager: $e")
                }

        } else {
            startModuleClass()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 110) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: Install Approved ")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}