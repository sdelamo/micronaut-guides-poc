package example.micronaut

import io.micronaut.context.condition.Condition
import io.micronaut.context.condition.ConditionContext
import io.micronaut.core.util.StringUtils

class SendGridEmailCondition : Condition {

    override fun matches(context: ConditionContext<*>?): Boolean {
        return envOrSystemProperty("SENDGRID_APIKEY", "sendgrid.apikey") &&
                envOrSystemProperty("SENDGRID_FROM_EMAIL", "sendgrid.fromemail")
    }

    private fun envOrSystemProperty(env: String, prop: String): Boolean {
        return StringUtils.isNotEmpty(System.getProperty(prop)) || StringUtils.isNotEmpty(System.getenv(env))
    }
}
