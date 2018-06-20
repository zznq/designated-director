# Designated Director

A small utlitly to help manager live draft day.

## Dependancies

  - neo4j (use docker-compose)
  - jdk 1.8
    - `brew cask install java8`
  - sbt
    - `brew install sbt`

## Building

```bash
$ sbt compile
```

## Running

```
$ sbt
$ project api
$ run
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
``` javascript
{
	"name": "Boom goes the roof"
}
```

#### Delete

url
```javascript
DELETE  /team/{team id}
```

