package com.devomer.detektrules.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class IconOnlyButtonMissingAccessibilityLabelRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "Sadece Image içeren Button'larda, Image'ın contentDescription'ı veya Button'un kendisine ait bir erişilebilirlik etiketi (Modifier.semantics ile) sağlanmalıdır.",
        Debt.TEN_MINS
    )

    private val parentButtonFuncName = listOf("Button", "IconButton")
    private val childFunctionName = listOf("Image", "Icon")
    private val contentDescriptionParamName = "contentDescription"
    private val modifierParamName = "modifier"
    private val semanticsFunctionName = "semantics"
    private val contentDescriptionPropertyName = "contentDescription"

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)


        if (expression.calleeExpression?.text !in parentButtonFuncName) {
            return
        }

        val imageCallExpression = getSingleImageContentFromButton(expression) ?: return

        if (!isImageContentDescriptionNullOrEmpty(imageCallExpression)) {
            return
        }

        val buttonModifierArgument = expression.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == modifierParamName
        }
        val buttonModifierExpression = buttonModifierArgument?.getArgumentExpression()

        if (hasSemanticContentDescriptionInModifier(buttonModifierExpression)) {
            return
        }

        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "Bir icon/image button oluşturdunuz ama contentDescription değeriniz null ya da empty ayarlanmış. erişilebilirlik için lütfen contentDescription tanımlayın"
            )
        )
    }

    /**
     * Verilen bir Button çağrısının içeriğinin sadece tek bir Image çağrısı olup olmadığını kontrol eder.
     * Eğer öyleyse Image çağrısını döndürür, değilse null döndürür.
     */
    private fun getSingleImageContentFromButton(buttonCallExpression: KtCallExpression): KtCallExpression? {
        val trailingLambda =
            buttonCallExpression.lambdaArguments.firstOrNull()?.getLambdaExpression()
        val lambdaBody = trailingLambda?.bodyExpression ?: return null

        val statements = lambdaBody.statements
        if (statements.size == 1) {
            val singleStatement = statements.firstOrNull()
            if (singleStatement is KtCallExpression && singleStatement.calleeExpression?.text in childFunctionName) {
                return singleStatement
            }
        }
        return null
    }

    /**
     * Verilen bir Image çağrısının contentDescription parametresinin eksik, null veya boş string olup olmadığını kontrol eder.
     * Eğer eksik, null veya boş ise true döndürür.
     */
    private fun isImageContentDescriptionNullOrEmpty(imageCallExpression: KtCallExpression): Boolean {
        val contentDescriptionArg = imageCallExpression.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == contentDescriptionParamName
        }

        if (contentDescriptionArg == null) {
            return true
        }

        val argExpression = contentDescriptionArg.getArgumentExpression()
        if (argExpression == null || argExpression.text == "null") {
            return true
        }

        if (argExpression is KtStringTemplateExpression &&
            argExpression.text == "\"\""
        ) {
            return true
        }
        return false
    }

    /**
     * Verilen bir Modifier ifadesinin içinde .semantics { contentDescription = "..." } olup olmadığını
     * ve contentDescription'ın geçerli (null veya boş olmayan) bir string olup olmadığını kontrol eder.
     * Bu fonksiyonu bir önceki TextField kuralından alıp uyarlayabilirsiniz veya ortak bir yerde tutabilirsiniz.
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