package uk.co.turtletom.abcadventure

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); tts=TextToSpeech(this,this); setContent { App { tts.speak(it,TextToSpeech.QUEUE_FLUSH,null,"abc") } } }
 override fun onInit(status:Int) { if(status==TextToSpeech.SUCCESS) tts.language=Locale.UK }
 override fun onDestroy(){ tts.shutdown(); super.onDestroy() }
}

@Composable fun App(speak:(String)->Unit) {
 val themed=BuildConfig.EDITION=="SPRINGFIELD"; val words=if(themed) springfield else normal
 val letters=('A'..'Z').mapIndexed{i,c->LetterItem(c,words[i],if(themed)"⭐" else pics[i])}
 var page by remember{mutableStateOf("home")}; var chosen by remember{mutableStateOf(letters.first())}; var stars by remember{mutableIntStateOf(0)}
 MaterialTheme(colorScheme=if(themed) lightColorScheme(primary=Color(0xFF1976D2),secondary=Color(0xFFFFD600)) else lightColorScheme(primary=Color(0xFF5B5BD6))) {
  Surface(Modifier.fillMaxSize()) { when(page) {
   "home"->Home(themed,stars){page=it}
   "learn"->Letters(letters,{page="home"}){chosen=it;page="letter"}
   "letter"->LetterPage(chosen,speak,{page="learn"}){stars++;page="learn"}
   else->Quiz(letters,speak,{stars++},{page="home"})
  }}
 }
}
@Composable fun Home(themed:Boolean,stars:Int,go:(String)->Unit){ Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){
 Spacer(Modifier.height(20.dp)); Text(if(themed)"Springfield ABC" else "ABC Adventure",fontSize=36.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
 Text(if(themed)"A fun Springfield alphabet" else "Learn • Listen • Play",fontSize=18.sp); Text("⭐ "+stars+" stars",fontSize=22.sp,fontWeight=FontWeight.Bold)
 BigButton("🔤  Learn A–Z"){go("learn")}; BigButton("🔊  Letter Sounds"){go("learn")}; BigButton("🎯  Find the Letter"){go("quiz")}; Text("Tracing and matching coming next",color=Color.Gray)
}}
@Composable fun BigButton(text:String,onClick:()->Unit)=Button(onClick,Modifier.fillMaxWidth().height(72.dp),shape=RoundedCornerShape(22.dp)){Text(text,fontSize=22.sp)}
@Composable fun Letters(list:List<LetterItem>,back:()->Unit,choose:(LetterItem)->Unit){Column(Modifier.fillMaxSize().padding(16.dp)){TextButton(back){Text("← Home")};Text("Choose a letter",fontSize=30.sp,fontWeight=FontWeight.Bold);LazyVerticalGrid(GridCells.Fixed(4),contentPadding=PaddingValues(vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(list){x->Card(Modifier.aspectRatio(1f).clickable{choose(x)}){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(x.letter.toString(),fontSize=34.sp,fontWeight=FontWeight.Black)}}}}}}
@Composable fun LetterPage(x:LetterItem,speak:(String)->Unit,back:()->Unit,learned:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Letters")};Text(x.letter.toString()+" "+x.letter.lowercaseChar(),fontSize=76.sp,fontWeight=FontWeight.Black);Text(x.emoji,fontSize=72.sp);Text(x.letter.toString()+" is for "+x.word,fontSize=30.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);BigButton("🔊 Hear it"){speak(x.letter.toString()+". "+x.letter+" is for "+x.word)};BigButton("⭐ I learned this!"){learned()}}}
@Composable fun Quiz(list:List<LetterItem>,speak:(String)->Unit,reward:()->Unit,back:()->Unit){var target by remember{mutableStateOf(list.random())};var choices by remember{mutableStateOf((list.filter{it!=target}.shuffled().take(3)+target).shuffled())};fun next(){target=list.random();choices=(list.filter{it!=target}.shuffled().take(3)+target).shuffled()};Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)){TextButton(back,Modifier.align(Alignment.Start)){Text("← Home")};Text("Find the letter",fontSize=32.sp,fontWeight=FontWeight.Black);Text("Tap "+target.letter,fontSize=28.sp);Button({speak("Find the letter "+target.letter)}){Text("🔊 Listen")};choices.forEach{c->BigButton(c.letter.toString()){if(c==target){reward();speak("Well done!");next()}else speak("Try again")}}}}
