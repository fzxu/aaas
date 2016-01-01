# AssS
Assets(Images only, for now) as a Service written in Scala using Akka Stream &amp; Http and stores in Cassandra.

It only handles images with various resize/crop features and cache them in the disk for massive GET.

AssS makes full use of the Akka Http and Akka Stream which is well known as 'Reactive Streams'. It should be a high
performance image server.

## Preparation

You need:

* Java 8
* Cassandra DB 2.2.x
* SBT: [http://www.scala-sbt.org/](http://www.scala-sbt.org/)

If you are a mac user and use `brew`, install them are as simple as:

```
brew install cassandra
brew install sbt
```

## Quick Start

* Create keyspace in Cassandra

Connect to Cassandra console:

```
cqlsh

Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 2.2.3 | CQL spec 3.3.1 | Native protocol v4]
Use HELP for help.
```

Create the keyspace:

```
CREATE KEYSPACE aaas WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };
```

* Clone this project

```
git clone git@github.com:arkxu/aaas.git
```

* Compile the project

```
cd aaas
sbt clean assembly
```

* Start the server

```
java -cp target/scala*/aaas-assembly-*.jar com.arkxu.aaas.Main
```

## Configuration

By default the server will load the config file from the [resource](https://github.com/arkxu/aaas/blob/master/src/main/resources/application.conf).

Feel free to copy and modify it, add yours to the classpath. The server can load customized configuration.

e.g.

Copy the application.conf to a new conf folder and rename to aaas.conf, run the server with that

```
mkdir conf
cp src/main/resources/application.conf conf/aaas.conf
java -cp conf:`ls target/scala*/aaas-assembly*.jar | tr ' ' ':'` -Dconfig.resource=./aaas.conf com.arkxu.aaas.Main
```

## Upload Image

The endpoint to upload image: `http://localhost:8090/v1`

Feel free to use any tools. e.g. [Postman](http://www.getpostman.com/) to send image to the server.

* Use Basic Authentication, default user/password: demo:demo

![Authentication](/docs/imgs/screen1.png)

![Authentication](/docs/imgs/screen2.png)

* Choose file(s) to upload

Let's upload some images to `/foo/bar`

![Authentication](/docs/imgs/screen3.png)

* Files can be multiple

In the response you will get the uploaded file UUID

![Authentication](/docs/imgs/screen4.png)

## Get Image from Different Sizes

Open your browser and access the image using:

`http://localhost:8090/v1/{UUID}.jpg`

You will get the image back in default sizes(Remember the default width/height in configuration?).

You also can get the resized image in two modes: **resize** or **crop**

### Get Resized Image

`http://localhost:8090/v1/{UUID}__{width}z{height}.jpg`

### Get Cropped Image

`http://localhost:8090/v1/{UUID}__{width}x{height}.jpg`


## List Images under a path

Send GET request to the path you uploaded to, it will return all the image UUIDs in that folder:
 
`http://localhost:8090/v1/foo/bar/`

## Delete Image(s)

Send DELETE request (with authentication of course) to a single image, or to its parent folder:

This will delete all the images under `/foo/bar`

DELETE `http://localhost:8090/v1/foo/bar/`

This deletes only one image with that specified UUID:

DELETE `http://localhost:8090/v1/{UUID}.jpg`

# Contribution

Please request or contribute back with any features/codes!

# License

MIT, feel free to copy and modify
