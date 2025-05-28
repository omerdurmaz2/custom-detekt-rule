package com.devomer.detektrules

import com.devomer.detektrules.rules.ClickableModifierMissingOnClickLabelRule
import com.devomer.detektrules.rules.IconOnlyButtonMissingAccessibilityLabelRule
import com.devomer.detektrules.rules.TextFieldMissingLabelOrContentDescriptionRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class AccessibilityRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "accessibility"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            ClickableModifierMissingOnClickLabelRule(config),
            TextFieldMissingLabelOrContentDescriptionRule(config),
            IconOnlyButtonMissingAccessibilityLabelRule(config)
        )
    )
}
