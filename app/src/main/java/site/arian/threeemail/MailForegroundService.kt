package site.arian.threeemail
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*

class MailForegroundService:Service(){
 companion object{const val START="site.arian.threeemail.START";private const val CHANNEL="3mail_monitor";private const val ID=3001}
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 override fun onCreate(){super.onCreate();if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"3MA1L Auto Reply",NotificationManager.IMPORTANCE_LOW));startForeground(ID,notification("Email monitor active"))}
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
  if(intent?.action==START&&!started){started=true;scope.launch{while(isActive){try{EmailEngine.poll(applicationContext);update("Last check completed")}catch(e:Exception){update("Check failed: ${e.message?.take(90) ?: "network/auth error"}")};delay(30_000L)}}}
  return START_STICKY
 }
 @Volatile private var started=false
 private fun notification(s:String):Notification{val b=if(Build.VERSION.SDK_INT>=26)Notification.Builder(this,CHANNEL) else Notification.Builder(this);return b.setContentTitle("3MA1L · AI Auto Reply").setContentText(s).setSmallIcon(android.R.drawable.ic_dialog_email).setOngoing(true).build()}
 private fun update(s:String){getSystemService(NotificationManager::class.java).notify(ID,notification(s))}
 override fun onDestroy(){scope.cancel();super.onDestroy()}
 override fun onBind(intent:Intent?):IBinder?=null
}
