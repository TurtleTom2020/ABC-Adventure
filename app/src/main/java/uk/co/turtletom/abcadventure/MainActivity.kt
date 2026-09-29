package uk.co.turtletom.abcadventure

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
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
private val springPics = listOf("🏪","🛹","🦸","🍩","🏠","👓","👴","🍩","🐭","🧢","🤡","🎷","💙","🥸","🚌","👩","🏛️","👦","🏙️","🌳","🍫","👨‍👩‍👦","👮","🩻","💛","🧟")

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
 private lateinit var tts: TextToSpeech
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); tts=TextToSpeech(this,this); setContent { App(this) { tts.speak(it,TextToSpeech.QUEUE_FLUSH,null,"abc") } } }
 override fun onInit(status:Int) { if(status==TextToSpeech.SUCCESS) tts.language=Locale.UK }
 override fun onDestroy(){ tts.shutdown(); super.onDestroy() }
}

@Composable fun App(context:Context,speak:(String)->Unit) {
 val prefs=remember{context.getSharedPreferences("abc_settings",Context.MODE_PRIVATE)}
 var themed by remember{mutableStateOf(prefs.getBoolean("springfield",false))}; var stars by remember{mutableIntStateOf(prefs.getInt("stars",0))}; var learned by remember{mutableStateOf(prefs.getStringSet("learned",emptySet())!!.toSet())}; val words=if(themed) springfield else normal
 val letters=('A'..'Z').mapIndexed{i,c->LetterItem(c,words[i],if(themed)springPics[i] else pics[i])}
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
 Spacer(Modifier.height(12.dp)); if(!themed){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp)){Image(painterResource(uk.co.turtletom.abcadventure.R.drawable.abc_logo_small),contentDescription="ABC Adventure",modifier=Modifier.fillMaxWidth().height(210.dp),contentScale=ContentScale.Fit)}}else{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFD600))){Column(Modifier.fillMaxWidth().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("⭐ SPRINGFIELD ABC ⭐",fontSize=30.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center);Text("Welcome to Springfield!",fontSize=17.sp,fontWeight=FontWeight.Bold)}}}
 Text(if(themed)"🍩  Learn • Laugh • Play  🍩" else "🔤 Learn  •  🔊 Listen  •  🎮 Play",fontSize=17.sp,fontWeight=FontWeight.Bold); Row(verticalAlignment=Alignment.CenterVertically){Text("ABC Adventure");Switch(checked=themed,onCheckedChange=setTheme);Text("Springfield")}; Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Text("⭐ "+stars+" stars     🏆 "+learned+"/26 letters",Modifier.padding(horizontal=20.dp,vertical=12.dp),fontSize=18.sp,fontWeight=FontWeight.Black)}
 BigButton("🔤  Learn A–Z"){go("learn")}; BigButton("🔊  Letter Sounds"){go("learn")}; BigButton("🎯  Find the Letter"){go("quiz")}; BigButton("🧩  Match It"){go("match")}; BigButton("✏️  Writing Practice"){go("tracepick")}
}}
@Composable fun BigButton(text:String,onClick:()->Unit)=Button(onClick,Modifier.fillMaxWidth().height(66.dp),shape=RoundedCornerShape(24.dp),elevation=ButtonDefaults.buttonElevation(defaultElevation=5.dp)){Text(text,fontSize=20.sp,fontWeight=FontWeight.Black)}
@Composable fun Letters(list:List<LetterItem>,learned:Set<String>,back:()->Unit,choose:(LetterItem)->Unit){Column(Modifier.fillMaxSize().padding(16.dp)){TextButton(back){Text("← Home")};Text("Choose a letter",fontSize=30.sp,fontWeight=FontWeight.Bold);LazyVerticalGrid(GridCells.Fixed(4),contentPadding=PaddingValues(vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(list){x->Card(Modifier.aspectRatio(1f).clickable{choose(x)},shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){SchoolLetter(x.letter,34);if(x.letter.toString() in learned)Text("⭐",fontSize=13.sp)}}}}}}}
@Composable fun LetterPage(x:LetterItem,speak:(String)->Unit,back:()->Unit,learned:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Letters")};Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(18.dp)){SchoolLetter(x.letter,76);Text(x.letter.lowercaseChar().toString(),fontSize=54.sp,fontWeight=FontWeight.Black)};SpringfieldArt(x.word,x.emoji);Text(x.letter.toString()+" is for "+x.word,fontSize=30.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);BigButton("🔊 Hear it"){speak(x.letter.toString()+". "+x.letter+" is for "+x.word)};BigButton("⭐ I learned this!"){learned()}}}
@Composable fun Quiz(list:List<LetterItem>,speak:(String)->Unit,reward:()->Unit,back:()->Unit){var target by remember{mutableStateOf(list.random())};var choices by remember{mutableStateOf((list.filter{it!=target}.shuffled().take(3)+target).shuffled())};fun next(){target=list.random();choices=(list.filter{it!=target}.shuffled().take(3)+target).shuffled()};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Home")};Text("Find the letter",fontSize=32.sp,fontWeight=FontWeight.Black);Text("Tap "+target.letter,fontSize=28.sp);Button({speak("Find the letter "+target.letter)}){Text("🔊 Listen")};choices.forEach{c->BigButton(c.letter.toString()){if(c==target){reward();speak("Well done!");next()}else speak("Try again")}}}}

@Composable fun MatchGame(list:List<LetterItem>,speak:(String)->Unit,reward:()->Unit,back:()->Unit){var target by remember{mutableStateOf(list.random())};var choices by remember{mutableStateOf((list.filter{it!=target}.shuffled().take(3)+target).shuffled())};fun next(){target=list.random();choices=(list.filter{it!=target}.shuffled().take(3)+target).shuffled()};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Home")};Text("🧩 Match It",fontSize=32.sp,fontWeight=FontWeight.Black);Text("Which word starts with "+target.letter+"?",fontSize=25.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);choices.forEach{c->BigButton(c.emoji+"  "+c.word){if(c==target){reward();speak("Brilliant! "+c.word+" starts with "+c.letter);next()}else speak("Nearly. Try another one")}}}}

@Composable fun TracePage(x:LetterItem,back:()->Unit,done:()->Unit){
 var points by remember(x.letter){mutableStateOf(listOf<Offset>())}
 var progress by remember(x.letter){mutableIntStateOf(0)}
 var misses by remember(x.letter){mutableIntStateOf(0)}
 var message by remember(x.letter){mutableStateOf("Start on the green dot and trace to the red dot")}
 fun guide(w:Float,h:Float)=if(x.letter=='I') listOf(Offset(w*.30f,h*.20f),Offset(w*.70f,h*.20f),Offset(w*.50f,h*.20f),Offset(w*.50f,h*.80f),Offset(w*.30f,h*.80f),Offset(w*.70f,h*.80f)) else listOf(Offset(w*.32f,h*.72f),Offset(w*.50f,h*.20f),Offset(w*.68f,h*.72f),Offset(w*.40f,h*.52f),Offset(w*.60f,h*.52f))
 Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
  TextButton(back,Modifier.align(Alignment.Start)){Text("← Letters")}
  Text("✏️ Trace "+x.letter,fontSize=30.sp,fontWeight=FontWeight.Black)
  Text(message,fontSize=17.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
  LinearProgressIndicator(progress={progress/100f},Modifier.fillMaxWidth().height(10.dp))
  Text(progress.toString()+"% complete",fontWeight=FontWeight.Bold)
  Box(Modifier.fillMaxWidth().height(360.dp).background(MaterialTheme.colorScheme.surfaceVariant,RoundedCornerShape(24.dp))){
   Text(x.letter.toString(),Modifier.align(Alignment.Center),fontSize=250.sp,fontWeight=FontWeight.Black,color=MaterialTheme.colorScheme.onSurface.copy(alpha=.13f))
   Canvas(Modifier.fillMaxSize().pointerInput(x.letter){
    detectDragGestures(
     onDragStart={p->points=listOf(p);progress=0;misses=0;message="Good — follow the dotted route"},
     onDrag={change,_->points=points+change.position},
     onDragEnd={message=if(progress>=85&&misses<12)"Brilliant tracing! ⭐" else "Nearly — stay on the guide and try again"}
    )
   }){
    val g=guide(size.width,size.height)
    g.forEachIndexed{i,p->drawCircle(if(i==0)Color(0xFF2EAD5B) else if(i==g.lastIndex)Color(0xFFE74C3C) else Color(0xFF7A7A7A),if(i==0||i==g.lastIndex)14f else 8f,p)}
    for(i in 0 until g.lastIndex) drawLine(Color(0xFF8B8B8B),g[i],g[i+1],5f,StrokeCap.Round)
    if(points.size>1){val path=Path();path.moveTo(points.first().x,points.first().y);points.drop(1).forEach{path.lineTo(it.x,it.y)};drawPath(path,Color(0xFF00A896),style=Stroke(width=18f,cap=StrokeCap.Round))
     var hit=0;var off=0
     points.forEach{p->val d=g.minOf{q->kotlin.math.sqrt((p.x-q.x)*(p.x-q.x)+(p.y-q.y)*(p.y-q.y))};if(d<95f)hit++ else off++}
     val calc=((hit.toFloat()/points.size)*100).toInt().coerceIn(0,100)
     if(calc!=progress||off/8!=misses){progress=calc;misses=off/8}
    }
   }
  }
  if(misses>2) Text("↩️ Move back towards the dotted guide",color=MaterialTheme.colorScheme.error,fontWeight=FontWeight.Bold)
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Button({points=emptyList();progress=0;misses=0;message="Start on the green dot and trace to the red dot"}){Text("↻ Clear")}
   Button({done();points=emptyList();progress=0;misses=0;message="⭐ Great job! Choose another when you're ready."},enabled=progress>=85&&misses<12){Text(if(progress>=85&&misses<12)"⭐ Claim star" else "🔒 Keep tracing")}
  }
 }
}

@Composable fun SchoolLetter(letter:Char,size:Int){if(letter.toString()=="I"){Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.width((size*0.65).dp).height(5.dp).background(MaterialTheme.colorScheme.onSurface));Box(Modifier.width(5.dp).height((size*0.72).dp).background(MaterialTheme.colorScheme.onSurface));Box(Modifier.width((size*0.65).dp).height(5.dp).background(MaterialTheme.colorScheme.onSurface))}}else Text(letter.toString(),fontSize=size.sp,fontWeight=FontWeight.Black)}

