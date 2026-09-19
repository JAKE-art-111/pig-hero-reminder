package com.pighero.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 桌面挂件：小猪头像 + 实时时钟（TextClock 由系统刷新）+ 下一条提醒。
 * 每小时由一个不精确循环广播唤醒一次，用于跨天刷新“下一条”文案。
 */
class PigWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TICK = "com.pighero.reminder.ACTION_WIDGET_TICK"
        private const val REQ_TICK = 8000
        private const val REQ_OPEN = 7000

        fun refreshAll(c: Context) {
            val mgr = AppWidgetManager.getInstance(c)
            val ids = mgr.getAppWidgetIds(ComponentName(c, PigWidgetProvider::class.java))
            ids.forEach { id -> updateOne(c, mgr, id) }
        }

        private fun updateOne(
            c: Context,
            mgr: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(c.packageName, R.layout.widget_pig)

            val dateFmt = SimpleDateFormat("M月d日 EEEE", Locale.CHINA)
            views.setTextViewText(R.id.tv_date, dateFmt.format(Calendar.getInstance().time))
            views.setTextViewText(R.id.tv_next, nextReminderText(c))

            val open = PendingIntent.getActivity(
                c, REQ_OPEN,
                Intent(c, WidgetConfigureActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, open)
            mgr.updateAppWidget(appWidgetId, views)
        }

        private fun nextReminderText(c: Context): String {
            val now = Calendar.getInstance()
            val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val actives = ReminderStore.activeSorted(c)
            if (actives.isEmpty()) return "今天没有安排，好好放松吧"
            val today = actives.firstOrNull { it.hour * 60 + it.minute > nowMin }
            return if (today != null) {
                "下一条 ${today.timeText} ${today.content}"
            } else {
                "明天 ${actives.first().timeText} ${actives.first().content}"
            }
        }

        fun scheduleHourlyTick(c: Context) {
            val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                c, REQ_TICK,
                Intent(c, PigWidgetProvider::class.java).setAction(ACTION_TICK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR,
                pi
            )
        }

        private fun cancelHourlyTick(c: Context) {
            val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                c, REQ_TICK,
                Intent(c, PigWidgetProvider::class.java).setAction(ACTION_TICK),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                am.cancel(pi)
                pi.cancel()
            }
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> updateOne(context, mgr, id) }
        scheduleHourlyTick(context)
    }

    override fun onEnabled(context: Context) {
        scheduleHourlyTick(context)
    }

    override fun onDisabled(context: Context) {
        cancelHourlyTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) refreshAll(context)
    }
}
