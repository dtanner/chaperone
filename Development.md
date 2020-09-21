### Build/Run chaperone locally
Run the `chaperone.AppKt` main class and pass in the config file and checks directory as program arguments.
You can use the files in `example-usage` as an example. e.g. if your repo code lives in `/Users/bob/code`:  
`--checks-dir=/Users/bob/code/chaperone/example-usage/docker-files/checks.d --config-file=/Users/bob/code/chaperone/example-usage/docker-files/config.toml`

### Build chaperone docker image locally
Caveat: this is slow, so only do this for final functional testing of a docker image.
e.g. If you want to test changes by bringing up the example-usage docker-compose file.  
```script
docker build -t edgescope/chaperone:latest .
cd example-usage
docker-compose up --build
```
