# local-dev
Provides a docker-compose file with some sample checks configured to send output to a local influxdb.  
Also includes a local grafana container with a sample dashboard to show how you can view the sample data.

Usage: `docker-compose up --build chaperone`

Grafana URL: http://localhost:3000
Default User: `admin/admin`

The InfluxDb datasource url should be configured as:
Url: `http://influxdb:8086`
Database: `metrics` (it might not be created yet, but should be auto-created after a minute when the metrics are published)
A sample grafana dashboard export is also included in this directory
Import it into grafana using the import tool to see how you can use it with the check results.