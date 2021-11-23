package io.logtemplate.examples.windows

import com.typesafe.config.ConfigFactory
import io.logtemplate.Drain
import io.logtemplate.`import`.common.{Ingestion, ReadConfig}
import io.logtemplate.state.{DrainConfig, DrainState}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WindowsLogsTest extends AnyFunSuite with Matchers {

  ignore("windows event log sample") {
    val sample =
      ("""2016-09-28 04:30:30, Info                  CBS    Starting TrustedInstaller initialization.
        |2016-09-28 04:30:30, Info                  CBS    Loaded Servicing Stack v6.1.7601.23505 with Core: C:\Windows\winsxs\amd64_microsoft-windows-servicingstack_31bf3856ad364e35_6.1.7601.23505_none_681aa442f6fed7f0\cbscore.dll
        |2016-09-28 04:30:31, Info                  CSI    00000001@2016/9/27:20:30:31.455 WcpInitialize (wcp.dll version 0.0.0.6) called (stack @0x7fed806eb5d @0x7fef9fb9b6d @0x7fef9f8358f @0xff83e97c @0xff83d799 @0xff83db2f)
        |2016-09-28 04:30:31, Info                  CSI    00000002@2016/9/27:20:30:31.458 WcpInitialize (wcp.dll version 0.0.0.6) called (stack @0x7fed806eb5d @0x7fefa006ade @0x7fef9fd2984 @0x7fef9f83665 @0xff83e97c @0xff83d799)
        |2016-09-28 04:30:31, Info                  CSI    00000003@2016/9/27:20:30:31.458 WcpInitialize (wcp.dll version 0.0.0.6) called (stack @0x7fed806eb5d @0x7fefa1c8728 @0x7fefa1c8856 @0xff83e474 @0xff83d7de @0xff83db2f)
        |2016-09-28 04:30:31, Info                  CBS    Ending TrustedInstaller initialization.
        |2016-09-28 04:30:31, Info                  CBS    Starting the TrustedInstaller main loop.
        |2016-09-28 04:30:31, Info                  CBS    TrustedInstaller service starts successfully.
        |2016-09-28 04:30:31, Info                  CBS    SQM: Initializing online with Windows opt-in: False
        |2016-09-28 04:30:31, Info                  CBS    SQM: Cleaning up report files older than 10 days.
        |2016-09-28 04:30:31, Info                  CBS    SQM: Requesting upload of all unsent reports.
        |2016-09-28 04:30:31, Info                  CBS    SQM: Failed to start upload with file pattern: C:\Windows\servicing\sqm\*_std.sqm, flags: 0x2 [HRESULT = 0x80004005 - E_FAIL]
        |2016-09-28 04:30:31, Info                  CBS    SQM: Failed to start standard sample upload. [HRESULT = 0x80004005 - E_FAIL]
        |2016-09-28 04:30:31, Info                  CBS    SQM: Queued 0 file(s) for upload with pattern: C:\Windows\servicing\sqm\*_all.sqm, flags: 0x6
        |2016-09-28 04:30:31, Info                  CBS    SQM: Warning: Failed to upload all unsent reports. [HRESULT = 0x80004005 - E_FAIL]
        |2016-09-28 04:30:31, Info                  CBS    No startup processing required, TrustedInstaller service was not set as autostart, or else a reboot is still pending.
        |2016-09-28 04:30:31, Info                  CBS    NonStart: Checking to ensure startup processing was not required.
        |2016-09-28 04:30:31, Info                  CSI    00000004 IAdvancedInstallerAwareStore_ResolvePendingTransactions (call 1) (flags = 00000004, progress = NULL, phase = 0, pdwDisposition = @0xb6fd90
        |2016-09-28 04:30:31, Info                  CSI    00000005 Creating NT transaction (seq 1), objectname [6]"(null)"
        |2016-09-28 04:30:31, Info                  CSI    00000006 Created NT transaction (seq 1) result 0x00000000, handle @0x214
        |""".stripMargin).split("\n")

    val config =
      ConfigFactory.parseString("""
        |read {
        |    # dissect pattern to find the timestamp (ts: mandatory) and message (msg: mandatory) for the log entry
        |    # Note: each line which will not match this pattern will be appended to previous event
        |    dissect.pattern = "%{ts} %{+ts}, %{log_level}                  %{service}    %{msg}"
        |    # the format which should be used to parse the timestamp parsed in the log
        |    date-format = "yyyy-MM-dd hh:mm:ss"
        |}
        |
        |drain {
        |    # can be bracket-aware, simple
        |    tokenizer = "bracket-aware"
        |    # do not tokenize what comes after this pattern, and enclose it in an 'enclosed token'
        |    exception-pattern = "\n"
        |}
        |""".stripMargin)
    val readConfig = ReadConfig(config)
    val ingestion = new Ingestion(readConfig)

    val drain = new Drain(DrainState(), DrainConfig(config))
    sample.foreach {
      line =>
        val logEntry = ingestion.dissect.extractLogEvent(line).toOption.get
        val preprocessed = drain.preprocess(logEntry.content)
        drain.process(preprocessed)
    }
    assert(drain.drainState.lengthMap.values.size == 12)
  }

}
