package com.devomer.detektrules.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

class ClickableModifierMissingOnClickLabelRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "Accessibility Rule: onClickLabel parameter check on .clickable() modifier",
        Debt.FIVE_MINS
    )

    private val clickableFunctionName = "clickable"
    private val onClickLabelParameterName = "onClickLabel"

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val callee = expression.calleeExpression ?: return

        if (callee.text != clickableFunctionName) {
            return
        }

        val hasOnClickLabelArgument = expression.valueArguments.any { argument ->
            argument.getArgumentName()?.asName?.identifier == onClickLabelParameterName
        }

        if (!hasOnClickLabelArgument) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "`.clickable` modifier'ı için `onClickLabel` parametresi eksik. " +
                        "Erişilebilirlik için lütfen anlamlı bir etiket sağlayın (örn: onClickLabel = \"Ayarları aç\")."
                )
            )
        }
    }
}
