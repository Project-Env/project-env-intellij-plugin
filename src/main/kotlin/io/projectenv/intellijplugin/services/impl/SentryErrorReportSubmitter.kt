package io.projectenv.intellijplugin.services.impl

import com.intellij.diagnostic.LogMessage
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
import java.awt.Component

class SentryErrorReportSubmitter : ErrorReportSubmitter() {

    init {
        Sentry.init { options ->
            options.dsn = "https://0db6a1b60a444f58a1a07dec6f63b060@o981100.ingest.sentry.io/5935634"
            options.isEnableUncaughtExceptionHandler = false
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
            setReleaseVersionOnSentryEvent(sentryEvent)

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
        return if (event.data is LogMessage) {
            val logMessage = (event.data as LogMessage)
            val sentryEvent = SentryEvent(logMessage.throwable)
            if (logMessage.message.isNotBlank()) {
                val sentryMessage = Message()
                sentryMessage.formatted = logMessage.message
                sentryEvent.message = sentryMessage
            }

            sentryEvent
        } else {
            val sentryEvent = SentryEvent()
            if (event.message != null) {
                val sentryMessage = Message()
                sentryMessage.formatted = event.message
                sentryEvent.message = sentryMessage
            } else {
                val sentryMessage = Message()
                sentryMessage.formatted = event.throwableText.split('\n').getOrNull(0)
                sentryEvent.message = sentryMessage
            }

            sentryEvent.level = SentryLevel.ERROR
            sentryEvent.contexts["Stacktrace"] = mapOf("Value" to event.throwableText)

            sentryEvent
        }
    }

    private fun setReleaseVersionOnSentryEvent(sentryEvent: SentryEvent) {
        sentryEvent.release = pluginDescriptor?.version
    }

    private fun successfullySent(sentryEventId: SentryId): Boolean {
        return sentryEventId != SentryId.EMPTY_ID
    }

    private fun captureUserFeedback(sentryEventId: SentryId, additionalInfo: String?) {
        if (additionalInfo != null) {
            val sentryUserFeedback = UserFeedback(sentryEventId)
            sentryUserFeedback.comments = additionalInfo

            Sentry.captureUserFeedback(sentryUserFeedback)
        }
    }
}
