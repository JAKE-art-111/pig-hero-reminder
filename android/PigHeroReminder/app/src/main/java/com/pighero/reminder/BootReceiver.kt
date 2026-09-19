package com.pighero.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 开机、应用更新、系统时间被修改后，重新安排全部闹钟并刷新挂件。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.scheduleAll(context)
        PigWidgetProvider.refreshAll(context)
        PigWidgetProvider.scheduleHourlyTick(context)
    }
}
