package com.diazz27kryt.businessempireassistant

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Ink = Color(0xFF101114)
private val Glass = Color(0xFF202228)
private val Muted = Color(0xFF9A9DA7)
private val White = Color(0xFFF5F5F7)

data class Stock(val name: String, val price: Double, val note: String)
data class Venture(val name: String, val cost: Double, val income: Double, val expense: Double)
data class Asset(val name: String, val kind: String, val value: Double, val income: Double)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Glass, primary = White)) { AssistantApp(this) } }
    }
}

private fun readRows(context: Context, key: String): JSONArray =
    try { JSONArray(context.getSharedPreferences("empire", Context.MODE_PRIVATE).getString(key, "[]")) } catch (_: Exception) { JSONArray() }

private fun saveStocks(context: Context, rows: List<Stock>) {
    val a = JSONArray()
    rows.forEach { a.put(JSONObject().put("name", it.name).put("price", it.price).put("note", it.note)) }
    context.getSharedPreferences("empire", Context.MODE_PRIVATE).edit().putString("stocks", a.toString()).apply()
}
private fun saveVentures(context: Context, rows: List<Venture>) {
    val a = JSONArray()
    rows.forEach { a.put(JSONObject().put("name", it.name).put("cost", it.cost).put("income", it.income).put("expense", it.expense)) }
    context.getSharedPreferences("empire", Context.MODE_PRIVATE).edit().putString("ventures", a.toString()).apply()
}
private fun saveAssets(context: Context, rows: List<Asset>) {
    val a = JSONArray()
    rows.forEach { a.put(JSONObject().put("name", it.name).put("kind", it.kind).put("value", it.value).put("income", it.income)) }
    context.getSharedPreferences("empire", Context.MODE_PRIVATE).edit().putString("assets", a.toString()).apply()
}
private fun money(v: Double) = "%,.2f".format(java.util.Locale.US, v).replace(",", " ").replace('.', ',')

@Composable
private fun AssistantApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("empire", Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf("Главная") }
    var cash by remember { mutableStateOf(prefs.getString("cash", "1000000") ?: "1000000") }
    val stocks = remember { mutableStateListOf<Stock>().apply {
        val a = readRows(context, "stocks"); for (i in 0 until a.length()) { val o=a.optJSONObject(i) ?: continue; add(Stock(o.optString("name"),o.optDouble("price"),o.optString("note"))) }
    } }
    val ventures = remember { mutableStateListOf<Venture>().apply {
        val a=readRows(context,"ventures"); for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(Venture(o.optString("name"),o.optDouble("cost"),o.optDouble("income"),o.optDouble("expense")))}
    } }
    val assets = remember { mutableStateListOf<Asset>().apply {
        val a=readRows(context,"assets"); for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;add(Asset(o.optString("name"),o.optString("kind"),o.optDouble("value"),o.optDouble("income")))}
    } }
    val tabs = listOf("Главная", "Биржа", "Бизнес", "Активы")
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF252831), Ink))).padding(16.dp)) {
        Text("BUSINESS EMPIRE", color=Muted, fontSize=11.sp, letterSpacing=2.sp)
        Text("Assistant", color=White, fontSize=29.sp, fontWeight=FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp), modifier=Modifier.fillMaxWidth()) {
            tabs.forEach { item -> FilterChip(selected=tab==item,onClick={tab=item},label={Text(item,fontSize=12.sp)}) }
        }
        Spacer(Modifier.height(12.dp))
        when(tab) {
            "Главная" -> HomeScreen(cash, { cash=it; prefs.edit().putString("cash",it).apply() }, stocks, ventures, assets)
            "Биржа" -> StocksScreen(context, stocks)
            "Бизнес" -> BusinessScreen(context, ventures)
            else -> AssetsScreen(context, assets)
        }
        Spacer(Modifier.height(8.dp))
        Text("Все значения вводятся вручную из твоей игры. Это не реальные биржевые котировки.",color=Muted,fontSize=10.sp)
    }
}

