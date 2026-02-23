package com.ordershieldsdk.auth.ui

import androidx.fragment.app.FragmentManager
import com.ordershieldsdk.auth.R
import com.ordershieldsdk.auth.core.ErrorHandler
import com.ordershieldsdk.auth.data.repository.AuthRepository
import com.ordershieldsdk.auth.internal.StepNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Shared helper to resolve next step (show vs skip-and-call-API) and navigate.
 */
object VerificationFlowHelper {

    /**
     * Resolve next action: if Skip(step), run that step's API then resolve again; if Show(step), show fragment; if null, go to COMPLETE.
     */
    fun resolveAndNavigate(
        fragmentManager: FragmentManager,
        currentStep: StepNavigator.Step,
        scope: CoroutineScope,
        context: android.content.Context,
        repository: AuthRepository
    ) {
        val next = StepNavigator.getNextStepToShow(currentStep)
        when (next) {
            null -> navigateToComplete(fragmentManager)
            is StepNavigator.NextAction.Show -> {
                val fragment = StepNavigator.createFragmentForStep(next.step)
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
            }
            is StepNavigator.NextAction.SkipNoApi -> {
                // Already completed (customer-info); just skip, no API
                resolveAndNavigate(fragmentManager, next.step, scope, context, repository)
            }
            is StepNavigator.NextAction.SkipWithApi -> {
                // Pre-set via set methods; call API then resolve
                scope.launch {
                    val result = repository.runSkipStepApi(next.step)
                    result.onSuccess {
                        resolveAndNavigate(fragmentManager, next.step, scope, context, repository)
                    }.onFailure { e ->
                        android.util.Log.e("VerificationFlowHelper", "Skip API failed for ${next.step}: ${e.message}")
                        ErrorHandler.showError(context, e)
                    }
                }
            }
        }
    }

    fun navigateToComplete(fragmentManager: FragmentManager) {
        val fragment = StepNavigator.createFragmentForStep(StepNavigator.Step.COMPLETE)
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
