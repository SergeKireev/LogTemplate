package io.logtemplate.examples.hadoop

import com.typesafe.config.ConfigFactory
import io.logtemplate.Drain
import io.logtemplate.`import`.common.{Ingestion, FileImportConfig}
import io.logtemplate.state.{DrainConfig, DrainState}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Failure

class HadoopTest extends AnyFunSuite with Matchers {
  test("hadoop sample test") {
    val sample =
      ("""2015-12-03 14:37:47,611 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: STARTUP_MSG:
        |2015-12-03 14:37:47,618 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: registered UNIX signal handlers for [TERM, HUP, INT]
        |2015-12-03 14:37:48,253 INFO org.apache.hadoop.metrics2.impl.MetricsConfig: loaded properties from hadoop-metrics2.properties
        |2015-12-03 14:37:48,315 INFO org.apache.hadoop.metrics2.impl.MetricsSystemImpl: Scheduled snapshot period at 10 second(s).
        |2015-12-03 14:37:48,315 INFO org.apache.hadoop.metrics2.impl.MetricsSystemImpl: DataNode metrics system started
        |2015-12-03 14:37:48,319 INFO org.apache.hadoop.hdfs.server.datanode.BlockScanner: Initialized block scanner with targetBytesPerSec 1048576
        |2015-12-03 14:37:48,321 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Configured hostname is mesos-master-1
        |2015-12-03 14:37:48,329 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Starting DataNode with maxLockedMemory = 0
        |2015-12-03 14:37:48,354 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Opened streaming server at /0.0.0.0:50010
        |2015-12-03 14:37:48,356 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Balancing bandwith is 1048576 bytes/s
        |2015-12-03 14:37:48,356 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Number threads for balancing is 5
        |""".stripMargin).split('\n')
    val config =
      ConfigFactory.parseString("""
                                  |read {
                                  |    # dissect pattern to find the timestamp (ts: mandatory) and message (msg: mandatory) for the log entry
                                  |    # Note: each line which will not match this pattern will be appended to previous event
                                  |    dissect.pattern = "%{ts} %{+ts} %{level} %{class}: %{msg}"
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
    val readConfig = FileImportConfig(config)
    val ingestion = new Ingestion(readConfig)

    val drain = new Drain(DrainState(), DrainConfig(config))
    sample.foreach {
      line =>
        val logEntry = ingestion.dissect.extractLogEvent(line).recoverWith {
          e =>
            e.printStackTrace()
            Failure(e)
        }.toOption.get
        val preprocessed = drain.preprocess(logEntry.content)
        drain.process(preprocessed)
    }
    assert(drain.drainState.lengthMap.values.size == 5)
  }
}