@Composable fun SpringfieldArt(name:String,fallback:String){
 val characterNames=setOf("Apu","Bart","Comic Book Guy","Flanders","Grandpa","Homer","Itchy","Jimbo","Krusty","Lisa","Marge","Ned","Otto","Patty","Quimby","Ralph","Üter","Van Houten","Wiggum")
 if(name !in characterNames){Text(fallback,fontSize=72.sp);return}
 Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF3B0))){
  Canvas(Modifier.size(190.dp).padding(12.dp)){drawSpringfieldFigure(name)}
 }
}
fun DrawScope.drawSpringfieldFigure(name:String){
 val w=size.width; val h=size.height
 val skin=Color(0xFFF2B56B); val ink=Color(0xFF303030)
 fun circle(x:Float,y:Float,r:Float,c:Color){drawCircle(c,r,Offset(w*x,h*y))}
 fun line(x1:Float,y1:Float,x2:Float,y2:Float,width:Float=8f,c:Color=ink){drawLine(c,Offset(w*x1,h*y1),Offset(w*x2,h*y2),width,StrokeCap.Round)}
 val body=when(name){"Homer"->Color(0xFFF4F4F4);"Bart"->Color(0xFFE74C3C);"Lisa"->Color(0xFFE85D3F);"Krusty"->Color(0xFF9C6ADE);"Wiggum"->Color(0xFF4A78C2);"Itchy"->Color(0xFF78A6B8);"Marge"->Color(0xFF62A9D8);else->Color(0xFF66A36F)}
 circle(.5f,.32f,.18f,if(name=="Itchy") Color(0xFF8799A6) else skin)
 drawRoundRect(body,Offset(w*.32f,h*.50f),androidx.compose.ui.geometry.Size(w*.36f,h*.35f),androidx.compose.ui.geometry.CornerRadius(22f,22f))
 circle(.44f,.29f,.025f,Color.White);circle(.56f,.29f,.025f,Color.White);circle(.44f,.29f,.010f,ink);circle(.56f,.29f,.010f,ink)
 line(.44f,.39f,.56f,.39f,5f)
 line(.38f,.82f,.34f,.98f);line(.62f,.82f,.66f,.98f)
 if(name=="Bart"){for(i in 0..5)line(.36f+i*.055f,.17f,.39f+i*.045f,.08f,6f)}
 if(name=="Homer"){line(.43f,.15f,.46f,.08f,4f);line(.49f,.14f,.52f,.07f,4f);circle(.76f,.68f,.10f,Color(0xFFD98B55));circle(.76f,.68f,.045f,Color(0xFFFFF3B0))}
 if(name=="Itchy"){circle(.34f,.16f,.075f,Color(0xFF8799A6));circle(.66f,.16f,.075f,Color(0xFF8799A6));line(.67f,.68f,.90f,.56f,5f,Color(0xFF8799A6))}
 if(name=="Lisa"){for(i in 0..7){val a=i*0.785f;line(.5f,.16f,.5f+kotlin.math.cos(a)*.18f,.16f+kotlin.math.sin(a)*.12f,6f)};line(.72f,.58f,.84f,.90f,9f,Color(0xFFC89B45))}
 if(name=="Marge"){drawOval(Color(0xFF4E73C8),Offset(w*.37f,-h*.05f),androidx.compose.ui.geometry.Size(w*.26f,h*.32f))}
 if(name=="Krusty"){circle(.32f,.20f,.09f,Color(0xFF6FA66F));circle(.68f,.20f,.09f,Color(0xFF6FA66F));circle(.5f,.35f,.035f,Color(0xFFE34C4C))}
 if(name=="Wiggum"){drawRect(Color(0xFF315A99),Offset(w*.34f,h*.10f),androidx.compose.ui.geometry.Size(w*.32f,h*.07f));circle(.5f,.13f,.035f,Color(0xFFE3B341))}
 if(name=="Flanders"||name=="Ned"){line(.40f,.38f,.60f,.38f,10f,Color(0xFF6B3F24));line(.38f,.28f,.47f,.28f,3f);line(.53f,.28f,.62f,.28f,3f)}
}
