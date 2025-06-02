package com.devomer.detektrules

import com.devomer.detektrules.rules.ClickableElementMissingAccessibilityLabel
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
            ClickableElementMissingAccessibilityLabel(config),
            TextFieldMissingAccessibilityLabel(config),
            ImageButtonMissingAccessibilityLabel(config)
        )
    )
}
