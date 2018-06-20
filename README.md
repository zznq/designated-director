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
<<<<<<< HEAD
=======

## API

### Teams

#### Index

url
```javascript
/teams
```


#### Item

url
```javascript
/teams/{team id}
```


#### Create

url
``` javascript
/teams
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
  /team/{team id}
```

>>>>>>> 54265f5... updates docs
