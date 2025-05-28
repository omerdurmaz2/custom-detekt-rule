package com.devomer.detektrules // Paket adınızın doğru olduğundan emin olun

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution // Bu anotasyon önemli
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall // Bu import gerekli
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull // Bu import gerekli

@RequiresTypeResolution // Bu kuralın type resolution gerektirdiğini belirtir
class ImageContentDescriptionRule(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "Image composable'ları için contentDescription null veya boş olmamalıdır. " +
            "Dekoratif görseller için null kullanılabilir, ancak boş string ('') önerilmez. " +
            "Bu kural hem null hem de boş string kullanımını işaret eder.",
        Debt.FIVE_MINS
    )

    private val imageComposableFqn = "androidx.compose.foundation.Image" // Kontrol edeceğimiz tam fonksiyon adı
    private val contentDescriptionParamName = "contentDescription"

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        // 1. Çağrılan fonksiyonun tam adını (FQN) bindingContext kullanarak al.
        val resolvedCall = expression.getResolvedCall(bindingContext) ?: return
        val fqName = resolvedCall.resultingDescriptor.fqNameOrNull()?.asString()

        // 2. Eğer FQN, hedeflediğimiz Image composable değilse, işlemi sonlandır.
        if (fqName != imageComposableFqn) {
            return
        }

        // 3. Fonksiyon "androidx.compose.foundation.Image" ise, contentDescription parametresini ara.
        val contentDescriptionArg = expression.valueArguments.find {
            it.getArgumentName()?.asName?.identifier == contentDescriptionParamName
        }

        // 4. 'contentDescription' parametresi Image çağrısında EKSİKSE raporla.
        if (contentDescriptionArg == null) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Image composable (${expression.text}) için 'contentDescription' parametresi eksik. " +
                        "Görsel dekoratif değilse anlamlı bir açıklama ekleyin, dekoratifse null bırakın."
                )
            )
            return // Raporladıktan sonra işlemi sonlandır.
        }

        // 5. 'contentDescription' parametresi VARSA, değerini kontrol et.
        val argExpression = contentDescriptionArg.getArgumentExpression() ?: return

        val isNullLiteral = argExpression.text == "null"
        val isEmptyString = argExpression is KtStringTemplateExpression &&
            (argExpression.entries.isEmpty() || argExpression.entries.all { it.text.isEmpty() }) &&
            argExpression.text == "\"\""

        if (isNullLiteral) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(argExpression),
                    "Image composable (${expression.text}) için 'contentDescription' " +
                        "açıkça null olarak ayarlanmış. " +
                        "Bu, görsel dekoratif ise doğrudur."
                )
            )
        } else if (isEmptyString) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(argExpression),
                    "Image composable (${expression.text}) için 'contentDescription' " +
                        "boş string (\"\") olarak ayarlanmış. " +
                        "Dekoratif görseller için null kullanın veya anlamlı bir açıklama sağlayın."
                )
            )
        }
    }
}
