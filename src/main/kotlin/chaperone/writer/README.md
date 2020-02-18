# Output Writers Implemented So Far

## StdOut
Writes results to stdout, formatted as:  
$timestamp $check_name $status $check_output  
```
2020-02-17T20:41:04.547812Z      simulated-check-3   OK   simulated pass
```

## InfluxDB
Writes to InfluxDB as a [Micrometer Timer](http://micrometer.io/docs/concepts#_timers), whose rows look like this:
```
select * from check_status_code
name: check_status_code
time                app             check             count env mean metric_type sum upper
----                ---             -----             ----- --- ---- ----------- --- -----
1581693844323000000 myapp-chaperone simulated-check-1 2     dev 0    histogram   0   0
1581694383566000000 myapp-chaperone simulated-check-1 2     dev 0.5  histogram   1   1
```
It records a `0` value for OK, and `1` for FAIL to a metric named `check_status_code`.
The reason it uses a Timer is because it stores the upper with each metric, which lets us query for any failures within a timespan. e.g.:  
`SELECT max("upper") FROM "check_status_code" WHERE ("app" = 'myapp-chaperone') AND $timeFilter GROUP BY time($__interval), "check" fill(null)`  
You can use this query for visualization with statusmap graphs, and also for alerting with a standard graph. 
Having a fill of null lets you pessimisticly alert on missing values in case there's an issue running the checks or getting to their data.  

Default tags and any check-specific tags become columns in InfluxDB, so you can slice and dice different charts based on those.
e.g. Maybe you want a separate chart or alerting rules based on an `env` tag that you've specified in the checks.