@Composable
private fun HomeScreen(cash:String,onCash:(String)->Unit,stocks:List<Stock>,ventures:List<Venture>,assets:List<Asset>) {
    val totalIncome=ventures.sumOf{it.income-it.expense}+assets.sumOf{it.income}
    LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { GlassCard {
            Text("ИГРОВОЙ КАПИТАЛ",color=Muted,fontSize=11.sp,letterSpacing=1.sp)
            OutlinedTextField(value=cash,onValueChange={onCash(it.filter{c->c.isDigit()}.take(15))},label={Text("Деньги в игре")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
            Text("₽  ${cash.ifBlank{"0"}}",color=White,fontSize=27.sp,fontWeight=FontWeight.Bold)
        } }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            MetricCard("АКТИВОВ", "${assets.size+ventures.size}", Modifier.weight(1f))
            MetricCard("ПОЗИЦИЙ", "${stocks.size}", Modifier.weight(1f))
        } }
        item { GlassCard {
            Text("ПАССИВНЫЙ ДОХОД",color=Muted,fontSize=11.sp)
            Text("${money(totalIncome)} ₽ / период",color=White,fontSize=22.sp,fontWeight=FontWeight.Bold)
            Text("Расчёт по внесённым тобой данным бизнеса и активов.",color=Muted,fontSize=12.sp)
        } }
        item { GlassCard {
            Text("БЫСТРЫЙ ОБЗОР",color=White,fontSize=18.sp,fontWeight=FontWeight.SemiBold)
            Text("Бизнесов: ${ventures.size} · Активов: ${assets.size} · Записей биржи: ${stocks.size}",color=Muted)
        } }
        item { GlassCard {
            Text("АНАЛИТИКА",color=White,fontSize=18.sp,fontWeight=FontWeight.SemiBold)
            Text(if(stocks.size<2) "Добавь хотя бы два наблюдения по одной акции, чтобы сравнить изменение цены." else "Последняя цена каждой записи сохранена. Сравнивай наблюдения вручную: приложение не выдумывает котировки.",color=Muted)
        } }
    }
}

@Composable
private fun StocksScreen(context:Context,rows:SnapshotStateList<Stock>) {
    var name by remember { mutableStateOf("") }; var price by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }
    LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { GlassCard {
            Text("НОВОЕ НАБЛЮДЕНИЕ",color=White,fontSize=18.sp,fontWeight=FontWeight.Bold)
            EntryField(name,{name=it},"Название акции")
            EntryField(price,{price=it},"Цена")
            EntryField(note,{note=it},"Заметка (необязательно)")
            Button(onClick={val p=price.replace(',','.').toDoubleOrNull();if(name.isNotBlank()&&p!=null&&p>=0){rows.add(0,Stock(name.trim(),p,note.trim()));saveStocks(context,rows);name="";price="";note=""}},modifier=Modifier.fillMaxWidth()) { Text("Сохранить цену") }
        } }
        if(rows.isEmpty()) item { EmptyCard("Пока нет наблюдений. Добавь название акции и цену из игры.") }
        items(rows) { row ->
            GlassCard {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(row.name,color=White,fontWeight=FontWeight.Bold,fontSize=17.sp); Text(row.note.ifBlank{"Наблюдение цены"},color=Muted,fontSize=12.sp) }
                    Text("${money(row.price)} ₽",color=White,fontWeight=FontWeight.Bold)
                }
                OutlinedButton(onClick={rows.remove(row);saveStocks(context,rows)}) { Text("Удалить запись") }
            }
        }
    }
}

