package com.example.timeregistrering.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.layout.DisplayFeature

@Composable
fun TabletLayout(
    displayFeatures: List<DisplayFeature>,
    navigationContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Navigation rail
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp),
                tonalElevation = 1.dp
            ) {
                navigationContent()
            }

            // Main content area with adaptive layout
            if (displayFeatures.isEmpty()) {
                // Regular tablet layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Main content (60% width)
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(end = 8.dp)
                    ) {
                        mainContent()
                    }

                    // Detail content (40% width)
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                    ) {
                        detailContent()
                    }
                }
            } else {
                // Foldable device layout
                AdaptivePaneLayout(
                    displayFeatures = displayFeatures,
                    mainContent = mainContent,
                    detailContent = detailContent
                )
            }
        }
    }
}
