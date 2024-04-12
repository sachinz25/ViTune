package app.vitune.compose.routing

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut

val defaultStacking = ContentTransform(
    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
    targetContentEnter = fadeIn(),
    targetContentZIndex = 1f
)

val defaultUnstacking = ContentTransform(
    initialContentExit = scaleOut(targetScale = 1.1f) + fadeOut(),
    targetContentEnter = EnterTransition.None,
    targetContentZIndex = 0f
)

val defaultStill = ContentTransform(
    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
    targetContentEnter = fadeIn(),
    targetContentZIndex = 1f
)

val AnimatedContentTransitionScope<Route?>.isStacking: Boolean
    get() = initialState == null && targetState != null

val AnimatedContentTransitionScope<Route?>.isUnstacking: Boolean
    get() = initialState != null && targetState == null

val AnimatedContentTransitionScope<Route?>.isStill: Boolean
    get() = initialState == null && targetState == null