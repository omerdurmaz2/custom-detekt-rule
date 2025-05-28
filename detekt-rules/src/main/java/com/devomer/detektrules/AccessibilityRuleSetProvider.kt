package com.devomer.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class AccessibilityRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "accessibility"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            ImageContentDescriptionRule(config),
            ClickableModifierMissingOnClickLabelRule(config) // YENİ KURALINIZ

        )
    )
}
