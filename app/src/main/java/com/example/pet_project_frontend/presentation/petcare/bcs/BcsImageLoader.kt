package com.example.pet_project_frontend.presentation.petcare.bcs

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberBcsPaintersFromAssets(names: List<String> = listOf("bcs1.png","bcs2.png","bcs3.png","bcs4.png","bcs5.png")): List<Painter?> {
    val ctx = LocalContext.current
    return remember(names) {
        names.map { name ->
            try {
                ctx.assets.open(name).use { input ->
                    val bmp = BitmapFactory.decodeStream(input)
                    if (bmp != null) BitmapPainter(bmp.asImageBitmap()) else null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
