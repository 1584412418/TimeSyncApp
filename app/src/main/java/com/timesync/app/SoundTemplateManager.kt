package com.timesync.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SoundTemplate(
    val id: String,
    val name: String,
    val fingerprint: FloatArray,
    val createdAt: Long
)

class SoundTemplateManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("sound_templates", Context.MODE_PRIVATE)
    private val templates = mutableListOf<SoundTemplate>()

    init {
        loadTemplates()
    }

    fun addTemplate(name: String, fingerprint: FloatArray): SoundTemplate {
        val template = SoundTemplate(
            id = System.currentTimeMillis().toString(),
            name = name,
            fingerprint = fingerprint,
            createdAt = System.currentTimeMillis()
        )
        templates.add(template)
        saveTemplates()
        return template
    }

    fun removeTemplate(id: String) {
        templates.removeAll { it.id == id }
        saveTemplates()
    }

    fun getTemplates(): List<SoundTemplate> = templates

    fun getTemplate(id: String): SoundTemplate? =
        templates.find { it.id == id }

    private fun saveTemplates() {
        val array = JSONArray()
        templates.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("name", t.name)
            obj.put("createdAt", t.createdAt)
            val fp = JSONArray()
            t.fingerprint.forEach { fp.put(it) }
            obj.put("fingerprint", fp)
            array.put(obj)
        }
        prefs.edit().putString("templates", array.toString()).apply()
    }

    private fun loadTemplates() {
        val data = prefs.getString("templates", "[]") ?: "[]"
        val array = JSONArray(data)
        templates.clear()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val fpArray = obj.getJSONArray("fingerprint")
            val fp = FloatArray(fpArray.length())
            for (j in 0 until fpArray.length()) {
                fp[j] = fpArray.getDouble(j).toFloat()
            }
            templates.add(
                SoundTemplate(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    fingerprint = fp,
                    createdAt = obj.getLong("createdAt")
                )
            )
        }
    }
}
