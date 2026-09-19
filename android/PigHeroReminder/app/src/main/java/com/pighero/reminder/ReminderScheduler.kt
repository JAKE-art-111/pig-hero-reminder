package com.pighero.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * 每日定时调度。
 *
 * 使用 [AlarmManager.setAlarmClock]：这是 Android 上最准时的闹钟类型，
 * 不受 Doze 省电模式影响，也不需要申请“精确闹钟”特殊权限。
 * 闹钟只触发一次，触发后由 [ReminderReceiver] 重新安排到第二天。
 */
object ReminderScheduler {

    private const val TAG = "PigScheduler"
    const val ACTION_FIRE = "com.pighero.reminder.ACTION_FIRE"

    private const val REQ_FIRE_BASE = 1000
    private const val REQ_SHOW_BASE = 5000

    /** 全量重排：先按现有数据取消旧闹钟，再为每条启用的提醒安排下一次触发。 */
    fun scheduleAll(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Android 12+：未授予“精确闹钟”权限时直接返回，绝不崩
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            Log.w(TAG, "未授予精确闹钟权限，本次跳过调度")
            return
        }
        val list = ReminderStore.getAll(c)

        // 取消旧闹钟（含已删除 / 已停用的）
        list.forEach { r ->
            val old = PendingIntent.getBroadcast(
                c, REQ_FIRE_BASE + r.id,
                Intent(c, ReminderReceiver::class.java).setAction(ACTION_FIRE),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (old != null) {
                am.cancel(old)
                old.cancel()
            }
        }

        // 安排新闹钟
        list.filter { it.enabled }.forEach { r ->
            val triggerAt = r.nextTriggerMillis()
            val firePi = PendingIntent.getBroadcast(
                c, REQ_FIRE_BASE + r.id,
                Intent(c, ReminderReceiver::class.java).apply {
                    action = ACTION_FIRE
                    putExtra("id", r.id)
                    putExtra("hour", r.hour)
                    putExtra("minute", r.minute)
                    putExtra("content", r.content)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // 点击系统闹钟图标时打开设置页
            val showPi = PendingIntent.getActivity(
                c, REQ_SHOW_BASE + r.id,
                Intent(c, WidgetConfigureActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showPi), firePi)
            Log.i(TAG, "已安排 ${r.timeText} ${r.content}")
        }
    }
}
