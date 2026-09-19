package com.pighero.reminder

import java.util.Calendar

/**
 * 一条每日提醒。
 * id 为本地自增主键；hour/minute 为 24 小时制；enabled 控制是否生效。
 */
data class Reminder(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val content: String,
    val enabled: Boolean
) {
    val timeText: String
        get() = String.format(java.util.Locale.CHINA, "%02d:%02d", hour, minute)

    /** 今天或下一次触发的毫秒时间戳（该时刻今天已过则顺延到明天）。 */
    fun nextTriggerMillis(now: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DATE, 1)
            }
        }.timeInMillis
    }
}
