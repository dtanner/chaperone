### Build chaperone docker image locally
e.g. If you want to test changes by bringing up the example-usage docker-compose file.  
`docker build -t edgescope/chaperone:latest .`
`cd example-usage`
`docker-compose up --build`