package com.mihai.dailyhabit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyWelcomeScreen(themeMode: ThemeMode, onWorkout: () -> Unit, onRest: () -> Unit, onNewPlan: () -> Unit, onToggleTheme: (ThemeMode) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Surface(onClick = onNewPlan, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary, shadowElevation = 6.dp) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Carica un nuovo piano", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp)) } }
            Box {
                Surface(onClick = { menuExpanded = true }, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Menu, "Menu", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)) } }
                DropdownMenu(
                    expanded = menuExpanded, 
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .width(220.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                                val isDark = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && systemDark)
                                
                                Text("☀️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                androidx.compose.material3.Switch(
                                    checked = isDark,
                                    onCheckedChange = { onToggleTheme(if (it) ThemeMode.DARK else ThemeMode.LIGHT) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("🌙", fontSize = 18.sp)
                            }
                        },
                        onClick = { }
                    )
                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    DropdownMenuItem(
                        text = { Text("Impostazioni", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface) }, 
                        onClick = { menuExpanded = false },
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    )
                    DropdownMenuItem(
                        text = { Text("Informazioni", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface) }, 
                        onClick = { menuExpanded = false },
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 54.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Buongiorno 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Pronto a prenderti cura\ndi te oggi?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, lineHeight = 23.sp)
            }
            Image(painterResource(R.drawable.app_icon), "Benessere quotidiano", Modifier.size(150.dp))
        }
        Surface(Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 2.dp) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(52.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Spa, null, tint = MaterialTheme.colorScheme.primary) } }
                Spacer(Modifier.height(18.dp))
                Text("Ti sei allenato oggi?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("La tua risposta ci aiuterà a mostrarti il piano\nnutrizionale più adatto a questa giornata.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(26.dp))
                TrackingChoiceButton(Icons.Rounded.FitnessCenter, "Sì, mi sono allenato", onWorkout)
                Spacer(Modifier.height(14.dp))
                TrackingChoiceButton(Icons.Rounded.Hotel, "No, giorno di riposo", onRest)
            }
        }
    }
}

@Composable private fun TrackingChoiceButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Icon(icon, null); Spacer(Modifier.width(14.dp)); Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}
