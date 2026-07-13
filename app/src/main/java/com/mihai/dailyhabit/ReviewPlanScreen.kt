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

private val ReviewCream = Color(0xFFFDFBF7)
private val ReviewLeaf = Color(0xFF639845)
private val ReviewBody = Color(0xFF5C635A)
private val ReviewPastel = Color(0xFFF0F4EC)

@Composable
fun ReviewPlanScreen(plan: DietPlan, onBack: () -> Unit, onConfirm: () -> Unit, onFoodChange: (Int, Int, String, FoodItem) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ArrowBack, "Indietro") } }
            Spacer(Modifier.weight(1f))
        }
        ReviewHero(plan.days.size)
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            itemsIndexed(plan.days) { dayIndex, day -> ReviewDayCard(dayIndex, day, onFoodChange) }
            item { Spacer(Modifier.height(4.dp)) }
        }
        ConfirmButton(onConfirm)
    }
}

@Composable private fun ReviewHero(count: Int) {
    Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = ReviewPastel, modifier = Modifier.size(56.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AssignmentTurnedIn, null, tint = ReviewLeaf) } }
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("Revisione piano", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                Surface(shape = RoundedCornerShape(50), color = ReviewPastel, modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.FitnessCenter, null, tint = ReviewLeaf) } }
                Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(day.day, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text("Pasti del giorno", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, if (expanded) "Comprimi" else "Espandi") }
            }
            if (expanded) day.meals.forEachIndexed { mealIndex, meal ->
                Text(meal.type.label, Modifier.padding(top = 18.dp, bottom = 4.dp), color = ReviewLeaf, fontWeight = FontWeight.Bold)
                if (meal.hasLunchAlternatives) Text("Potrai scegliere anche tra le alternative del pranzo", color = ReviewBody, fontSize = 12.sp)
                meal.groups.forEachIndexed { groupIndex, group ->
                    Text("Alternativa ${groupIndex + 1}", Modifier.padding(top = 12.dp, bottom = 6.dp).clip(RoundedCornerShape(50)).background(ReviewPastel).padding(horizontal = 12.dp, vertical = 6.dp), color = ReviewLeaf, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    group.alternatives.forEachIndexed { foodIndex, food ->
                        ReviewFoodRow(food, onNameChanged = { onFoodChange(dayIndex, mealIndex, food.clientId, food.copy(name = it)) }, onQuantityChanged = { onFoodChange(dayIndex, mealIndex, food.clientId, food.copy(quantity = it)) })
                        if (foodIndex < group.alternatives.lastIndex) HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewFoodRow(food: FoodItem, onNameChanged: (String) -> Unit, onQuantityChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = ReviewPastel, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Restaurant, null, Modifier.size(22.dp), tint = ReviewLeaf) } }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("Alimento", color = ReviewBody, fontSize = 10.sp)
            BasicTextField(food.name, onNameChanged, Modifier.fillMaxWidth(), textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium))
        }
        Column(Modifier.width(78.dp)) {
            Text("Quantità", color = ReviewLeaf, fontSize = 10.sp)
            BasicTextField(food.quantity, onQuantityChanged, Modifier.fillMaxWidth(), textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable private fun ConfirmButton(onConfirm: () -> Unit) {
    Surface(onClick = onConfirm, modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp), shape = RoundedCornerShape(50), color = Color.Transparent) {
        Row(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF5C9246), Color(0xFF85B654)))).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f)); Text("Conferma piano", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Spacer(Modifier.weight(1f)); Icon(Icons.Rounded.ArrowForward, null, tint = Color.White)
        }
    }
}
