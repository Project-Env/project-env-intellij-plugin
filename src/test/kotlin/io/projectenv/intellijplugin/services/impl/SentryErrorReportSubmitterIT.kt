package io.projectenv.intellijplugin.services.impl

import com.intellij.diagnostic.LogMessage
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import java.awt.Component
import java.util.*

class SentryErrorReportSubmitterIT : BasePlatformTestCase() {

    @Test
    fun testGetReportActionText() {
        assertThat(SentryErrorReportSubmitter().reportActionText).isEqualTo("Report to Author")
    }

    @Test
    fun testSubmitErrors() {
        executeSubmitErrorsTest()
    }

    /**
     * As Lambdas are compiled into normal methods having a generated name starting with the name of the method declaring the Lambda,
     * they are treated as test methods by JUnit. Therefore, we move the test content into a method not matching the JUnit naming convention.
     */
    private fun executeSubmitErrorsTest() {
        mockStatic(Sentry::class.java).use { sentry ->
            sentry.`when`<Void> { Sentry.init(any<Sentry.OptionsConfiguration<SentryOptions>>()) }.then {
                // noop
            }
            sentry.`when`<SentryId> { Sentry.captureEvent(any()) }.then {
                val event = it.getArgument<SentryEvent>(0)
                assertThat(event.message?.formatted).isEqualTo("test message")

                SentryId(UUID.randomUUID())
            }
            sentry.`when`<Void> { Sentry.captureUserFeedback(any()) }.then {
                val userFeedback = it.getArgument<UserFeedback>(0)
                assertThat(userFeedback.comments).isEqualTo("test comment")
            }

            val event = IdeaLoggingEvent("test message", Throwable("test error"))

            val component = mock(Component::class.java)
            SentryErrorReportSubmitter().submit(arrayOf(event), "test comment", component) {
                assertThat(it.status).isEqualTo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
            }
        }
    }

    @Test
    fun testSubmitErrorsWithFailedSubmission() {
        executeSubmitErrorsWithFailedSubmissionTest()
    }

    /**
     * As Lambdas are compiled into normal methods having a generated name starting with the name of the method declaring the Lambda,
     * they are treated as test methods by JUnit. Therefore, we move the test content into a method not matching the JUnit naming convention.
     */
    private fun executeSubmitErrorsWithFailedSubmissionTest() {
        mockStatic(Sentry::class.java).use { sentry ->
            sentry.`when`<Void> { Sentry.init(any<Sentry.OptionsConfiguration<SentryOptions>>()) }.then {
                // noop
            }
            sentry.`when`<SentryId> { Sentry.captureEvent(any()) }.then {
                val event = it.getArgument<SentryEvent>(0)
                assertThat(event.message?.formatted).isEqualTo("test message")

                SentryId.EMPTY_ID
            }

            val event = IdeaLoggingEvent("test message", Throwable("test error"))

            val component = mock(Component::class.java)
            SentryErrorReportSubmitter().submit(arrayOf(event), "test comment", component) {
                assertThat(it.status).isEqualTo(SubmittedReportInfo.SubmissionStatus.FAILED)
            }
        }
    }

    @Test
    fun testSubmitErrorsWithLogMessage() {
        executeSubmitErrorsLogMessageTest()
    }

    private fun executeSubmitErrorsLogMessageTest() {
        mockStatic(Sentry::class.java).use { sentry ->
            val throwable = Throwable("test error")
            val message = "test message"

            sentry.`when`<Void> { Sentry.init(any<Sentry.OptionsConfiguration<SentryOptions>>()) }.then {
                // noop
            }
            sentry.`when`<SentryId> { Sentry.captureEvent(any()) }.then {
                val event = it.getArgument<SentryEvent>(0)
                assertThat(event.message?.formatted).isEqualTo(message)
                assertThat(event.throwable).isEqualTo(throwable)

                SentryId(UUID.randomUUID())
            }
            sentry.`when`<Void> { Sentry.captureUserFeedback(any()) }.then {
                val userFeedback = it.getArgument<UserFeedback>(0)
                assertThat(userFeedback.comments).isEqualTo("test comment")
            }

            val component = mock(Component::class.java)
            SentryErrorReportSubmitter().submit(arrayOf(LogMessage.createEvent(throwable, message)), "test comment", component) {
                assertThat(it.status).isEqualTo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
            }
        }
    }
}
