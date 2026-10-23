package site.arian.threeemail
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity:ComponentActivity(){
 private val lime=Color.rgb(183,243,74);private val ink=Color.rgb(24,48,24);private val muted=Color.rgb(101,114,96)
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main);private lateinit var root:LinearLayout;private lateinit var inbox:LinearLayout;private var offset=0
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
 root=LinearLayout(this).apply{orientation=1;setPadding(dp(18),dp(18),dp(18),dp(24));setBackgroundColor(Color.rgb(249,252,246))}
 setContentView(ScrollView(this).apply{addView(root)});build()
 }
 private fun build(){
  val logo=ImageView(this).apply{val id=resources.getIdentifier("three_mail_logo","drawable",packageName);if(id!=0)setImageResource(id)else setImageResource(android.R.drawable.ic_dialog_email);background=rounded(lime,20);setPadding(dp(8),dp(8),dp(8),dp(8));scaleType=ImageView.ScaleType.FIT_CENTER}
  root.addView(logo,LinearLayout.LayoutParams(dp(76),dp(76)).apply{gravity=Gravity.CENTER_HORIZONTAL})
  root.addView(label("3MA1L",30,ink,true).apply{gravity=Gravity.CENTER_HORIZONTAL})
  root.addView(label("Premium AI Email Assistant",14,muted,false).apply{gravity=Gravity.CENTER_HORIZONTAL;setPadding(0,0,0,dp(12))})
  card { addView(label("Gmail + Gemini settings",18,ink,true))
   val em=edit("Gmail address",EmailEngine.email(this@MainActivity));val pw=edit("Gmail App Password", "");pw.inputType=129
   val key=edit("Gemini API key","");key.inputType=129
   val model=edit("Gemini model","gemini-2.5-flash")
   val enabled=CheckBox(this@MainActivity).apply{text="Enable automatic AI replies";setTextColor(ink);isChecked=EmailEngine.enabled(this@MainActivity)}
   listOf(em,pw,key,model).forEach{addView(it,match().apply{topMargin=dp(8)})};addView(enabled)
   addView(button("Save settings"){EmailEngine.save(this@MainActivity,em.text.toString(),pw.text.toString(),key.text.toString(),model.text.toString(),enabled.isChecked);toast("Settings saved locally.")})
   addView(button("Start 30-second monitor"){EmailEngine.save(this@MainActivity,em.text.toString(),pw.text.toString(),key.text.toString(),model.text.toString(),enabled.isChecked);ContextCompat.startForegroundService(this@MainActivity,Intent(this@MainActivity,MailForegroundService::class.java).setAction(MailForegroundService.START))}.apply{setPadding(0,dp(7),0,0)})
   addView(button("Stop monitor"){stopService(Intent(this@MainActivity,MailForegroundService::class.java))}.apply{setPadding(0,dp(7),0,0)})
   addView(label("Use a Gmail App Password, not your normal password. Automatic replies send email without manual approval when enabled.",12,muted,false).apply{setPadding(0,dp(8),0,0)})
  }
  card {addView(label("Inbox",18,ink,true));inbox=LinearLayout(this).apply{orientation=1};addView(inbox,match().apply{topMargin=dp(8)})
   addView(button("Refresh inbox"){offset=0;loadInbox(true)}.apply{setPadding(0,dp(7),0,0)})
   addView(button("Load 50 more"){loadInbox(false)}.apply{setPadding(0,dp(7),0,0)})
  }
 }
 private fun loadInbox(clear:Boolean){if(clear)inbox.removeAllViews();inbox.addView(label("Loading…",13,muted,false));scope.launch{try{
  val items=withContext(Dispatchers.IO){EmailEngine.inbox(this@MainActivity,offset,50)};inbox.removeAllViews()
  if(items.isEmpty())inbox.addView(label("No more messages.",13,muted,false))
  items.forEach{m->val box=LinearLayout(this@MainActivity).apply{orientation=1;setPadding(dp(10),dp(10),dp(10),dp(10));background=rounded(Color.rgb(247,251,239),12);addView(label(m.subject,15,ink,true));addView(label("${m.from}\n${m.date}",12,muted,false))}
   inbox.addView(box,match().apply{bottomMargin=dp(7)})}
  offset+=items.size
 }catch(e:Exception){inbox.removeAllViews();inbox.addView(label("Inbox error: ${e.message ?: "Check Gmail settings/network"}",13,Color.RED,false))}}}
 private fun card(f:LinearLayout.()->Unit){val v=LinearLayout(this).apply{orientation=1;setPadding(dp(14),dp(14),dp(14),dp(14));background=rounded(Color.WHITE,20);elevation=dp(2).toFloat();f()};root.addView(v,match().apply{topMargin=dp(12)})}
 private fun edit(h:String,v:String)=EditText(this).apply{hint=h;if(v.isNotBlank())setText(v);background=rounded(Color.rgb(249,252,246),12);setPadding(dp(10),dp(10),dp(10),dp(10))}
 private fun label(t:String,s:Int,c:Int,b:Boolean)=TextView(this).apply{text=t;textSize=s.toFloat();setTextColor(c);if(b)setTypeface(typeface,Typeface.BOLD)}
 private fun button(t:String,f:()->Unit)=Button(this).apply{text=t;setTextColor(ink);background=rounded(lime,13);setOnClickListener{f()}}
 private fun rounded(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
 private fun match()=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun toast(t:String)=Toast.makeText(this,t,Toast.LENGTH_LONG).show()
 override fun onDestroy(){scope.cancel();super.onDestroy()}
}
