# Chaperone
Chaperone is a simple yet powerful monitoring application, intended to be deployed as a docker container.

# Features
- Periodic execution of arbitrary checks. Execute commands directly in a check, or call out to your own scripts.
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
The command just needs to be executable, so the sky's the limit.  Add any apps or scripts to the app that you want and 
call them. Like bash, curl, and jq? You're already covered. Prefer Python or Kotlin Script? Just add them to your 
docker container and call them instead.

You might not even need to call a script file.  For example, if you just want to check that an HTTP call returns a 200 
status code, try this:  
`command = '''[[ $(curl -sL -w '%{http_code}' -o /dev/null 'https://httpbin.org/status/200') == "200" ]]'''`  
*For those unfamiliar with curl and bash, this makes an HTTP call and validates that the response code was 200.*  
*For those new to TOML, the triple-ticks indicate a literal string, which lets us use single and double quotes in 
the command without having to escape them. This is why we use TOML and not JSON or YAML.*

You place all your checks in a directory, and when the app starts up, it runs each check on its schedule or interval.

## Outputs
Where the results of your checks go. A checks result consists of its status (OK or FAIL), and any output from the command. 
You can pick and choose from these output destinations in the global config file. e.g.:
```toml
[outputs.log]
destination="stdout" # options are stdout or a file path. defaults to stdout
format="logstash" # options are pretty or logstash. defaults to pretty

[outputs.influxdb]
db="metrics"
defaultTags={app="myproject-chaperone"} # optional tags applied to all your checks
uri="http://localhost:8086"

[outputs.slack]
webhook="https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
onlyWriteFailures=true # in case you want to get a slack message when failures happen
```  

[More details on output writers](./src/main/kotlin/chaperone/writer/README.md)

## Docker
The base image uses debian-slim.

# Example Usage
See the [example usage](example-usage/README.md) directory for an example project, with some sample checks and output configuration. 
Take that example, then replace its checks with whatever you want to do.  

# Tips
- Each check's command is executed from the directory of where that check is defined, 
so you can use relative paths in the command definition.
This makes it easier to test your command locally in a terminal, then copy/paste the command into a check config when it's ready.
- The `checks.d` directory can contain subdirectories of files. Especially for more complicated checks that might 
have supporting files used by the check, a suggested organizational starting point is to have a directory per check, 
like this:
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
In simple mode, you call a single script, and it returns a single result.  That's fine for really simple stuff, but 
you'll want to read up on template checks to create multiple related checks.[Template Checks](./docs/template-checks.md).

# Interval and Schedule Configuration
You configure each check to run on an `interval` or a `schedule`. You must choose one, and you can't choose both.

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
As mentioned earlier, get your scripts running first via command line or unit test, and _then_ 
configure your check TOML. If a check isn't behaving as expected, you have a couple debugging options:
1. In an individual check's TOML config, you can set `debug = true`. Doing this sets the bash `x` flag when calling 
the script, causing it to output variable values as the script is being evaluated.
2. You can also set the `CHAPERONE_LOG_LEVEL` environment variable to a value of `DEBUG`, which will output more 
information to the log destination as each check is called.

# Contributions
The app is written in Kotlin, and uses the standard kotlin code format. Questions, comments, and pull requests welcome. See [Development.md](Development.md) for some docs on developing locally.
