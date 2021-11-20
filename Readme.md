<h3> Description </h3>

Implementation of the drain algorithm for log templating.

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
```sql
# variables
ts: DateTime
id: UUID
[type]_[name, id, val]_[index:{0, 10}]
```

Contains metrics and facets for the variables extracted from log templates

```sql
# template
ts: DateTime
id: UUID
text: String
```

<h3> Future improvements </h3>

- templating algorithms

- Ingestion sources
  - csv, json file types
  - http streaming
  - kafka, rabbitmq

- Exports targets
  - graph oriented database