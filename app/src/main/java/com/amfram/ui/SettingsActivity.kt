package com.amfram.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.amfram.R
import com.amfram.data.ShowMode
import com.amfram.util.AppSettings
import com.amfram.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        binding.darkModeSwitch.isChecked = AppSettings.isDark(this)
        binding.darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            AppSettings.setDark(this, checked)
        }

        val orientNames = listOf(getString(R.string.orientation_auto), getString(R.string.orientation_portrait), getString(R.string.orientation_landscape))
        binding.orientationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, orientNames)
        binding.orientationSpinner.setSelection(AppSettings.orientation(this))
        binding.orientationSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                AppSettings.setOrientation(this@SettingsActivity, pos)
                AppSettings.applyOrientation(this@SettingsActivity)
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        val modes = ShowMode.values().map { it.name }
        binding.modeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes)
        binding.modeSpinner.setSelection(AppSettings.showModeOrdinal(this))

        binding.modeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val mode = ShowMode.values()[pos]
                binding.gridFields.visibility = if (mode == ShowMode.FrameWall) View.VISIBLE else View.GONE
                binding.intervalLayout.visibility = if (mode == ShowMode.Slide) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        val gridModes = listOf(getString(R.string.grid_mode_center), getString(R.string.grid_mode_fill))
        binding.gridModeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gridModes)
        binding.gridModeSpinner.setSelection(
            if (AppSettings.gridMode(this) == "fill") 1 else 0
        )

        binding.intervalEditText.setText(AppSettings.intervalSeconds(this).toString())
        val rowValues = (1..6).map { it.toString() }
        binding.gridRowsSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, rowValues)
        binding.gridRowsSpinner.setSelection(AppSettings.gridRows(this) - 1)
        binding.gridColsSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, rowValues)
        binding.gridColsSpinner.setSelection(AppSettings.gridCols(this) - 1)
        val replaceValues = (3..15).map { it.toString() }
        binding.replaceIntervalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, replaceValues)
        binding.replaceIntervalSpinner.setSelection((AppSettings.replaceIntervalSeconds(this).coerceIn(3, 15) - 3))

        val spacing = AppSettings.gridSpacingDp(this)
        binding.gridSpacingSeekBar.max = 4 // 0..4 -> 1..5dp
        binding.gridSpacingSeekBar.progress = spacing - 1
        binding.gridSpacingValueText.text = getString(R.string.grid_size) + ": " + spacing + "dp"
        binding.gridSpacingSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val v = progress + 1
                binding.gridSpacingValueText.text = getString(R.string.grid_size) + ": " + v + "dp"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                AppSettings.setGridSpacing(this@SettingsActivity, (seekBar?.progress ?: 1) + 1)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        val mode = binding.modeSpinner.selectedItemPosition
        AppSettings.setShowMode(this, mode)
        binding.intervalEditText.text.toString().toIntOrNull()?.let {
            if (it > 0) AppSettings.setInterval(this, it)
        }
        AppSettings.setGridRows(this, binding.gridRowsSpinner.selectedItemPosition + 1)
        AppSettings.setGridCols(this, binding.gridColsSpinner.selectedItemPosition + 1)
        val fillSelected = binding.gridModeSpinner.selectedItemPosition == 1
        AppSettings.setGridMode(this, if (fillSelected) "fill" else "center")
        AppSettings.setReplaceInterval(this, binding.replaceIntervalSpinner.selectedItemPosition + 3)
    }
}