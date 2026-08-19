package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class WheelPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("spin_wheel_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TITLE = "wheel_title"
        private const val KEY_MESSAGE_TEMPLATE = "message_template"
        private const val KEY_CENTER_LOGO = "center_logo_uri"
        private const val KEY_ITEMS_JSON = "items_json"

        const val DEFAULT_TITLE = "ကံစမ်းမဲ လှည့်ကြမယ်"
        const val DEFAULT_MESSAGE_TEMPLATE = "Congratulation!!! {item} ကို ကံထူးသွားပါပြီ"

        val DEFAULT_ITEMS = listOf(
            WheelItem(name = "ထီး (Umbrella)", colorHex = "#D0BCFF", quantity = 3),
            WheelItem(name = "ဦးထုပ် (Cap)", colorHex = "#EADDFF", quantity = 5),
            WheelItem(name = "တီရှပ် (T-Shirt)", colorHex = "#F9DEDC", quantity = 2),
            WheelItem(name = "ဖုန်းကဒ် (Top-up)", colorHex = "#C4EED0", quantity = 4),
            WheelItem(name = "ရေဗူး (Bottle)", colorHex = "#C2E7FF", quantity = 3),
            WheelItem(name = "ဘောပင် (Pen)", colorHex = "#FFD8E4", quantity = 5)
        )
    }

    fun getTitle(): String {
        return try {
            val title = prefs.getString(KEY_TITLE, DEFAULT_TITLE)
            if (title.isNullOrBlank()) DEFAULT_TITLE else title
        } catch (e: Throwable) {
            DEFAULT_TITLE
        }
    }

    fun saveTitle(title: String) {
        try {
            prefs.edit().putString(KEY_TITLE, title.ifBlank { DEFAULT_TITLE }).apply()
        } catch (e: Throwable) {
            // ignore
        }
    }

    fun getMessageTemplate(): String {
        return try {
            val template = prefs.getString(KEY_MESSAGE_TEMPLATE, DEFAULT_MESSAGE_TEMPLATE)
            if (template.isNullOrBlank()) DEFAULT_MESSAGE_TEMPLATE else template
        } catch (e: Throwable) {
            DEFAULT_MESSAGE_TEMPLATE
        }
    }

    fun saveMessageTemplate(template: String) {
        try {
            prefs.edit().putString(KEY_MESSAGE_TEMPLATE, template.ifBlank { DEFAULT_MESSAGE_TEMPLATE }).apply()
        } catch (e: Throwable) {
            // ignore
        }
    }

    fun getCenterLogoUri(): String? {
        return try {
            prefs.getString(KEY_CENTER_LOGO, null)
        } catch (e: Throwable) {
            null
        }
    }

    fun saveCenterLogoUri(uriString: String?) {
        try {
            if (uriString == null) {
                prefs.edit().remove(KEY_CENTER_LOGO).apply()
            } else {
                prefs.edit().putString(KEY_CENTER_LOGO, uriString).apply()
            }
        } catch (e: Throwable) {
            // ignore
        }
    }

    fun getItems(): List<WheelItem> {
        val jsonStr = try {
            prefs.getString(KEY_ITEMS_JSON, null)
        } catch (e: Throwable) {
            null
        } ?: return DEFAULT_ITEMS

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<WheelItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").trim()
                if (name.isNotEmpty()) {
                    list.add(
                        WheelItem(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            name = name,
                            colorHex = obj.optString("colorHex", "#D0BCFF"),
                            quantity = obj.optInt("quantity", 1).coerceIn(1, 100)
                        )
                    )
                }
            }
            if (list.isEmpty()) DEFAULT_ITEMS else list
        } catch (e: Throwable) {
            DEFAULT_ITEMS
        }
    }

    fun saveItems(items: List<WheelItem>) {
        try {
            val safeItems = if (items.isEmpty()) DEFAULT_ITEMS else items
            val jsonArray = JSONArray()
            for (item in safeItems) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("colorHex", item.colorHex)
                    put("quantity", item.quantity)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_ITEMS_JSON, jsonArray.toString()).apply()
        } catch (e: Throwable) {
            // ignore
        }
    }

    fun resetToDefaults() {
        try {
            prefs.edit()
                .putString(KEY_TITLE, DEFAULT_TITLE)
                .putString(KEY_MESSAGE_TEMPLATE, DEFAULT_MESSAGE_TEMPLATE)
                .remove(KEY_CENTER_LOGO)
                .remove(KEY_ITEMS_JSON)
                .apply()
        } catch (e: Throwable) {
            // ignore
        }
    }
}

