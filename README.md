# PBus #

It is a server which parses data provided by Google Transit and provides bus information to the Real-time Pittsburgh Bus Schedule App, which is developed by Darren Shen.

## Build & Run ##

```sh
$ cd PBus
$ ./sbt
> container:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
