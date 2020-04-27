# Output Writers Implemented So Far

## Log
Logs the results in a couple configurable ways. The options are:
- *destination*: with possible values of `"stdout"` or `"/a/valid/file/path.log"`
- *format*: either `"pretty"` or `"logstash"`

The pretty format looks like this:
```
2020-04-25 21:13:45,472 INFO {name=check, tags={env=dev}, status=OK} all is ok
```

The logstash format looks like this:
```
{"@timestamp":"2020-04-25T21:14:42.643-05:00","message":"all is ok","logger":"logwriter","level":"INFO","name":"check","tags":"{env=dev}","status":"OK"}
```

## InfluxDB
Writes to InfluxDB, whose rows look like this:
```
> select * from check_status_code
name: check_status_code
time                app             check                   env  letter value
----                ---             -----                   ---  ------ -----
1584027735225000000 myapp-chaperone basic example           dev         0
1584027735225000000 myapp-chaperone command execution error dev         1
1584027735225000000 myapp-chaperone simulated check         dev         0
1584027735349000000 myapp-chaperone template example - a    test a      0
1584027735350000000 myapp-chaperone template example - b    test b      0
1584027735351000000 myapp-chaperone template example - c    test c      0
1584027735603000000 myapp-chaperone sample http check       dev         0
```
It records a `0` value for OK, and `1` for FAIL to a metric named `check_status_code`.  

A grafana query using the [statusmap-plugin](https://grafana.com/grafana/plugins/flant-statusmap-panel) chart for the above data might be:  
`SELECT max("value") FROM "check_status_code" WHERE ("app" = 'myapp-chaperone') AND $timeFilter GROUP BY time($__interval), "check" fill(null)`  
You can use this query for visualization with statusmap graphs, and also for alerting with a standard graph. 
Having a fill of null lets you pessimisticly alert on missing values in case there's an issue running the checks or getting to their data.  

Default tags and any check-specific tags become columns in InfluxDB, so you can slice and dice different charts based on those.  
e.g. typical default tags are application and environment name.

## Slack
Writes to the slack API. Requires a slack webhook configured for a channel.