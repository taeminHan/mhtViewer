//package com.kyle.mhtviewer
//
//import android.Manifest
//import android.app.Activity
//import android.app.AlertDialog
//import android.content.Intent
//import android.content.pm.PackageManager
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.OnBackPressedCallback
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//class HomeActivity : AppCompatActivity() {
//
//    private lateinit var openFolderLauncher: ActivityResultLauncher<Intent>
//    private val PERMISSION_REQUEST_CODE = 123
//    private val callback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            // 뒤로 버튼 이벤트 처리
//            showExitConfirmationDialog()
//        }
//    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//        val mainIntent = Intent(this, MainActivity::class.java)
//        openFolderLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == RESULT_OK) {
//                val data: Intent? = result.data
//                data?.data?.let { uri ->
//                    // 파일 경로를 가져와서 MHT 파일 읽기 및 WebView에 표시
//                    mainIntent.putExtra("uri", uri.toString())
//                    startActivity(mainIntent)
//                }
//            }
//        }
//
//        // 파일 읽기 권한 확인
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
//            )
//        } else {
//            // 파일 읽기 권한이 있을 때 다운로드 폴더 열기
//            openDownloadFolder()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 파일 읽기 권한을 허용했을 때 다운로드 폴더 열기
//                openDownloadFolder()
//            } else {
//                // 권한을 거부한 경우 처리
//                // 사용자에게 권한이 필요하다는 메시지를 표시하거나 다른 조치를 취할 수 있습니다.
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == Activity.RESULT_OK) {
//            // 파일 읽기 권한이 승인된 경우
//            openDownloadFolder()
//        }
//    }
//
//
//    private fun openDownloadFolder() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        intent.type = "application/octet-stream"
//        // MHT 파일 형식에 맞게 수정
//        openFolderLauncher.launch(intent)
//        this.onBackPressedDispatcher.addCallback(this, callback)
//    }
//    private fun showExitConfirmationDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("앱 종료")
//        builder.setMessage("앱을 종료하시겠습니까?")
//        builder.setPositiveButton("확인") { _, _ ->
//            finish()
//        }
//        builder.setNegativeButton("취소") { dialog, _ ->
//            dialog.dismiss()
//        }
//        val dialog = builder.create()
//        dialog.show()
//    }
//
//}