package com.example.bornwish

import android.R
import android.annotation.TargetApi
import android.app.Notification
import android.app.Notification.PRIORITY_HIGH
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log


class ContactBDay : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals("StartForeground")) {
            startForegroundService()
            val r = Runnable {
                val mcontentResolver = contentResolver
                val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = mcontentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null)
                if (cursor!!.count > 0) {
                    while (cursor.moveToNext()) {
                        val contactInfoMap = HashMap<String, String>()
                        val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID))
                        val contactDisplayName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))

                        val columns = arrayOf(
                            ContactsContract.CommonDataKinds.Event.START_DATE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.MIMETYPE
                        )

                        val where =
                            ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                                    " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId

                        val selectionArgs: Array<String>? = null
                        val sortOrder = ContactsContract.Contacts.DISPLAY_NAME

                        val birthdayCur = mcontentResolver.query(
                            ContactsContract.Data.CONTENT_URI,
                            columns,
                            where,
                            selectionArgs,
                            sortOrder
                        )
                        if (birthdayCur!!.getCount() > 0) {
                            while (birthdayCur.moveToNext()) {
                                val birthday =
                                    birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE))
                                Log.e(
                                    "Main disp with bday ",
                                    contactDisplayName + "'s bday is " + birthday + " Contact's Id " + contactId
                                )
                            }
                        }
                        birthdayCur.close()
                    }
                }
                cursor.close()
                stopForeground(true)
                stopSelf()
            }
            val queryThread = Thread(r)
            queryThread.start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun startForegroundService() {
        Log.d("ContactBDay", "Start foreground service.")

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentText("App is running")
            .setPriority(PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}