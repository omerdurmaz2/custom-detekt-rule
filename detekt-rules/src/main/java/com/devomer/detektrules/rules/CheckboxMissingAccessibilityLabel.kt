package com.devomer.detektrules.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class CheckboxMissingAccessibilityLabel(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName, // Kural ID'si: CheckboxMissingContentDescriptionRule
        Severity.Warning,
        "For accessibility, a `Checkbox` composable must have a `contentDescription` provided via `Modifier.semantics`.",
        Debt.FIVE_MINS
    )

    private val checkboxComposableName = "Checkbox"
    private val modifierParamName = "modifier"
    private val semanticsFunctionName = "semantics"
    private val contentDescriptionPropertyName = "contentDescription"

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        // 1. Ziyaret edilen çağrı bir "Checkbox" Composable mı?
        if (expression.calleeExpression?.text != checkboxComposableName) {
            return
        }

        // 2. Checkbox'ın "modifier" parametresini bul.
        val modifierArgument = expression.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == modifierParamName
        }
        val modifierExpression = modifierArgument?.getArgumentExpression()

        // 3. Modifier içinde geçerli bir semantic contentDescription var mı diye kontrol et.
        if (hasSemanticContentDescriptionInModifier(modifierExpression)) {
            // Eğer varsa, sorun yok.
            return
        }

        // 4. Eğer bulunamazsa, sorun raporla.
        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "This `Checkbox` is missing a `contentDescription`. For accessibility, describe what this checkbox represents using `Modifier.semantics`."
            )
        )
    }

    /**
     * Verilen bir Modifier ifadesinin içinde .semantics { contentDescription = "..." } olup olmadığını
     * ve contentDescription'ın geçerli (null veya boş olmayan) bir string olup olmadığını kontrol eder.
     */
    private fun hasSemanticContentDescriptionInModifier(modifierExpression: KtExpression?): Boolean {
        if (modifierExpression == null) return false

        val callExpressionsInModifier =
            modifierExpression.collectDescendantsOfType<KtCallExpression>().toList()
        val allPossibleCalls = if (modifierExpression is KtCallExpression) {
            callExpressionsInModifier + modifierExpression
        } else {
            callExpressionsInModifier
        }

        for (callExpr in allPossibleCalls) {
            if (callExpr.calleeExpression?.text == semanticsFunctionName) {
                val lambdaArgument = callExpr.lambdaArguments.firstOrNull()
                val lambdaBody = lambdaArgument?.getLambdaExpression()?.bodyExpression
                lambdaBody?.statements?.forEach { statement ->
                    if (statement is KtBinaryExpression &&
                        statement.left?.text == contentDescriptionPropertyName
                    ) {
                        val rhs = statement.right
                        if (rhs != null && rhs.text != "null") {
                            if (rhs is KtStringTemplateExpression) {
                                if (rhs.entries.isNotEmpty() || (rhs.text != "\"\"" && rhs.text != "\"\"\"\"\"\"")) {
                                    return true
                                }
                            } else { // stringResource() vb.
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}