package uk.co.turtletom.abcadventure

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class LetterItem(val letter: Char, val word: String, val emoji: String)
private val normal = listOf("Apple","Ball","Cat","Dog","Elephant","Fish","Grapes","House","Ice cream","Juice","Kite","Lion","Moon","Nest","Orange","Penguin","Queen","Rainbow","Sun","Tiger","Umbrella","Van","Whale","Xylophone","Yo-yo","Zebra")
private val pics = listOf("🍎","⚽","🐱","🐶","🐘","🐟","🍇","🏠","🍦","🧃","🪁","🦁","🌙","🪺","🍊","🐧","👑","🌈","☀️","🐯","☂️","🚐","🐳","🎵","🪀","🦓")
private val springfield = listOf("Apu","Bart","Comic Book Guy","Donut","Evergreen Terrace","Flanders","Grandpa","Homer","Itchy","Jimbo","Krusty","Lisa","Marge","Ned","Otto","Patty","Quimby","Ralph","Springfield","Treehouse","Üter","Van Houten","Wiggum","X-ray","Yellow","Zombie")

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
 private lateinit var tts: TextToSpeech
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); tts=TextToSpeech(this,this); setContent { App(this) { tts.speak(it,TextToSpeech.QUEUE_FLUSH,null,"abc") } } }
 override fun onInit(status:Int) { if(status==TextToSpeech.SUCCESS) tts.language=Locale.UK }
 override fun onDestroy(){ tts.shutdown(); super.onDestroy() }
}

