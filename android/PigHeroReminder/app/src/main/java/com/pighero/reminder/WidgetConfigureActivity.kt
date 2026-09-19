package com.pighero.reminder

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText

import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast

/**
 * 挂件配置页（同时也是桌面图标入口）：
 * 添加 / 开关 / 删除每日提醒，改动立即保存并重排闹钟。
 */
class WidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var listContainer: LinearLayout
    private var askedExactAlarm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        // 用户没点“完成”就退出时，不添加挂件
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        NotificationHelper.ensureChannel(this)
        requestNotificationPermission()

        listContainer = findViewById(R.id.list_container)
        val timePicker = findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)
        val etContent = findViewById<EditText>(R.id.et_content)

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            val content = etContent.text.toString().trim()
            ReminderStore.add(this, timePicker.hour, timePicker.minute, content)
            etContent.setText("")
            afterChange()
            Toast.makeText(this, "已添加，猪猪侠记住啦", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            NotificationHelper.showTest(this)
        }

        findViewById<Button>(R.id.btn_done).setOnClickListener { finishConfigure() }
    }

    override fun onResume() {
        super.onResume()
        ensureExactAlarmPermission()
        // 每次回到页面都重排闹钟并刷新列表（覆盖开机后首次进入等情况）
        ReminderScheduler.scheduleAll(this)
        renderList()
    }

    /** Android 12+：没有精确闹钟权限就引导用户去系统设置里打开。 */
    private fun ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        if (am.canScheduleExactAlarms() || askedExactAlarm) return
        askedExactAlarm = true
        Toast.makeText(this, "需要“精确闹钟”权限，提醒才能准点响", Toast.LENGTH_LONG).show()
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:$packageName"))
            )
        } catch (e: Exception) {
            try {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                )
            } catch (_: Exception) { }
        }
    }

    private fun afterChange() {
        ReminderScheduler.scheduleAll(this)
        PigWidgetProvider.refreshAll(this)
        renderList()
    }

    private fun renderList() {
        listContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        val list = ReminderStore.getAll(this).sortedWith(compareBy({ it.hour }, { it.minute }))

        if (list.isEmpty()) {
            val empty = TextView(this).apply {
                text = "还没有提醒，在下面添加一条吧"
                textSize = 13f
                setTextColor(0xFFB39E96.toInt())
                gravity = android.view.Gravity.CENTER
                setPadding(0, 28, 0, 28)
            }
            listContainer.addView(empty)
            return
        }

        list.forEach { r ->
            val item = inflater.inflate(R.layout.item_reminder, listContainer, false)
            item.findViewById<TextView>(R.id.item_time).text = r.timeText
            val tvContent = item.findViewById<TextView>(R.id.item_content)
            tvContent.text = r.content
            val sw = item.findViewById<Switch>(R.id.item_switch)
            sw.isChecked = r.enabled
            tvContent.alpha = if (r.enabled) 1f else 0.4f
            sw.setOnCheckedChangeListener { _, checked ->
                ReminderStore.setEnabled(this, r.id, checked)
                afterChange()
            }
            item.findViewById<View>(R.id.item_del).setOnClickListener {
                ReminderStore.remove(this, r.id)
                afterChange()
            }
            listContainer.addView(item)
        }
    }

    private fun finishConfigure() {
        ReminderScheduler.scheduleAll(this)
        PigWidgetProvider.refreshAll(this)
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            )
        }
        finish()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }
}
