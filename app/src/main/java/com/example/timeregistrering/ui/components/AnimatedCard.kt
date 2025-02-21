package com.example.timeregistrering.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCard(
    visible: Boolean,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        var isPressed by remember { mutableStateOf(false) }
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        val elevation by animateFloatAsState(
            targetValue = if (isPressed) 0.dp.value else 4.dp.value,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Card(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable {
                    isPressed = true
                    onClick()
                },
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation.dp,
                pressedElevation = 0.dp
            )
        ) {
            Box {
                content()
            }
        }

        DisposableEffect(isPressed) {
            onDispose {
                if (isPressed) {
                    isPressed = false
                }
            }
        }
    }
}
