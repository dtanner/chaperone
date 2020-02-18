# Chaperone
Chaperone is a simple and extensible monitoring application, intended to be deployed as a docker container.

# Features
- Scheduled execution of arbitrary checks. Execute commands directly in a check, or call out to your own custom commands.
- Simple configuration using TOML files.
- Pluggable architecture makes it easy to add new output destinations for your check results. Some batteries already included, like InfluxDB, and more will be added over time. 

# Main Concepts
## Check
Each check is a TOML file that looks like this:  
```toml
name = "basic-example"
description = "basic example showing how to run a command/script"
command = "ls | head -n 1" # the command exit code is used to determine status. 0 = OK, anything else = FAIL
interval = "1m"
timeout = "5s"
tags = {env="dev"} # optional - tags let you categorize the output in tools like InfluxDB/Grafana
```
The command executes as a bash command, so the sky's the limit.  Add any apps or scripts to the app that you want and call them.
You might not even need to call a script file.  For example, if you just want to check that an HTTP call returns a 200 status code, try this:  
`command = '''[[ $(curl -sL -w '%{http_code}' -o /dev/null 'https://httpbin.org/status/200') == "200" ]]'''`  

*For those new to TOML, the triple-ticks indicate a literal string, which lets us use single and double quotes in the command without having to escape them. This is why we use TOML and not JSON or YAML.*

You shove all your checks in a directory, and when the app starts up, it runs them on their schedule.

## Outputs
Where the results of your checks go. A checks result consists of its status (OK or FAIL), and any output from the command. 
Each destination is configured in a global config file as an optional table. e.g.:
```toml
[outputs.stdout]

[outputs.influxdb]
db="metrics"
defaultTags={app="myapp-chaperone"} # optional tags applied to all your checks
uri="http://localhost:8086"
```  

## Docker
The base image uses debian-slim. I originally used alpine-slim, but I was spending more time figuring out how to add dependencies than I was writing checks.

# Example Usage
See the [example usage](example-usage/README.md) directory for an example project, with some sample checks and output configuration. 
Take that example, then replace its checks with whatever you want to do.  

# Tips
- Each check's command is executed from the directory of that check, so you can use relative paths in the command definition.
This makes it easier to test your command locally in a terminal, then copy/paste the command into a check config when it's ready.
- The `checks.d` directory can contain subdirectories of files. e.g. `/checks.d/http/test-a.toml`, or `/checks.d/stage/test-a.toml`.
You may find it easier to manage your checks if they're organized by feature/type/environment etc...

# Contributions
The app is written in kotlin, and uses the standard kotlin code format. Questions, comments, and pull requests welcome.
