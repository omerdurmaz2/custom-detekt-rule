package com.devomer.detektrules.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class TextFieldMissingLabelOrContentDescriptionRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "TextField/OutlinedTextField için `label` parametresi veya `Modifier.semantics` içinde `contentDescription` sağlanmalıdır. " +
                "Bu, ekran okuyucuların giriş alanının amacını kullanıcıya iletmesine yardımcı olur.",
        Debt.TEN_MINS
    )

    private val targetTextFieldNames = setOf("TextField", "OutlinedTextField")
    private val labelParameterName = "label"
    private val modifierParameterName = "modifier"
    private val semanticsFunctionName = "semantics"
    private val contentDescriptionPropertyName = "contentDescription"

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val callee = expression.calleeExpression ?: return

        if (callee.text !in targetTextFieldNames) {
            return
        }

        val labelArgument = expression.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == labelParameterName
        }

        if (labelArgument != null) {
            return
        }

        val modifierArgument = expression.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == modifierParameterName
        }

        val modifierExpression = modifierArgument?.getArgumentExpression()
        if (modifierExpression != null) {
            if (hasSemanticContentDescriptionInModifier(modifierExpression)) {
                return
            }
        }

        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "TextField/OutlinedTextField (${callee.text}) için `label` parametresi veya " +
                        "`Modifier.semantics` içinde geçerli bir `contentDescription` sağlanmalıdır."
            )
        )
    }

    private fun hasSemanticContentDescriptionInModifier(modifierExpression: KtExpression): Boolean {
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
                            } else {
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