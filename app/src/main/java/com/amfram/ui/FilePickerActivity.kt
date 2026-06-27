package com.amfram.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.amfram.R
import com.amfram.databinding.ActivityFilePickerBinding
import java.io.File

/**
 * 4.4 原生兼容的文件浏览器（不依赖 SAF）。
 * 用 java.io.File 遍历，需要 READ_EXTERNAL_STORAGE 权限。
 */
class FilePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilePickerBinding
    private var currentDir: File = Environment.getExternalStorageDirectory()
    private val adapter = FileAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasReadPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQ_PERM
            )
            return
        }
        init()
    }

    private fun init() {
        binding.listView.adapter = adapter
        binding.btnUp.setOnClickListener {
            val parent = currentDir.parentFile
            if (parent != null && parent.canRead()) {
                currentDir = parent
                refresh()
            }
        }
        binding.btnOk.setOnClickListener {
            android.util.Log.d("AMFram", "ok button clicked, path=${currentDir.absolutePath}")
            val data = Intent()
            data.putExtra(EXTRA_PATH, currentDir.absolutePath)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
        refresh()
    }

    private fun refresh() {
        binding.pathText.text = currentDir.absolutePath
        adapter.update(currentDir)
        binding.btnOk.text = if (adapter.imageCount > 0)
            getString(R.string.pick_folder_ok) + " (${adapter.imageCount})"
        else getString(R.string.pick_folder_ok)
    }

    private fun hasReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) init()
            else {
                Toast.makeText(this, R.string.pick_storage_perm, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private inner class FileAdapter : BaseAdapter() {
        private val items = mutableListOf<File>()
        var imageCount: Int = 0

        fun update(dir: File) {
            items.clear()
            imageCount = 0
            val list = dir.listFiles()
            if (list != null) {
                // 子目录在前
                items.addAll(list.filter { it.isDirectory }.sortedBy { it.name.lowercase() })
                // 图片在后
                val imgs = list.filter { it.isFile && isImageFile(it.name) }
                    .sortedBy { it.name.lowercase() }
                imageCount = imgs.size
                items.addAll(imgs)
            }
            notifyDataSetChanged()
        }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): File? = items.getOrNull(position)
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_file_entry, parent, false)
            val f = items[position]
            view.findViewById<TextView>(R.id.name).text = f.name
            view.findViewById<android.widget.ImageView>(R.id.icon).setImageResource(
                if (f.isDirectory) android.R.drawable.ic_menu_more
                else android.R.drawable.ic_menu_gallery
            )
            view.setOnClickListener {
                if (f.isDirectory) {
                    currentDir = f
                    refresh()
                }
            }
            return view
        }
    }

    companion object {
        const val EXTRA_PATH = "extra_path"
        private const val REQ_PICK = 0x201
        private const val REQ_PERM = 0x202
        fun startForResult(activity: AppCompatActivity, requestCode: Int = REQ_PICK) {
            val intent = Intent(activity, FilePickerActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
        fun isImageFile(name: String): Boolean {
            val l = name.lowercase()
            return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") ||
                l.endsWith(".webp") || l.endsWith(".gif") || l.endsWith(".bmp")
        }
    }
}