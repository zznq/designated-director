# Designated Director

A small utlitly to help manager live draft day.

[![Build Status](https://travis-ci.org/zznq/designated-director.svg?branch=master)](https://travis-ci.org/zznq/designated-director) 
[![Maintainability](https://api.codeclimate.com/v1/badges/1349e51a942942c4c486/maintainability)](https://codeclimate.com/github/zznq/designated-director/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/1349e51a942942c4c486/test_coverage)](https://codeclimate.com/github/zznq/designated-director/test_coverage)

## Dependencies
  - neo4j (use docker-compose)
    - [Docker for Mac](https://docs.docker.com/docker-for-mac/install/)
  - JDK 1.8
    - `brew cask install java8`
  - SBT
    - `brew install sbt`

## Building

```bash
$ sbt compile
```

## Running

Neo4j:
```
docker-compose -f docker/docker-compose.yml up
```

Application:
```bash
$ sbt 'project api' run
```

## API

### Teams

#### Index

url
```javascript
GET /teams
```


#### Item

url
```javascript
GET /teams/{team id}
```


#### Create

url
``` javascript
POST /teams
```

body
``` json
{
	"name": "Boom goes the roof"
}
```

#### Delete

url
```javascript
DELETE  /team/{team id}
```


![forthebadge](https://forthebadge.com/images/badges/made-with-crayons.svg)
