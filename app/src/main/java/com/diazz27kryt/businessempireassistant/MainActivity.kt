package com.diazz27kryt.businessempireassistant

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

private val Night = Color(0xFF080A12)
private val Panel = Color(0xFF171B29)
private val Muted = Color(0xFF9AA3B8)
private val Accent = Color(0xFFE7EAF4)
private val Green = Color(0xFF7BE0B0)
private val Red = Color(0xFFFF8585)

private data class Business(val id: String, val name: String, val description: String, val cost: Double, val baseIncome: Double, val icon: String)
private data class Stock(val ticker: String, val name: String, val start: Double)

private val businesses = listOf(
    Business("coffee", "Кофейня", "Небольшой бизнес с постоянным потоком клиентов", 1_000.0, 18.0, "☕"),
    Business("market", "Мини-маркет", "Магазин товаров повседневного спроса", 5_000.0, 75.0, "🏪"),
    Business("wash", "Автомойка", "Сервис с ежедневной выручкой", 18_000.0, 260.0, "🚘"),
    Business("studio", "IT-студия", "Разработка цифровых продуктов", 75_000.0, 1_250.0, "💻"),
    Business("factory", "Завод", "Крупное производство", 350_000.0, 6_500.0, "🏭"),
    Business("space", "Космическая корпорация", "Технологии следующего уровня", 2_000_000.0, 42_000.0, "🚀")
)
private val stocks = listOf(
    Stock("NOVA", "Nova Technologies", 120.0),
    Stock("ORBT", "Orbit Systems", 86.0),
    Stock("POLR", "Polar Energy", 54.0),
    Stock("ATLS", "Atlas Industries", 210.0)
)
private fun cashText(value: Double): String = "$" + String.format(Locale.US, "%,.0f", value)
private fun Context.prefs() = getSharedPreferences("business_empire_game", Context.MODE_PRIVATE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 10, 18)
        window.navigationBarColor = android.graphics.Color.rgb(8, 10, 18)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Night, surface = Panel)) { BusinessEmpireGame(this) } }
    }
}

