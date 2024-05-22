package me.neversleep.plusplus

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {
    private var xConf: SharedPreferences? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_main)
        main()
    }

    private fun checkEdXposed() {
        try {
            this.xConf = getSharedPreferences("x_conf", MODE_WORLD_READABLE)
        } catch (unused: SecurityException) {
            AlertDialog.Builder(this).setMessage(getString(R.string.not_supported))
                .setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int -> finish() })
                .setNegativeButton(R.string.ignore, null).show()
        }
    }

    override fun onResume() {
        super.onResume()
        main()
    }

    @SuppressLint("RestrictedApi")
    protected fun main() {
        val disableSleep: CheckBox = findViewById(R.id.disable_sleep)
        val switchCompat: SwitchCompat = findViewById(R.id.power)
        disableSleep.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (xConf != null) {
                val old = xConf!!.getBoolean("disable_sleep", false)
                if (old != isChecked) {
                    if (!xConf!!.edit().putBoolean("disable_sleep", isChecked).commit()) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.disable_sleep_error_tips,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "error: xConf is null!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        switchCompat.setOnCheckedChangeListener { compoundButton, z ->
            val sharedPreferences = this@MainActivity.xConf
            if (sharedPreferences != null) {
                if (!sharedPreferences.edit().putBoolean("power", z).commit()) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.failed_tips),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        checkEdXposed()
        if (this.xConf == null) {
            Toast.makeText(this, "error: xConf is null!", Toast.LENGTH_LONG).show()
        } else {
            switchCompat.setChecked(xConf!!.getBoolean("power", false))
        }
    }


    companion object {
        val activeVersion: Int
            get() = 0
    }
}