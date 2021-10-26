# Chaperone
Chaperone is a simple yet powerful monitoring application, intended to be deployed as a docker container.

# Features
- Scheduled execution of arbitrary checks. Execute commands directly in a check, or call out to your own custom commands.
- Simple configuration using TOML files. 
- Basic checks are super easy, and dynamic template-driven checks are barely an inconvenience.
- Configurable output destinations for your check results, for example stdout, InfluxDB, and Slack.

# Main Concepts
## Check
Each check is a TOML file that looks like this:  
```toml
name = "basic example"
description = "basic example showing how to run a command/script"
command = "ls | head -n 1" # the command exit code is used to determine status. 0 = OK, anything else = FAIL
interval = "1m" # the command will run every minute. alternatively, you can configure a cron schedule.
timeout = "5s"
tags = {env="dev"} # optional - tags let you categorize the output in tools like InfluxDB/Grafana
debug = true # optional - defaults to false. If set to true, this logs the commands as they're run.
```
The command executes as a bash command, so the sky's the limit.  Add any apps or scripts to the app that you want and call them.
You might not even need to call a script file.  For example, if you just want to check that an HTTP call returns a 200 status code, try this:  
`command = '''[[ $(curl -sL -w '%{http_code}' -o /dev/null 'https://httpbin.org/status/200') == "200" ]]'''`  

*For those new to TOML, the triple-ticks indicate a literal string, which lets us use single and double quotes in the command without having to escape them. This is why we use TOML and not JSON or YAML.*

You shove all your checks in a directory, and when the app starts up, it runs them on their schedule.

## Outputs
Where the results of your checks go. A checks result consists of its status (OK or FAIL), and any output from the command. 
Each destination is configured in the global config file. e.g.:
```toml
[outputs.log]
destination="stdout" # options are stdout or a file path. defaults to stdout
format="logstash" # options are pretty or logstash. defaults to pretty

[outputs.influxdb]
db="metrics"
defaultTags={app="myproject-chaperone",env="dev"} # optional tags applied to all your checks
uri="http://localhost:8086"

[outputs.slack]
webhook="https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
onlyWriteFailures=true # in case you only want to get a slack message when failures happen
```  

[More details](./src/main/kotlin/chaperone/writer/README.md)

## Docker
The base image uses debian-slim. 

# Example Usage
See the [example usage](example-usage/README.md) directory for an example project, with some sample checks and output configuration. 
Take that example, then replace its checks with whatever you want to do.  

# Tips
- Each check's command is executed from the directory of that check, so you can use relative paths in the command definition.
This makes it easier to test your command locally in a terminal, then copy/paste the command into a check config when it's ready.
- The `checks.d` directory can contain subdirectories of files. e.g. `/checks.d/http/test-a.toml`, or `/checks.d/stage/test-a.toml`.
You may find it easier to manage your checks if they're organized by feature/type/environment etc...
You can also put non-check files in your checks directory. This is a good way to organize lots of checks that use lots of files as part of their actions.
With this technique, you can easily add and remove everything related to a check in a single directory.
e.g.:
```
checks.d/
    check-a/
        scripta1.sh
        scripta2
    check-b/
        scriptb.sh
    etc...
```

# Template Checks
In simple mode, you call a single script, and it returns a single result.  
If you want to run multiple checks driven by a template, see [Template Checks](./docs/template-checks.md).

# Interval and Schedule Configuration
You can configure each check to run on an `interval` or a `schedule`. You must choose one, and you can't choose both.

### Interval
By setting an `interval` in your check configuration, it will immediately run when chaperone is started, and then 
run every XT interval after that, where X is the interval value and T is the time unit of measure. 
The time unit of measurement can be one of `s` (seconds), `m` (minutes), `h` (hours), or even `d` (days).
For example, `interval = "10m"` will run the check every 10 minutes.

### Schedule
If you configure your check with a `schedule`, then 
[UNIX crontab Convention](https://www.unix.com/man-page/linux/5/crontab/) is supported.
The syntax is a string containing 5 fields, where each field represents in this order:
- minute
- hour
- day of month
- month
- day of week

For example, to run every day, five minutes after midnight:
`schedule = "5 0 * * *"`

*Limitation*: It doesn't support special string values like "@hourly" or "@daily". It should support everything 
else though, like wildcards (`*`), ranges (`2,4`), lists (`1-5`), and step values (`*/2`).

# Script writing process and debugging
As mentioned earlier, get your scripts running first by testing them via command line, and _then_ configure your 
check TOML. If a check isn't behaving as expected, you have a couple debugging options:
1. In an individual check's TOML config, you can set `debug = true`. Doing this sets the bash `x` flag when calling 
the script, causing it to output variable values as the script is being evaluated.
2. You can also set the `CHAPERONE_LOG_LEVEL` environment variable to a value of `DEBUG`, which will output more 
information to the log destination as each check is called.  

# Contributions
The app is written in kotlin, and uses the standard kotlin code format. Questions, comments, and pull requests welcome. See [Development.md](Development.md) for some docs on developing locally.
