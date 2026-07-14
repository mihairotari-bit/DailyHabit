package com.mihai.dailyhabit

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReviewPlanScreen(plan: DietPlan, onBack: () -> Unit, onConfirm: () -> Unit, onFoodChange: (Int, Int, String, FoodItem) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (plan.isTestData || plan.parserEngine == ParserEngine.FAKE_TEST) {
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer).padding(16.dp)) {
                Text("Dati di test", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }
        }
        ReviewHero(plan.days.size)
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            itemsIndexed(plan.days) { dayIndex, day -> ReviewDayCard(dayIndex, day, onFoodChange) }
            item { Spacer(Modifier.height(4.dp)) }
        }
        val isTest = plan.isTestData || plan.parserEngine == ParserEngine.FAKE_TEST
        ConfirmButton(
            onConfirm = { if (!isTest) onConfirm() },
            enabled = !isTest
        )
    }
}

@Composable private fun ReviewHero(count: Int) {
    Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AssignmentTurnedIn, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) } }
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("Revisione piano", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Piano alimentare", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Text("$count giorni rilevati. Controlla ogni riga prima\ndi confermare.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Image(painterResource(R.drawable.app_icon), "Piano verificato", modifier = Modifier.size(80.dp))
    }
}

@Composable private fun ReviewDayCard(dayIndex: Int, day: DailyMeals, onFoodChange: (Int, Int, String, FoodItem) -> Unit) {
    var expanded by remember(day.day) { mutableStateOf(true) }
    Card(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.FitnessCenter, null, tint = MaterialTheme.colorScheme.onSecondaryContainer) } }
                Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(day.day, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface); Text("Pasti del giorno", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, if (expanded) "Comprimi" else "Espandi", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (expanded) day.meals.forEachIndexed { mealIndex, meal ->
                Text(meal.type.label, Modifier.padding(top = 18.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                if (meal.hasLunchAlternatives) Text("Potrai scegliere anche tra le alternative del pranzo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                meal.options.forEachIndexed { optionIndex, option ->
                    val optionLabel = if (meal.options.size > 1) "Opzione ${optionIndex + 1}" else null
                    if (optionLabel != null) {
                        Text(optionLabel, Modifier.padding(top = 12.dp, bottom = 6.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    option.groups.forEachIndexed { groupIndex, group ->
                        if (option.groups.size > 1 || meal.options.size == 1) {
                            Text(
                                "Alternativa ${groupIndex + 1}", 
                                Modifier
                                    .padding(top = 16.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp), 
                                color = MaterialTheme.colorScheme.onSecondaryContainer, 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        group.alternatives.forEachIndexed { foodIndex, food ->
                            ReviewFoodRow(food, onNameChanged = { onFoodChange(dayIndex, mealIndex, food.clientId, food.copy(name = it)) }, onQuantityChanged = { onFoodChange(dayIndex, mealIndex, food.clientId, food.copy(quantity = it)) })
                            if (foodIndex < group.alternatives.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewFoodRow(food: FoodItem, onNameChanged: (String) -> Unit, onQuantityChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Restaurant, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer) } }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("Alimento", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            BasicTextField(food.name, onNameChanged, Modifier.fillMaxWidth(), textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium))
        }
        Column(Modifier.width(78.dp)) {
            Text("Quantità", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
            BasicTextField(food.quantity, onQuantityChanged, Modifier.fillMaxWidth(), textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable private fun ConfirmButton(onConfirm: () -> Unit, enabled: Boolean = true) {
    Surface(onClick = { if (enabled) onConfirm() }, modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp), shape = RoundedCornerShape(50), color = Color.Transparent) {
        val bgBrush = if (enabled) Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)) else Brush.horizontalGradient(listOf(Color.Gray, Color.LightGray))
        Row(Modifier.fillMaxSize().background(bgBrush).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f)); Text("Conferma piano", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Spacer(Modifier.weight(1f)); Icon(Icons.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
