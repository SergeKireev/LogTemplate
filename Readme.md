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

<h3> Future improvements <h3>