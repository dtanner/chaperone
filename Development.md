### Build/Run chaperone locally

Run the `chaperone.AppKt` main class and pass in the config file and checks directory as program
arguments. You can use the files in `example-usage` as an example. e.g.:
`--checks-dir=~/code/chaperone/example-usage/docker-files/checks.d --config-file=~/code/chaperone/example-usage/docker-files/config.toml`

### Build/Run chaperone docker image locally

Caveat: this is slow, so only do this for functional testing of a docker image.
e.g. If you want to test changes by bringing up the example-usage docker-compose file.

```script
# From the project root directory:
./gradlew distTar
docker build --tag "edgescope/chaperone:latest" .
cd example-usage
docker-compose up --build
```
