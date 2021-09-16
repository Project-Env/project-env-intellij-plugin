package io.projectenv.intellijplugin.services.impl

import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.Consumer
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.UserFeedback
import io.sentry.protocol.Message
import io.sentry.protocol.SentryId
import org.apache.commons.lang.StringUtils
import java.awt.Component

class SentryErrorReportSubmitter : ErrorReportSubmitter() {

    init {
        Sentry.init { options ->
            options.dsn = "https://0db6a1b60a444f58a1a07dec6f63b060@o981100.ingest.sentry.io/5935634"
            options.enableUncaughtExceptionHandler = false
        }
    }

    override fun getReportActionText(): String {
        return "Report to Author"
    }

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        ProgressManager.getInstance().run {
            val sentryEvent = createSentryEventFromError(getMainError(events))
            setReleaseVersionOnSentryEvent(sentryEvent, pluginDescriptor?.version)

            val sentryEventId = Sentry.captureEvent(sentryEvent)
            if (successfullySent(sentryEventId)) {
                captureUserFeedback(sentryEventId, additionalInfo)

                consumer.consume(SubmittedReportInfo(SubmissionStatus.NEW_ISSUE))
            } else {
                consumer.consume(SubmittedReportInfo(SubmissionStatus.FAILED))
            }
        }

        return true
    }

    private fun getMainError(events: Array<out IdeaLoggingEvent>): IdeaLoggingEvent {
        return events[0]
    }

    private fun createSentryEventFromError(event: IdeaLoggingEvent): SentryEvent {
        val sentryEvent = SentryEvent()

        val sentryMessage = Message()
        sentryMessage.formatted = event.throwableText.split('\n').getOrNull(0)
        sentryEvent.message = sentryMessage

        sentryEvent.level = SentryLevel.ERROR
        sentryEvent.contexts["Stacktrace"] = mapOf("Value" to event.throwableText)

        return sentryEvent
    }

    private fun setReleaseVersionOnSentryEvent(sentryEvent: SentryEvent, releaseVersion: String?) {
        sentryEvent.release = releaseVersion
    }

    private fun successfullySent(sentryEventId: SentryId): Boolean {
        return sentryEventId != SentryId.EMPTY_ID
    }

    private fun captureUserFeedback(sentryEventId: SentryId, additionalInfo: String?) {
        if (StringUtils.isNotEmpty(additionalInfo)) {
            val sentryUserFeedback = UserFeedback(sentryEventId)
            sentryUserFeedback.comments = additionalInfo

            Sentry.captureUserFeedback(sentryUserFeedback)
        }
    }
}
