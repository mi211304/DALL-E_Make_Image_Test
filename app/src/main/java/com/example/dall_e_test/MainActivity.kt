package com.example.dall_e_test

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val openaiApiKey = "API_KYE"    //openaiのapiキー

    private lateinit var userInputEditText: EditText
    private lateinit var generateButton: Button
    private lateinit var generatedImageView: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userInputEditText = findViewById(R.id.userInputEditText)
        generateButton = findViewById(R.id.generateButton)
        generatedImageView = findViewById(R.id.generatedImageView)
        generateButton.setOnClickListener {
            val userInput = userInputEditText.text.toString()
            generateImageAndDownload(userInput)
        }
    }

    private fun generateImageAndDownload(userInput: String) {
        // HTTPリクエストを送信して画像を生成
        val client = OkHttpClient()
        val url = "https://api.openai.com/v1/davinci/images/generate"
        val json = """
                {
                    "prompt": "Generate an image of: $userInput",
                    "max_tokens": 50,
                    "n": 1
                }
        """.trimIndent()

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $openaiApiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {

                    val responseBody = response.body

                    responseBody?.byteStream()?.use { inputStream ->
                        // ファイルに画像を保存
                        val savedImagePath = saveImage(inputStream)

                        runOnUiThread {
                            // 画像をImageViewに設定
                            setImageView(savedImagePath)
                        }
                        Log.d("SavedImage", "Saved image to: $savedImagePath") // 追加
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("DownloadError", e.message ?: "Error downloading image")
            }
        })
    }


    //画像の保存処理
    private fun saveImage(inputStream: java.io.InputStream): String {
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "generated_image.png"
        val file = File(directory, fileName)

        try {
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
            return file.absolutePath
        } catch (e: IOException) {
            Log.e("SaveError", "Error saving image")
            return ""
        }
    }

    private fun setImageView(imagePath: String) {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        generatedImageView.setImageBitmap(bitmap)
    }
}



