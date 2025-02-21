package com.example.timeregistrering.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature

@Composable
fun AdaptivePaneLayout(
    displayFeatures: List<DisplayFeature>,
    mainContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit
) {
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
    
    when {
        foldingFeature?.state == FoldingFeature.State.FLAT -> {
            // Device is flat, show side-by-side layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    mainContent()
                }
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                ) {
                    detailContent()
                }
            }
        }
        foldingFeature?.state == FoldingFeature.State.HALF_OPENED -> {
            // Device is half opened, show content in separate panes
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxWidth()
                ) {
                    mainContent()
                }
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxWidth()
                ) {
                    detailContent()
                }
            }
        }
        else -> {
            // Default layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    mainContent()
                }
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .padding(start = 8.dp)
                ) {
                    detailContent()
                }
            }
        }
    }
}
