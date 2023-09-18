package com.kyle.mhtviewer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var openFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedFilePath: String
    private lateinit var webView: WebView
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)

        // WebView 설정
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // 런처 설정
        openFolderLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    // 파일 경로를 가져와서 MHT 파일 읽기 및 WebView에 표시
                    selectedFilePath = getPathFromUri(uri)
                    loadMHTFile(selectedFilePath)
                }
            }
        }

        // 파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // 파일 읽기 권한이 있을 때 다운로드 폴더 열기
            openDownloadFolder()
        }
    }

    private fun openDownloadFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        openFolderLauncher.launch(intent)
    }

    private fun getPathFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        val path: String
        if (cursor != null) {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            path = cursor.getString(index)
            cursor.close()
        } else {
            path = uri.path.toString()
        }
        return path
    }

    private fun loadMHTFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val htmlContent = file.readText()
                webView.loadDataWithBaseURL(null, htmlContent, "multipart/related", "UTF-8", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


