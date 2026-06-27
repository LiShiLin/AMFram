package com.amfram.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amfram.R
import com.amfram.data.SourceListRepo
import com.amfram.util.AppSettings
import com.amfram.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = SourceAdapter(
        onPlay = { openPlay(it) },
        onLongClick = { confirmDelete(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sourceListView.layoutManager = LinearLayoutManager(this)
        binding.sourceListView.adapter = adapter

        binding.fabAdd.setOnClickListener { openConfig(null) }
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            SourceListRepo.ensureDefault(this@MainActivity)
            val list = SourceListRepo.load(this@MainActivity)
            adapter.submitList(list)
            binding.emptyView.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun openConfig(name: String?) {
        val intent = Intent(this, SourceConfigActivity::class.java)
        intent.putExtra(SourceConfigActivity.EXTRA_NAME, name)
        startActivity(intent)
    }

    private fun openPlay(source: com.amfram.data.SourceConfig) {
        val intent = Intent(this, PlayActivity::class.java)
        intent.putExtra(PlayActivity.EXTRA_SOURCE, source)
        startActivity(intent)
    }

    private fun confirmDelete(source: com.amfram.data.SourceConfig) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(source.name)
            .setPositiveButton(R.string.delete) { _, _ ->
                SourceListRepo.remove(this, source.name)
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}