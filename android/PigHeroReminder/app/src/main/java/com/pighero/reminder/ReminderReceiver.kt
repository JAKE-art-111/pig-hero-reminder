package com.pighero.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 到点接收：弹出通知、刷新挂件，并把该提醒重新安排到明天。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_FIRE) return
        val id = intent.getIntExtra("id", -1)
        val r = ReminderStore.getAll(context).find { it.id == id } ?: return

        NotificationHelper.show(context, r)
        PigWidgetProvider.refreshAll(context)
        // 全量重排：本次闹钟已触发，nextTriggerMillis 会自动顺延到明天
        ReminderScheduler.scheduleAll(context)
    }
}
