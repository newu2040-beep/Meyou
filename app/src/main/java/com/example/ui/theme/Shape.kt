package com.example.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val MeyouShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

// Asymmetric and specialized expressive shapes
val ExpressivePillShape = RoundedCornerShape(50)
val ExpressiveCardShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 8.dp,
    bottomEnd = 24.dp,
    bottomStart = 24.dp
)
val ExpressiveHeroCardShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp,
    bottomEnd = 16.dp,
    bottomStart = 32.dp
)
val ExpressiveQuoteShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 8.dp,
    bottomEnd = 28.dp,
    bottomStart = 8.dp
)
val ExpressiveBookmarkShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomEnd = 24.dp,
    bottomStart = 4.dp
)
