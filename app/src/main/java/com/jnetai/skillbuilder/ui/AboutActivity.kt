package com.jnetai.skillbuilder.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jnetai.skillbuilder.BuildConfig
import com.jnetai.skillbuilder.R
import com.jnetai.skillbuilder.databinding.ActivityAboutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"

        // Version from BuildConfig
        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        binding.btnCheckUpdates.setOnClickListener { checkForUpdates() }

        binding.btnShare.setOnClickListener { shareApp() }

        binding.btnViewSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnetai-clawbot/SkillBuilder"))
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun checkForUpdates() {
        binding.tvUpdateStatus.visibility = View.VISIBLE
        binding.tvUpdateStatus.text = "Checking for updates..."
        binding.btnCheckUpdates.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/jnetai-clawbot/SkillBuilder/releases/latest")
                    val connection = url.openConnection()
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connection.connect()
                    val json = connection.getInputStream().bufferedReader().use { it.readText() }
                    val obj = JSONObject(json)
                    val tagName = obj.optString("tag_name", "unknown")
                    val htmlUrl = obj.optString("html_url", "")
                    Pair(tagName, htmlUrl)
                }

                val currentVersion = BuildConfig.VERSION_NAME
                val latestVersion = result.first
                val releaseUrl = result.second

                if (latestVersion != "unknown" && latestVersion != currentVersion) {
                    binding.tvUpdateStatus.text = "New version available: $latestVersion\nTap to download"
                    binding.tvUpdateStatus.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                        startActivity(intent)
                    }
                    binding.tvUpdateStatus.setTextColor(getColor(R.color.md_theme_dark_primary))
                } else {
                    binding.tvUpdateStatus.text = "You're up to date! (v$currentVersion)"
                }
            } catch (e: Exception) {
                binding.tvUpdateStatus.text = "Could not check for updates"
            }
            binding.btnCheckUpdates.isEnabled = true
        }
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "SkillBuilder - Track Your Skills")
            putExtra(Intent.EXTRA_TEXT, "Check out SkillBuilder - the adaptive skill tracking app!\n\nhttps://github.com/jnetai-clawbot/SkillBuilder")
        }
        startActivity(Intent.createChooser(shareIntent, "Share SkillBuilder"))
    }
}