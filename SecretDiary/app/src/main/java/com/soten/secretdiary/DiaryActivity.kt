package com.soten.secretdiary

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import com.soten.secretdiary.databinding.ActivityDiaryBinding
import com.soten.secretdiary.databinding.ActivityMainBinding

class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryBinding

    private val handler = Handler(Looper.getMainLooper()) // 메인스레드에 연결 된 핸들러러

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val detailPreferences = getSharedPreferences("diary", Context.MODE_PRIVATE)

        binding.diaryEditText.setText(detailPreferences.getString("detail", ""))

        val runnable = Runnable {
            getSharedPreferences("diary", Context.MODE_PRIVATE).edit {
                putString("detail", binding.diaryEditText.text.toString())
            }

            Log.d("DiaryActivity", "SAVE ${binding.diaryEditText.text}")
        }

        binding.diaryEditText.addTextChangedListener {

            Log.d("DiaryActivity", "Text :: $it")
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 500)
        }
    }
}