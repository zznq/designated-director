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

```javascript
/teams
```


#### Item

```javascript
/team/{team id}
```


#### Create

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

```javascript
  /team/{team id}
```