@Composable fun App(context:Context,speak:(String)->Unit) {
 val prefs=remember{context.getSharedPreferences("abc_settings",Context.MODE_PRIVATE)}
 var themed by remember{mutableStateOf(prefs.getBoolean("springfield",false))}; var stars by remember{mutableIntStateOf(prefs.getInt("stars",0))}; var learned by remember{mutableStateOf(prefs.getStringSet("learned",emptySet())!!.toSet())}; val words=if(themed) springfield else normal
 val letters=('A'..'Z').mapIndexed{i,c->LetterItem(c,words[i],if(themed)"⭐" else pics[i])}
 var page by remember{mutableStateOf("home")}; var chosen by remember{mutableStateOf(letters.first())}
 MaterialTheme(colorScheme=if(themed) lightColorScheme(primary=Color(0xFF1565C0),onPrimary=Color.White,secondary=Color(0xFFFFD600),tertiary=Color(0xFFE53935),background=Color(0xFFFFF8D6),surface=Color(0xFFFFFFFF)) else lightColorScheme(primary=Color(0xFF6C4DFF),onPrimary=Color.White,secondary=Color(0xFFFF8A65),tertiary=Color(0xFF00A896),background=Color(0xFFF7F4FF),surface=Color.White)) {
  Surface(Modifier.fillMaxSize()) { when(page) {
   "home"->Home(themed,stars,learned.size,{ page=it },{ themed=it; prefs.edit().putBoolean("springfield",it).apply() })
   "learn"->Letters(letters,learned,{page="home"}){chosen=it;page="letter"}
   "letter"->LetterPage(chosen,speak,{page="learn"}){stars++;learned=learned+chosen.letter.toString();prefs.edit().putInt("stars",stars).putStringSet("learned",learned).apply();page="learn"}
   "quiz"->Quiz(letters,speak,{stars++;prefs.edit().putInt("stars",stars).apply()},{page="home"})
   "tracepick"->Letters(letters,learned,{page="home"}){chosen=it;page="trace"}
   "trace"->TracePage(chosen,{page="tracepick"}){stars++;learned=learned+chosen.letter.toString();prefs.edit().putInt("stars",stars).putStringSet("learned",learned).apply()}
   else->MatchGame(letters,speak,{stars++;prefs.edit().putInt("stars",stars).apply()},{page="home"})
  }}
 }
}
@Composable fun Home(themed:Boolean,stars:Int,learned:Int,go:(String)->Unit,setTheme:(Boolean)->Unit){ Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){
 Spacer(Modifier.height(12.dp)); Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=if(themed) Color(0xFFFFD600) else MaterialTheme.colorScheme.primaryContainer)){Column(Modifier.fillMaxWidth().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(if(themed)"⭐ SPRINGFIELD ABC ⭐" else "🌈 ABC ADVENTURE 🌈",fontSize=30.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center);Text(if(themed)"Welcome to Springfield!" else "Let’s learn something brilliant!",fontSize=17.sp,fontWeight=FontWeight.Bold)}}
 Text(if(themed)"🍩  Learn • Laugh • Play  🍩" else "🔤 Learn  •  🔊 Listen  •  🎮 Play",fontSize=17.sp,fontWeight=FontWeight.Bold); Row(verticalAlignment=Alignment.CenterVertically){Text("ABC Adventure");Switch(checked=themed,onCheckedChange=setTheme);Text("Springfield")}; Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Text("⭐ "+stars+" stars     🏆 "+learned+"/26 letters",Modifier.padding(horizontal=20.dp,vertical=12.dp),fontSize=18.sp,fontWeight=FontWeight.Black)}
 BigButton("🔤  Learn A–Z"){go("learn")}; BigButton("🔊  Letter Sounds"){go("learn")}; BigButton("🎯  Find the Letter"){go("quiz")}; BigButton("🧩  Match It"){go("match")}; BigButton("✏️  Writing Practice"){go("tracepick")}
}}
@Composable fun BigButton(text:String,onClick:()->Unit)=Button(onClick,Modifier.fillMaxWidth().height(66.dp),shape=RoundedCornerShape(24.dp),elevation=ButtonDefaults.buttonElevation(defaultElevation=5.dp)){Text(text,fontSize=20.sp,fontWeight=FontWeight.Black)}
@Composable fun Letters(list:List<LetterItem>,learned:Set<String>,back:()->Unit,choose:(LetterItem)->Unit){Column(Modifier.fillMaxSize().padding(16.dp)){TextButton(back){Text("← Home")};Text("Choose a letter",fontSize=30.sp,fontWeight=FontWeight.Bold);LazyVerticalGrid(GridCells.Fixed(4),contentPadding=PaddingValues(vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(list){x->Card(Modifier.aspectRatio(1f).clickable{choose(x)},shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(x.letter.toString(),fontSize=34.sp,fontWeight=FontWeight.Black);if(x.letter.toString() in learned)Text("⭐",fontSize=13.sp)}}}}}}}
@Composable fun LetterPage(x:LetterItem,speak:(String)->Unit,back:()->Unit,learned:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Letters")};Text(x.letter.toString()+" "+x.letter.lowercaseChar(),fontSize=76.sp,fontWeight=FontWeight.Black);Text(x.emoji,fontSize=72.sp);Text(x.letter.toString()+" is for "+x.word,fontSize=30.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);BigButton("🔊 Hear it"){speak(x.letter.toString()+". "+x.letter+" is for "+x.word)};BigButton("⭐ I learned this!"){learned()}}}
@Composable fun Quiz(list:List<LetterItem>,speak:(String)->Unit,reward:()->Unit,back:()->Unit){var target by remember{mutableStateOf(list.random())};var choices by remember{mutableStateOf((list.filter{it!=target}.shuffled().take(3)+target).shuffled())};fun next(){target=list.random();choices=(list.filter{it!=target}.shuffled().take(3)+target).shuffled()};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Home")};Text("Find the letter",fontSize=32.sp,fontWeight=FontWeight.Black);Text("Tap "+target.letter,fontSize=28.sp);Button({speak("Find the letter "+target.letter)}){Text("🔊 Listen")};choices.forEach{c->BigButton(c.letter.toString()){if(c==target){reward();speak("Well done!");next()}else speak("Try again")}}}}

@Composable fun MatchGame(list:List<LetterItem>,speak:(String)->Unit,reward:()->Unit,back:()->Unit){var target by remember{mutableStateOf(list.random())};var choices by remember{mutableStateOf((list.filter{it!=target}.shuffled().take(3)+target).shuffled())};fun next(){target=list.random();choices=(list.filter{it!=target}.shuffled().take(3)+target).shuffled()};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Home")};Text("🧩 Match It",fontSize=32.sp,fontWeight=FontWeight.Black);Text("Which word starts with "+target.letter+"?",fontSize=25.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);choices.forEach{c->BigButton(c.emoji+"  "+c.word){if(c==target){reward();speak("Brilliant! "+c.word+" starts with "+c.letter);next()}else speak("Nearly. Try another one")}}}}

@Composable fun TracePage(x:LetterItem,back:()->Unit,done:()->Unit){var points by remember(x.letter){mutableStateOf(listOf<Offset>())};Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Letters")};Text("✏️ Trace "+x.letter,fontSize=30.sp,fontWeight=FontWeight.Black);Text("Follow the big letter with your finger",fontSize=17.sp);Box(Modifier.fillMaxWidth().height(360.dp).background(MaterialTheme.colorScheme.surfaceVariant,RoundedCornerShape(24.dp))){Text(x.letter.toString(),Modifier.align(Alignment.Center),fontSize=250.sp,fontWeight=FontWeight.Black,color=MaterialTheme.colorScheme.onSurface.copy(alpha=.12f));Canvas(Modifier.fillMaxSize().pointerInput(x.letter){detectDragGestures(onDragStart={points=listOf(it)},onDrag={change,_->points=points+change.position})}){if(points.size>1){val path=Path();path.moveTo(points.first().x,points.first().y);points.drop(1).forEach{path.lineTo(it.x,it.y)};drawPath(path,Color(0xFF00A896),style=Stroke(width=18f,cap=StrokeCap.Round))}}};Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Button({points=emptyList()}){Text("↻ Clear")};Button({done();points=emptyList()}){Text("⭐ Done!")}}}}
