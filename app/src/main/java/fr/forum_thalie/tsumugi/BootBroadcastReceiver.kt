package fr.forum_thalie.tsumugi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import fr.forum_thalie.tsumugi.alarm.RadioAlarm
import fr.forum_thalie.tsumugi.playerstore.PlayerStore
import fr.forum_thalie.tsumugi.streamerNotificationService.WorkerStore
import fr.forum_thalie.tsumugi.streamerNotificationService.startStreamerMonitor

class BootBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, arg1: Intent) {
        Log.d(tag, "Broadcast Receiver received $arg1")
        // define preferenceStore for places of the program that needs to access Preferences without a context
        preferenceStore = PreferenceManager.getDefaultSharedPreferences(context)

        if (arg1.action == Intent.ACTION_BOOT_COMPLETED) {
            WorkerStore.instance.init(context)
            startStreamerMonitor(context) // will actually start it only if enabled in settings
            RadioAlarm.instance.setNextAlarm(context) // schedule next alarm
        }

        if (arg1.getStringExtra("action") == "fr.forum_thalie.tsumugi.${Actions.PLAY_OR_FALLBACK.name}" )
        {
            RadioAlarm.instance.setNextAlarm(context) // schedule next alarm
            if (PlayerStore.instance.streamerName.value.isNullOrBlank())
                PlayerStore.instance.initPicture(context)
            if (!PlayerStore.instance.isInitialized)
                PlayerStore.instance.initApi()

            val i = Intent(context, RadioService::class.java)
            i.putExtra("action", Actions.PLAY_OR_FALLBACK.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(i)
            else
                context.startService(i)
        }
    }
}