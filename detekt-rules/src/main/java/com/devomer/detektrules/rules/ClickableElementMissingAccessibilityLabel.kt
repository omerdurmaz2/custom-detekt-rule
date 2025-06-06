package com.devomer.detektrules.rules // Kendi paket adınızı kullanın

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class ClickableElementMissingAccessibilityLabel(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "For accessibility, any clickable element must have a clear label. This label can" +
                " be provided by: " +
                "\n1) the onClickLabel parameter in the .clickable modifier, " +
                "\n2) a contentDescription set on the element itself via Modifier.semantics, OR " +
                "\n3) self-describing content within the element (e.g., meaningful Text, an Image/Icon " +
                "with its own contentDescription, or a custom Composable that manages its own " +
                "accessibility). " +
                "\nIf the content is purely decorative/structural and does not provide" +
                " a description through these means, the onClickLabel parameter becomes mandatory " +
                "for the .clickable modifier.",
        Debt.TEN_MINS
    )

    private val clickableFunctionName = "clickable"
    private val onClickLabelParameterName = "onClickLabel"
    private val textComposableName = "Text"
    private val imageFunctionName = "Image"
    private val iconFunctionName = "Icon"
    private val contentDescriptionParamName = "contentDescription"
    private val modifierParamName = "modifier"
    private val semanticsFunctionName = "semantics"
    private val contentDescriptionPropertyName = "contentDescription"

    private val standardLayoutComposables =
        setOf("Box", "Row", "Column", "Surface", "LazyRow", "LazyColumn", "Spacer", "Divider")


    override fun visitCallExpression(clickableCallExpression: KtCallExpression) {
        super.visitCallExpression(clickableCallExpression)

        if (clickableCallExpression.calleeExpression?.text != clickableFunctionName) {
            return
        }

        val hasOnClickLabelArgument = clickableCallExpression.valueArguments.any { argument ->
            argument.getArgumentName()?.asName?.identifier == onClickLabelParameterName
        }

        if (hasOnClickLabelArgument) {
            return
        }

        val parentComposableCall = findParentComposableForModifier(clickableCallExpression)

        if (parentComposableCall == null) {
            reportForMissingOnClickLabelOnly(clickableCallExpression)
            return
        }

        val contentLambda = parentComposableCall.lambdaArguments.firstOrNull()?.getLambdaExpression()
        val lambdaBody = contentLambda?.bodyExpression

        if (lambdaBody != null) {
            if (checkContentForAccessibility(lambdaBody)) {
                return
            }
        }

        reportForMissingLabelAndContent(clickableCallExpression, parentComposableCall)
    }

    private fun findParentComposableForModifier(clickableCallExpression: KtCallExpression): KtCallExpression? {
        var modifierChainRoot: KtExpression = clickableCallExpression
        while (modifierChainRoot.parent is KtDotQualifiedExpression) {
            modifierChainRoot = modifierChainRoot.parent as KtDotQualifiedExpression
        }

        var currentPsiElement: PsiElement? = modifierChainRoot
        repeat(4) {
            val valueArgument = currentPsiElement?.parent as? KtValueArgument
            if (valueArgument != null &&
                (valueArgument.getArgumentName()?.asName?.identifier == modifierParamName || valueArgument.isNamed().not())
            ) {
                return valueArgument.parent?.parent as? KtCallExpression
            }
            currentPsiElement = currentPsiElement?.parent
            if (currentPsiElement == null || currentPsiElement is KtFile) return null
        }
        return null
    }

    /**
     * Verilen bir PSI öğesinin (lambda gövdesi veya tek bir Composable çağrısı)
     * erişilebilir bir açıklama içerip içermediğini kontrol eder.
     * Eğer erişilebilir içerik veya güvenilir bir özel Composable bulunursa true döner.
     */
    private fun checkContentForAccessibility(contentElement: KtElement): Boolean {
        val childrenToInspect: List<PsiElement> = when (contentElement) {
            is KtBlockExpression -> contentElement.statements // Lambda gövdesindeki ifadeler
            is KtCallExpression -> listOf(contentElement)    // Lambda gövdesi tek bir çağrıysa
            else -> {
                return false
            }
        }

        if (childrenToInspect.isEmpty()) {
            return false
        }

        for (child in childrenToInspect) {
            if (child is KtCallExpression) {
                val calleeName = child.calleeExpression?.text

                if (isCallDescriptive(child)) {
                    return true
                }

                if (calleeName in standardLayoutComposables) {
                    val innerContentLambda = child.lambdaArguments.firstOrNull()?.getLambdaExpression()
                    val innerLambdaBody = innerContentLambda?.bodyExpression
                    if (innerLambdaBody != null) {
                        if (checkContentForAccessibility(innerLambdaBody)) {
                            return true
                        }
                    }
                }
                else {
                    return true
                }
            }
            else if (child is KtBlockExpression) {
                if (checkContentForAccessibility(child)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Verilen bir KtCallExpression'ın (Text, Image, Icon veya Modifier.semantics ile)
     * kendini açıklayıp açıklamadığını kontrol eder.
     */
    private fun isCallDescriptive(call: KtCallExpression): Boolean {
        if (call.calleeExpression?.text == textComposableName) {
            val textArg = call.valueArguments.firstOrNull {
                it.getArgumentName()?.asName?.identifier == "text" || it.getArgumentName() == null
            }
            val textValueExpression = textArg?.getArgumentExpression()
            if (textValueExpression != null) {
                if (textValueExpression is KtStringTemplateExpression) {
                    if (textValueExpression.entries.isNotEmpty() || (textValueExpression.text != "\"\"" && textValueExpression.text != "\"\"\"\"\"\"")) {
                        return true
                    }
                } else {
                    return true
                }
            }
        }

        if (call.calleeExpression?.text in listOf(imageFunctionName, iconFunctionName)) {
            val contentDescParam = call.valueArguments.find {
                it.getArgumentName()?.asName?.identifier == contentDescriptionParamName
            }
            if (contentDescParam != null) {
                val cdExpr = contentDescParam.getArgumentExpression()
                if (cdExpr != null && cdExpr.text != "null") {
                    if (cdExpr is KtStringTemplateExpression) {
                        if (cdExpr.entries.isNotEmpty() || (cdExpr.text != "\"\"" && cdExpr.text != "\"\"\"\"\"\"")) {
                            return true
                        }
                    } else {
                        return true
                    }
                }
            }
        }

        val modifierArg = call.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == modifierParamName
        }
        val modifierExpr = modifierArg?.getArgumentExpression()
        return hasSemanticContentDescriptionInModifier(modifierExpr)
    }

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
                            } else { return true }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun reportForMissingOnClickLabelOnly(clickableCallExpression: KtCallExpression) {
        report(
            CodeSmell(
                issue,
                Entity.from(clickableCallExpression),
                "Missing onClickLabel for this .clickable modifier. Unable to analyze " +
                        "content for an alternative description or determine the parent Composable. " +
                        "Please provide an onClickLabel to ensure accessibility"
            )
        )
    }

    private fun reportForMissingLabelAndContent(
        clickableCallExpression: KtCallExpression,
        parentComposableCall: KtCallExpression
    ) {
        report(
            CodeSmell(
                issue,
                Entity.from(clickableCallExpression),
                "This clickable element is missing an accessible label. Please provide an " +
                        "onClickLabel in the .clickable modifier, or set a contentDescription on the" +
                        " element via Modifier.semantics, or ensure its content is self-describing " +
                        "(e.g., via Text, or an Image/Icon with its own contentDescription)."
            )
        )
    }
}