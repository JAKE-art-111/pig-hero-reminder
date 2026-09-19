package com.pighero.reminder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * 用 SharedPreferences + JSON 保存全部提醒，无第三方依赖。
 */
object ReminderStore {

    private const val PREFS = "pig_reminder_prefs"
    private const val KEY_DATA = "data_json"
    private const val KEY_NEXT_ID = "next_id"

    private val defaults = listOf(
        Triple(7, 30, "起床洗漱，精神满满"),
        Triple(12, 0, "吃午饭，好好休息"),
        Triple(18, 0, "吃晚饭啦"),
        Triple(21, 30, "放下手机，准备睡觉")
    )

    private fun prefs(c: Context) =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** 读取全部提醒，首次启动时写入示例提醒。 */
    fun getAll(c: Context): MutableList<Reminder> {
        val p = prefs(c)
        if (!p.contains(KEY_DATA)) {
            val list = defaults.mapIndexed { i, t ->
                Reminder(i + 1, t.first, t.second, t.third, true)
            }
            save(c, list)
            p.edit().putInt(KEY_NEXT_ID, defaults.size + 1).apply()
            return list.toMutableList()
        }
        val arr = JSONArray(p.getString(KEY_DATA, "[]"))
        val list = ArrayList<Reminder>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Reminder(
                    id = o.getInt("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    content = o.getString("content"),
                    enabled = o.getBoolean("enabled")
                )
            )
        }
        return list
    }

    fun save(c: Context, list: List<Reminder>) {
        val arr = JSONArray()
        list.sortedWith(compareBy({ it.hour }, { it.minute })).forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("hour", r.hour)
                put("minute", r.minute)
                put("content", r.content)
                put("enabled", r.enabled)
            })
        }
        prefs(c).edit().putString(KEY_DATA, arr.toString()).apply()
    }

    fun add(c: Context, hour: Int, minute: Int, content: String): Reminder {
        val list = getAll(c)
        val p = prefs(c)
        val id = p.getInt(KEY_NEXT_ID, list.size + 1)
        val r = Reminder(id, hour, minute, content.ifBlank { "该做事情啦" }, true)
        list.add(r)
        save(c, list)
        p.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return r
    }

    fun remove(c: Context, id: Int) {
        save(c, getAll(c).filterNot { it.id == id })
    }

    fun setEnabled(c: Context, id: Int, enabled: Boolean) {
        val list = getAll(c)
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(enabled = enabled)
            save(c, list)
        }
    }

    /** 按时间排序后的启用提醒。 */
    fun activeSorted(c: Context): List<Reminder> =
        getAll(c).filter { it.enabled }.sortedWith(compareBy({ it.hour }, { it.minute }))
}
