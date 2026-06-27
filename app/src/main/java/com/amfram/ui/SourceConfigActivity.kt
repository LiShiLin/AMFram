package com.amfram.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amfram.data.SourceConfig
import com.amfram.data.SourceListRepo
import com.amfram.data.SourceType
import com.amfram.databinding.ActivitySourceConfigBinding

class SourceConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySourceConfigBinding
    private var editingName: String? = null
    private var pickedFolderPath: String? = null
    private val REQ_PICK_FOLDER = 0x201

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        android.util.Log.d("AMFram", "onActivityResult req=$requestCode result=$resultCode data=$data")
        if (requestCode == REQ_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val path = data?.getStringExtra(FilePickerActivity.EXTRA_PATH)
            android.util.Log.d("AMFram", "got path=$path")
            if (!path.isNullOrEmpty()) {
                pickedFolderPath = path
                binding.folderPathText.text = path
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySourceConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        editingName = intent.getStringExtra(EXTRA_NAME)

        binding.pickFolderButton.setOnClickListener {
            val intent = Intent(this, FilePickerActivity::class.java)
            startActivityForResult(intent, REQ_PICK_FOLDER)
        }

        editingName?.let { name ->
            val list = SourceListRepo.load(this)
            val target = list.firstOrNull { it.name == name } ?: return@let
            binding.nameEditText.setText(target.name)
            if (target.type == SourceType.Local && target.path.isNotEmpty()) {
                pickedFolderPath = target.path
                binding.folderPathText.text = target.path
            }
        }

        binding.saveButton.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameEditText.error = getString(com.amfram.R.string.source_name)
            return
        }

        val config = SourceConfig(
            name = name,
            type = SourceType.Local,
            path = pickedFolderPath ?: ""
        )

        if (editingName != null && editingName != name) {
            SourceListRepo.remove(this, editingName!!)
        }
        SourceListRepo.add(this, config)
        finish()
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
    }
}
