# LogTemplate [![Scala CI](https://github.com/SergeKireev/LogTemplates/actions/workflows/scala.yml/badge.svg)](https://github.com/SergeKireev/LogTemplates/actions/workflows/scala.yml)

<h3> Description </h3>

LogTemplate is a tool for extracting common patterns from logs (called templates) using a clustering algorithm.

The templating enables to: 
- Automatically extract metrics and facets from logs
- Automatic discovery of event types, which enables the study of sequences of events

Currently is supported:
- Offline raw file ingestion
- Exporting to a column based database (clickhouse)
- Visualization of metrics/facets using Grafana (timeseries charts or raw sql)

<h4> Drain templating </h4>
LogTemplate uses the drain algorithm for log templating.
Academic description of the algorithm may be found here:

http://jiemingzhu.github.io/pub/pjhe_icws2017.pdf

<h3> Getting started </h3>

- Install docker-compose
- Provide a log file in the `example` folder (keep in mind it should correspond to the file mentionned in `application.conf`)
- Run:
```
  # cd docker
  # docker-compose up
```

- The file should have been processed, and modeled into the column database.
- you may connect to `localhost:3000` in your browser to grafana
- an example dashboard is ready to showcase the templates

<h3> Configuration reference </h3>

<h3> Exported data reference </h3>

<h4> Clickhouse </h4>

Data is exported into two tables:

`variables` contains metrics and facets for the variables extracted from log templates

```sql
# variables
ts: DateTime
id: UUID
[type]_[name, id, val]_[index:{0, 10}]
```

`template` contains the text for the log template, so the log can be rehydrated with variable values if needed

```sql
# template
ts: DateTime
id: UUID
text: String
```

<h3> Future improvements </h3>

- Add templating algorithms

- Ingestion sources
  - csv, json file types
  - http streaming
  - kafka, rabbitmq

- Exports targets
  - graph oriented database

- Showcase examples
  - Example to compute elapsed times between to events as postprocessing
  - Examples of ingested data
  