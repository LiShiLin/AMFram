package com.amfram.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.amfram.R
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

        val types = SourceType.values().map { it.name }
        binding.typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        binding.typeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val type = SourceType.values()[pos]
                binding.smbFields.visibility = if (type == SourceType.SMB) View.VISIBLE else View.GONE
                binding.localFields.visibility = if (type == SourceType.Local) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        binding.pickFolderButton.setOnClickListener {
            val intent = Intent(this, FilePickerActivity::class.java)
            startActivityForResult(intent, REQ_PICK_FOLDER)
        }

        // 编辑模式：预填
        editingName?.let { name ->
            val list = SourceListRepo.load(this)
            val target = list.firstOrNull { it.name == name } ?: return@let
            binding.nameEditText.setText(target.name)
            binding.typeSpinner.setSelection(SourceType.values().indexOf(target.type))
            binding.hostEditText.setText(target.host)
            binding.shareEditText.setText(target.shareName)
            binding.usernameEditText.setText(target.username)
            binding.passwordEditText.setText(target.getPassword())
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
            binding.nameEditText.error = getString(R.string.source_name)
            return
        }
        val type = SourceType.values()[binding.typeSpinner.selectedItemPosition]
        val path = if (type == SourceType.Local) (pickedFolderPath ?: "") else ""

        val config = SourceConfig(
            name = name,
            type = type,
            path = path,
            host = binding.hostEditText.text.toString().trim(),
            shareName = binding.shareEditText.text.toString().trim(),
            username = binding.usernameEditText.text.toString().trim()
        ).setPassword(binding.passwordEditText.text.toString())

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