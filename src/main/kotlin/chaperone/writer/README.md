# Output Writers Implemented So Far

## StdOut
Writes results to stdout, formatted as:  
$timestamp $check_name $status $check_output  
```
2020-02-17T20:41:04.547812Z      simulated-check-3   OK   simulated pass
```

## InfluxDB
Writes to InfluxDB as a Timer.  Counts make more logical sense, but i couldn't figure out how to combine them with the statusmap plugin, 
since it wants the retrieved value to be a discrete value of e.g. 0 for success, 1 for fail, etc.  
https://grafana.com/grafana/plugins/flant-statusmap-panel

It records a `0` value for OK, and `1` for FAIL to a metric named `check_status_code`. 
From that you can use a query to find failures using the max(upper) column. e.g.:  
`SELECT max("upper") FROM "check_status_code" WHERE ("app" = 'foo') AND $timeFilter GROUP BY time($__interval), "check" fill(null)`  
You can use this query for visualization with statusmap charts, and also for alerting with a standard graph. 
Having a fill of null lets you pessimisticly alert on missing values in case there's an issue running the checks or getting to their data.  

Default tags and any check-specific tags are included as tags to InfluxDB, so you can slice and dice different charts based on those.
e.g. Maybe you want a separate chart or alerting rules based on an `env` tag that you've specified in the checks.
