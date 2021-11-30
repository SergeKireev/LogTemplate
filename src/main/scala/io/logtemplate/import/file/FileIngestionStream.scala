package io.logtemplate.`import`.file

import cats.effect.IO
import fs2.Stream
import io.logtemplate.domain.log.LogEntry

object FileIngestionStream {
  def buildLogStream(reader: LogFileReader): Stream[IO, LogEntry] = {
    Stream.bracket(reader.open())(_ => reader.close()).flatMap {
      _ =>
        Stream.repeatEval(reader.read()).unNoneTerminate
    }
  }
}