@Composable
private fun BusinessEmpireGame(context: Context) {
    val prefs = remember { context.prefs() }
    var cash by remember { mutableDoubleStateOf(prefs.getString("cash", "10000")?.toDoubleOrNull() ?: 10000.0) }
    var selectedTab by remember { mutableStateOf("Империя") }
    val owned = remember { mutableStateMapOf<String, Int>().apply {
        businesses.forEach { put(it.id, prefs.getInt("owned_${it.id}", 0)) }
    } }
    val levels = remember { mutableStateMapOf<String, Int>().apply {
        businesses.forEach { put(it.id, prefs.getInt("level_${it.id}", 0)) }
    } }
    val shares = remember { mutableStateMapOf<String, Int>().apply {
        stocks.forEach { put(it.ticker, prefs.getInt("shares_${it.ticker}", 0)) }
    } }
    val prices = remember { mutableStateMapOf<String, Double>().apply {
        stocks.forEach { put(it.ticker, prefs.getString("price_${it.ticker}", it.start.toString())?.toDoubleOrNull() ?: it.start) }
    } }
    var day by remember { mutableIntStateOf(prefs.getInt("day", 1)) }
    var event by remember { mutableStateOf("Рынки открыты. Построй свою империю!") }
    val income = businesses.sumOf { b -> (owned[b.id] ?: 0) * b.baseIncome * (1.0 + (levels[b.id] ?: 0) * 0.5) }
    val stockValue = stocks.sumOf { s -> (shares[s.ticker] ?: 0) * (prices[s.ticker] ?: s.start) }
    val netWorth = cash + stockValue + businesses.sumOf { b -> (owned[b.id] ?: 0) * b.cost * 0.7 }

    fun save() {
        prefs.edit().putString("cash", cash.toString()).putInt("day", day).apply {
            businesses.forEach { b -> putInt("owned_${b.id}", owned[b.id] ?: 0); putInt("level_${b.id}", levels[b.id] ?: 0) }
            stocks.forEach { s -> putInt("shares_${s.ticker}", shares[s.ticker] ?: 0); putString("price_${s.ticker}", (prices[s.ticker] ?: s.start).toString()) }
        }.apply()
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            cash += income
            save()
        }
    }

    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF171B2A), Night))).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("BUSINESS EMPIRE", color = Muted, fontSize = 11.sp, letterSpacing = 3.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Империя", color = Accent, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
            Text("ДЕНЬ $day", color = Muted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("КАПИТАЛ", cashText(cash), Modifier.weight(1f))
            StatCard("ДОХОД / СЕК", cashText(income), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        StatCard("СОСТОЯНИЕ ИМПЕРИИ", cashText(netWorth), Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("Империя", "Бизнес", "Биржа", "Активы").forEach { tab ->
                FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(tab, fontSize = 11.sp) })
            }
        }
        Spacer(Modifier.height(8.dp))
        when (selectedTab) {
            "Империя" -> HomePage(event, income, netWorth, owned.values.sum())
            "Бизнес" -> BusinessPage(cash, owned, levels, onBuy = { b ->
                if (cash >= b.cost * (1.35.powSafe(owned[b.id] ?: 0))) {
                    cash -= b.cost * (1.35.powSafe(owned[b.id] ?: 0)); owned[b.id] = (owned[b.id] ?: 0) + 1; save()
                }
            }, onUpgrade = { b ->
                val level = levels[b.id] ?: 0
                val price = b.cost * (0.65 + level * 0.45)
                if ((owned[b.id] ?: 0) > 0 && cash >= price) { cash -= price; levels[b.id] = level + 1; save() }
            })
            "Биржа" -> MarketPage(cash, prices, shares, onBuy = { s ->
                val p = prices[s.ticker] ?: s.start
                if (cash >= p) { cash -= p; shares[s.ticker] = (shares[s.ticker] ?: 0) + 1; save() }
            }, onSell = { s ->
                if ((shares[s.ticker] ?: 0) > 0) { cash += prices[s.ticker] ?: s.start; shares[s.ticker] = (shares[s.ticker] ?: 0) - 1; save() }
            }, onRefresh = {
                stocks.forEach { s -> prices[s.ticker] = ((prices[s.ticker] ?: s.start) * (1.0 + Random.nextDouble(-0.09, 0.10))).coerceAtLeast(1.0) }
                day += 1
                event = listOf("Технологический сектор растёт", "Инвесторы ждут новых отчётов", "Энергетика показывает рост", "На рынке повышенная волатильность").random()
                save()
            })
            else -> AssetsPage(owned, levels, shares, stockValue)
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun Double.powSafe(power: Int): Double { var result = 1.0; repeat(power.coerceAtMost(30)) { result *= this }; return result }

@Composable
private fun HomePage(event: String, income: Double, netWorth: Double, count: Int) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { CosmicCard {
            Text("ТВОЯ ЦЕЛЬ", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
            Text("Построй бизнес-империю", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Покупай компании, увеличивай прибыль и инвестируй в рынок. Начни с малого — стань владельцем корпорации.", color = Muted, fontSize = 13.sp)
        } }
        item { CosmicCard {
            Text("ТЕКУЩАЯ СИТУАЦИЯ", color = Accent, fontWeight = FontWeight.Bold)
            Text("Компаний во владении: $count", color = Muted)
            Text("Пассивный доход: ${cashText(income)} в секунду", color = Green)
            Text("Общее состояние: ${cashText(netWorth)}", color = Accent)
        } }
        item { CosmicCard {
            Text("СОБЫТИЯ РЫНКА", color = Accent, fontWeight = FontWeight.Bold)
            Text(event, color = Muted)
            Text("Цены акций меняются при обновлении биржи.", color = Muted, fontSize = 11.sp)
        } }
    }
}

@Composable
private fun BusinessPage(cash: Double, owned: Map<String, Int>, levels: Map<String, Int>, onBuy: (Business) -> Unit, onUpgrade: (Business) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        items(businesses) { b ->
            val count = owned[b.id] ?: 0
            val level = levels[b.id] ?: 0
            val price = b.cost * 1.35.powSafe(count)
            val upgradePrice = b.cost * (0.65 + level * 0.45)
            CosmicCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.icon, fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(b.name, color = Accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(b.description, color = Muted, fontSize = 11.sp)
                    }
                    Text("×$count", color = Accent, fontWeight = FontWeight.Bold)
                }
                Text("Доход: ${cashText(b.baseIncome * (1 + level * 0.5))}/сек · Уровень $level", color = Green, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onBuy(b) }, enabled = cash >= price, modifier = Modifier.weight(1f)) { Text("Купить · ${cashText(price)}", fontSize = 11.sp) }
                    OutlinedButton(onClick = { onUpgrade(b) }, enabled = count > 0 && cash >= upgradePrice, modifier = Modifier.weight(1f)) { Text("Улучшить", fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun MarketPage(cash: Double, prices: Map<String, Double>, shares: Map<String, Int>, onBuy: (Stock) -> Unit, onSell: (Stock) -> Unit, onRefresh: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { CosmicCard {
            Text("ФОНДОВАЯ БИРЖА", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Вымышленный игровой рынок. Котировки не связаны с реальными компаниями.", color = Muted, fontSize = 11.sp)
            Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Следующий торговый день") }
        } }
        items(stocks) { s ->
            val price = prices[s.ticker] ?: s.start
            val count = shares[s.ticker] ?: 0
            CosmicCard {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(s.ticker, color = Accent, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(s.name, color = Muted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(cashText(price), color = Accent, fontWeight = FontWeight.Bold)
                        Text("У тебя: $count шт.", color = Muted, fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onBuy(s) }, enabled = cash >= price, modifier = Modifier.weight(1f)) { Text("Купить") }
                    OutlinedButton(onClick = { onSell(s) }, enabled = count > 0, modifier = Modifier.weight(1f)) { Text("Продать") }
                }
            }
        }
    }
}

@Composable
private fun AssetsPage(owned: Map<String, Int>, levels: Map<String, Int>, shares: Map<String, Int>, stockValue: Double) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { CosmicCard {
            Text("МОИ АКТИВЫ", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Стоимость акций: ${cashText(stockValue)}", color = Green)
        } }
        items(businesses.filter { (owned[it.id] ?: 0) > 0 }) { b ->
            CosmicCard {
                Text("${b.icon}  ${b.name}", color = Accent, fontWeight = FontWeight.Bold)
                Text("Количество: ${owned[b.id]} · Уровень: ${levels[b.id]}", color = Muted)
                Text("Оценочная стоимость: ${cashText(b.cost * (owned[b.id] ?: 0) * 0.7)}", color = Green)
            }
        }
        items(stocks.filter { (shares[it.ticker] ?: 0) > 0 }) { s ->
            CosmicCard { Text("${s.name} · ${shares[s.ticker]} акций", color = Accent) }
        }
        if (owned.values.sum() == 0 && shares.values.sum() == 0) item { CosmicCard { Text("Пока активов нет. Купи первый бизнес или акции на бирже.", color = Muted) } }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Panel.copy(alpha = 0.96f), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(13.dp)) {
            Text(label, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(value, color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
private fun CosmicCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Panel.copy(alpha = 0.94f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}
