package com.pighero.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build

/**
 * 通知渠道与通知发送；并负责把矢量小猪渲染成通知大图。
 * 注：androidx.annotation 仅为编译期注解，框架本身不需要任何运行时依赖。
 */
object NotificationHelper {

    const val CHANNEL_ID = "pig_daily_reminder"
    private const val CHANNEL_NAME = "每日提醒"
    private const val REQ_NOTI_BASE = 6000

    fun ensureChannel(c: Context) {
        val mgr = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "猪猪侠每天到点的日程提醒"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    enableLights(true)
                }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    fun show(c: Context, r: Reminder) {
        ensureChannel(c)
        val mgr = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(REQ_NOTI_BASE + r.id, build(c, "${r.timeText}  ${r.content}"))
    }

    fun showTest(c: Context) {
        ensureChannel(c)
        val mgr = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(
            REQ_NOTI_BASE,
            build(c, "（测试）猪猪侠报到！以后每天到点我都会准时喊你")
        )
    }

    private fun build(c: Context, text: String): Notification {
        val open = PendingIntent.getActivity(
            c, 7000,
            Intent(c, WidgetConfigureActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(c, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(c)
        }
        return builder
            .setSmallIcon(R.drawable.ic_pig_notification)
            .setLargeIcon(vectorToBitmap(c, R.drawable.ic_pig_hero, 256))
            .setContentTitle("猪猪侠提醒你")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
    }

    /** 把矢量图渲染成位图，用作通知 largeIcon。 */
    private fun vectorToBitmap(c: Context, resId: Int, size: Int): Bitmap {
        val d: Drawable? = c.getDrawable(resId)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        d?.setBounds(0, 0, size, size)
        d?.draw(canvas)
        return bmp
    }
}
