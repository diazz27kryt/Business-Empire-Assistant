package com.diazz27kryt.businessempireassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private val Ink = Color(0xFF101114)
private val Glass = Color(0xFF202228)
private val Muted = Color(0xFF9A9DA7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Glass, primary = Color.White)) { AssistantApp() } }
    }
}

@Composable
private fun AssistantApp() {
    var tab by remember { mutableStateOf("Главная") }
    var cash by remember { mutableStateOf("1000000") }
    val tabs = listOf("Главная", "Биржа", "Бизнес", "Активы")
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF252831), Ink))).padding(18.dp)) {
        Text("BUSINESS EMPIRE", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
        Text("Assistant", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEach { item ->
                FilterChip(selected = tab == item, onClick = { tab = item }, label = { Text(item) })
            }
        }
        Spacer(Modifier.height(16.dp))
        when (tab) {
            "Главная" -> HomeScreen(cash, { cash = it })
            "Биржа" -> InfoScreen("Биржа", "Добавление котировок и история цен появятся в следующем обновлении.")
            "Бизнес" -> InfoScreen("Доходность бизнеса", "Введи стоимость актива и доход за период — калькулятор окупаемости будет добавлен следующим этапом.")
            else -> InfoScreen("Мои активы", "Здесь появится список недвижимости, бизнеса и других игровых активов.")
        }
        Spacer(Modifier.weight(1f))
        Text("Данные вводятся вручную. Прогнозы не гарантируют рост.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun HomeScreen(cash: String, onCash: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard {
                Text("ИГРОВОЙ КАПИТАЛ", color = Muted, fontSize = 11.sp, letterSpacing = 1.sp)
                OutlinedTextField(value = cash, onValueChange = { onCash(it.filter(Char::isDigit).take(15)) }, label = { Text("Деньги в игре") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("₽  ${cash.ifBlank { "0" }}", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
        }
        item { GlassCard { Text("Быстрый обзор", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Text("Пока нет записанных активов и котировок.", color = Muted) } }
        item { GlassCard { Text("Совет аналитика", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Text("Добавь несколько наблюдений цены одной акции в разное время — тогда можно будет оценить её динамику.", color = Muted) } }
    }
}

@Composable
private fun InfoScreen(title: String, description: String) {
    GlassCard { Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(description, color = Muted, lineHeight = 22.sp) }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Glass.copy(alpha = 0.88f), shape = RoundedCornerShape(24.dp), tonalElevation = 5.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
