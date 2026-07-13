package com.mihai.dailyhabit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ivory = Color(0xFFFDFBF7)
private val Forest = Color(0xFF1D3B2B)
private val Leaf = Color(0xFF639845)
private val Body = Color(0xFF5C635A)
private val SageBorder = Color(0xFFC8D2C4)
private val Privacy = Color(0xFFF0F4EC)
private val ButtonStart = Color(0xFF5C9246)
private val ButtonEnd = Color(0xFF85B654)

@Composable
fun UploadPlanScreen(modifier: Modifier = Modifier, onSelectFile: () -> Unit, darkTheme: Boolean = false, onToggleTheme: () -> Unit = {}) {
    val background = MaterialTheme.colorScheme.background
    Column(modifier.background(background)) {
        UploadHeader()
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                UploadDropZone(Modifier.weight(1f).fillMaxWidth(), onSelectFile)
                PrivacyBanner(Modifier.fillMaxWidth().padding(top = 20.dp))
            }
        }
    }
}

@Composable
private fun UploadHeader() {
    Row(
        Modifier.fillMaxWidth().padding(start = 24.dp, top = 36.dp, end = 20.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Carica il tuo", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 31.sp)
            Text("piano nutrizionale", color = MaterialTheme.colorScheme.primary, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 31.sp)
            Spacer(Modifier.height(10.dp))
            Text("Importa il tuo piano e inizia\na monitorare i tuoi pasti.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
        }
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.app_icon),
            contentDescription = "Illustrazione alimentazione sana",
            modifier = Modifier.size(112.dp).padding(start = 8.dp),
        )
    }
}

@Composable
private fun UploadDropZone(modifier: Modifier, onSelectFile: () -> Unit) {
    Box(
        modifier
            .padding(vertical = 12.dp)
            .drawDashedRoundRect(SageBorder, 24.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(72.dp).shadow(14.dp, RoundedCornerShape(50), spotColor = Color(0x665C9246)),
                shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface,
            ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.FileUpload, null, Modifier.size(36.dp), tint = Forest) } }
            Spacer(Modifier.height(16.dp))
            Text("PDF, Excel o CSV", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(ButtonStart, ButtonEnd))).clickable(onClick = onSelectFile).height(52.dp).padding(horizontal = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Outlined.FolderOpen, null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text("Seleziona file", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PrivacyBanner(modifier: Modifier) {
    Row(modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Security, "Privacy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text("La tua privacy è la nostra priorità.\nI tuoi dati sono accessibili esclusivamente a te.", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

private fun Modifier.drawDashedRoundRect(color: Color, radius: androidx.compose.ui.unit.Dp) = drawBehind {
    val stroke = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())), cap = StrokeCap.Round)
    drawRoundRect(color, Offset(stroke.width / 2, stroke.width / 2), Size(size.width - stroke.width, size.height - stroke.width), CornerRadius(radius.toPx()), style = stroke)
}

@Preview(showBackground = true, backgroundColor = 0xFFFDFBF7, heightDp = 850)
@Composable private fun UploadPlanPreview() = MaterialTheme { UploadPlanScreen(Modifier.fillMaxSize(), {}) }
