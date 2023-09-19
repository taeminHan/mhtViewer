package com.kyle.mhtviewer

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var openFolderLauncher: ActivityResultLauncher<Intent>
    private val PERMISSION_REQUEST_CODE = 123
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.d("qwe", webView.isActivated.toString())
            // 뒤로 버튼 이벤트 처리
            if(webView.isActivated){
                webView.isActivated = false
                openDownloadFolder()
            }else{
                webView.destroy()
                showExitConfirmationDialog()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webView = findViewById(R.id.webView)
        this.onBackPressedDispatcher.addCallback(this, callback)
        openFolderLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    // 파일 경로를 가져와서 MHT 파일 읽기 및 WebView에 표시
                    loadMHTFile(uri)
                }
            }
        }
        // 파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
            )
        } else {
            // 파일 읽기 권한이 있을 때 다운로드 폴더 열기
            openDownloadFolder()
        }
        val qweIntent = intent.data
        // WebView 설정
        val webSettings = webView.settings
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = true
        webView.setInitialScale(100) // 웹 페이지를 100% 크기로 시작


        if(qweIntent != null){
            loadMHTFile(qweIntent)
        }
    }

    // 권한 요청 결과 처리


    // Base64 디코딩 함수
    private fun loadMHTFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                var line: String?
                var isReadingContent = false
                val contentBuilder = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith("Content-Type: text/html")) {
                        isReadingContent = true
                        continue
                    }
                    if (line!!.startsWith("--BOUNDARY") && isReadingContent) {
                        break
                    }
                    if (isReadingContent) {
                        // "Content-Transfer-Encoding: base64" 헤더 제거
                        if (!line!!.startsWith("Content-Transfer-Encoding: base64")) {
                            // 줄 바꿈 문자를 제거하여 contentBuilder에 추가
                            contentBuilder.append(line?.replace("\n", ""))
                        }
                    }
                }

                val base64EncodedHtml = contentBuilder.toString()
                val decodedData = Base64.decode(base64EncodedHtml, Base64.DEFAULT)
                val decodedHtml = String(decodedData, Charsets.UTF_8)

                // 디코딩된 HTML 콘텐츠를 WebView에 로드합니다.
                webView.isActivated = true
                webView.loadDataWithBaseURL(null, decodedHtml, "text/html", "UTF-8", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 파일 읽기 권한을 허용했을 때 다운로드 폴더 열기
                openDownloadFolder()
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            // 파일 읽기 권한이 승인된 경우
//            openDownloadFolder()
//        }
//    }


    private fun openDownloadFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/octet-stream"
        // MHT 파일 형식에 맞게 수정
        openFolderLauncher.launch(intent)
    }
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("앱 종료")
        builder.setMessage("앱을 종료하시겠습니까?")
        builder.setPositiveButton("확인") { _, _ ->
            finish()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}