@Composable
private fun BusinessScreen(context:Context,rows:SnapshotStateList<Venture>) {
    var name by remember { mutableStateOf("") };var cost by remember { mutableStateOf("") };var income by remember { mutableStateOf("") };var expense by remember { mutableStateOf("") }
    LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { GlassCard {
            Text("ДОБАВИТЬ БИЗНЕС",color=White,fontSize=18.sp,fontWeight=FontWeight.Bold)
            EntryField(name,{name=it},"Название бизнеса")
            EntryField(cost,{cost=it},"Стоимость покупки")
            EntryField(income,{income=it},"Доход за период")
            EntryField(expense,{expense=it},"Расходы за период")
            Button(onClick={val c=num(cost);val i=num(income);val e=num(expense);if(name.isNotBlank()&&c!=null&&i!=null&&e!=null&&c>=0&&i>=0&&e>=0){rows.add(0,Venture(name.trim(),c,i,e));saveVentures(context,rows);name="";cost="";income="";expense=""}},modifier=Modifier.fillMaxWidth()){Text("Добавить бизнес")}
        } }
        if(rows.isEmpty()) item { EmptyCard("Добавь бизнес, чтобы считать чистую прибыль и срок окупаемости.") }
        items(rows) { v ->
            val profit=v.income-v.expense
            GlassCard {
                Text(v.name,color=White,fontSize=17.sp,fontWeight=FontWeight.Bold)
                Text("Чистая прибыль: ${money(profit)} ₽ / период",color=if(profit>=0) White else Color(0xFFFF8A80))
                Text("Окупаемость: ${if(profit>0) "${money(v.cost/profit)} периодов" else "пока не окупается"}",color=Muted)
                Text("Покупка: ${money(v.cost)} ₽ · Доход: ${money(v.income)} ₽ · Расходы: ${money(v.expense)} ₽",color=Muted,fontSize=12.sp)
                OutlinedButton(onClick={rows.remove(v);saveVentures(context,rows)}){Text("Удалить")}
            }
        }
    }
}

@Composable
private fun AssetsScreen(context:Context,rows:SnapshotStateList<Asset>) {
    var name by remember { mutableStateOf("") };var kind by remember { mutableStateOf("Недвижимость") };var value by remember { mutableStateOf("") };var income by remember { mutableStateOf("") }
    LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { GlassCard {
            Text("ДОБАВИТЬ АКТИВ",color=White,fontSize=18.sp,fontWeight=FontWeight.Bold)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=kind=="Недвижимость",onClick={kind="Недвижимость"},label={Text("Недвижимость")});FilterChip(selected=kind=="Автомобиль",onClick={kind="Автомобиль"},label={Text("Авто")})}
            EntryField(name,{name=it},"Название актива")
            EntryField(value,{value=it},"Стоимость")
            EntryField(income,{income=it},"Доход за период (если есть)")
            Button(onClick={val v=num(value);val i=num(income.ifBlank{"0"});if(name.isNotBlank()&&v!=null&&i!=null&&v>=0&&i>=0){rows.add(0,Asset(name.trim(),kind,v,i));saveAssets(context,rows);name="";value="";income=""}},modifier=Modifier.fillMaxWidth()){Text("Добавить актив")}
        } }
        if(rows.isEmpty()) item { EmptyCard("Здесь будут твои дома, квартиры и автомобили из Business Empire.") }
        items(rows) { a ->
            GlassCard {
                Text(a.kind.uppercase(),color=Muted,fontSize=10.sp,letterSpacing=1.sp)
                Text(a.name,color=White,fontSize=17.sp,fontWeight=FontWeight.Bold)
                Text("Стоимость: ${money(a.value)} ₽",color=White)
                Text("Доход: ${money(a.income)} ₽ / период",color=Muted)
                OutlinedButton(onClick={rows.remove(a);saveAssets(context,rows)}){Text("Удалить")}
            }
        }
    }
}

private fun num(s:String)=s.replace(" ","").replace(',','.').toDoubleOrNull()

@Composable
private fun EntryField(value:String,onValue:(String)->Unit,label:String) {
    OutlinedTextField(value=value,onValueChange=onValue,label={Text(label)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=if(label.contains("Название")||label.contains("Заметка")) KeyboardType.Text else KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
}
@Composable
private fun MetricCard(label:String,value:String,modifier:Modifier=Modifier) {
    Surface(modifier=modifier,color=Glass,shape=RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp)) { Text(label,color=Muted,fontSize=10.sp);Text(value,color=White,fontSize=23.sp,fontWeight=FontWeight.Bold) } }
}
@Composable
private fun EmptyCard(message:String) { GlassCard { Text(message,color=Muted) } }
@Composable
private fun GlassCard(content:@Composable ColumnScope.()->Unit) {
    Surface(color=Glass.copy(alpha=0.92f),shape=RoundedCornerShape(22.dp),tonalElevation=4.dp,modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp),content=content)
    }
}
