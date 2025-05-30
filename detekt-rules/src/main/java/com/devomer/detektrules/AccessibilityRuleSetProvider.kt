package com.devomer.detektrules

import com.devomer.detektrules.rules.ClickableModifierMissingOnClickLabelRule
import com.devomer.detektrules.rules.ImageButtonMissingAccessibilityLabel
import com.devomer.detektrules.rules.TextFieldMissingAccessibilityLabel
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class AccessibilityRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "accessibility"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            ClickableModifierMissingOnClickLabelRule(config),
            TextFieldMissingAccessibilityLabel(config),
            ImageButtonMissingAccessibilityLabel(config)
        )
    )
}
