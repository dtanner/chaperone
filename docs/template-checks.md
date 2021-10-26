# Template Checks
This is a slightly more advanced usage of Chaperone, but is very common and useful for collections of similar checks.
If you're just starting out, make sure you understand the basic usage scenarios first.  

# Usage Scenario
Let's say we have a bunch of apps running on our servers, and we want to periodically check that they're all up.  
The apps that are running isn't a constant; it grows and shrinks as teams bring new apps up and retire old apps.  
We have a script named `list-apps.sh` that when called, returns a list of the currently deployed app names like this:  
```
arbalest
thorn
apostate
```

We also have a script we can call to get the current health of an app, named `get-app-health.sh` that takes a single argument `$appName`.  
We want to write the health status of each app as a separate health check every 5 minutes. 
Let's create `app-health.toml` to do this...

```toml
description = "team X's application health check status"
template = "../scripts/list-apps.sh"
name = "app - $1"
command = "../scripts/get-app-health.sh"
interval = "10m"
timeout = "10s"
tags = {category="appcheck", env="prod", app="$1"}
```

Now every five minutes, chaperone will call `list-apps.sh`, and for each app returned, will execute `get-app-health.sh` for that app. 
stdout might look like this:  

```
2020-02-21T01:20:02.531372Z      app - arbalest          OK   {category=appcheck,env=prod,app=arbalest}     app is healthy
2020-02-21T01:20:02.532529Z      app - thorn             OK   {category=appcheck,env=prod,app=thorn}        app is healthy
2020-02-21T01:20:02.532529Z      app - apostate          FAIL {category=appcheck,env=prod,app=apostate}     app failed health check
```

# How Does it Work?
The `template` value is what creates the dynamic list of checks. 
Its output is used to generate the arguments that will be passed into the `command`, `name`, and `tags` properties.  
The `name`, `tags`, and `command` properties can use bash positional argument syntax to perform string interpolation. 
e.g. `$1`, `$2`, `$@` should all work.

There's a couple tests for templates in [CheckTest.kt](../src/test/kotlin/chaperone/CheckTest.kt) for reference.  
There's also an [annotated example](../example-usage/docker-files/checks.d/template-example.toml) to start from.

