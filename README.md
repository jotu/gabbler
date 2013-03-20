Gabbler
=======

Gabbler is a simple chat demo for a modern web application: Single-page client based on [AngularJS](http://angularjs.org), highly scalable RESTful server written in [Scala](http://www.scala-lang.org), [Akka](http://akka.io) and [spray](http://spray.io).

The Gabbler server is built on top of Akka actors, non-blocking and asynchronous from top to bottom. Therefore it can easily be scaled up and out. In order to keep things simple and focus on the core message-passing architecture, Basic Authentication and Ajax with long polling are used. This might be revised when spray supports OAuth and WebSocket, but long polling is particularly well suited to showcase the strengths of Akka actors and spray.

Huge thanks to Mathias Doenitz from the spray team for his help and contributions!

Running Gabbler
---------------

Clone this repository, `cd` into it and start [sbt](http://www.scala-sbt.org). Then simply execute `run` to start the Gabbler server:

```
gabbler$ sbt
[info] ...
[info] Set current project to gabbler (in build file:/Users/heiko/projects/gabbler/)
> run
[info] Running name.heikoseeberger.gabbler.GabblerServerApp 
Hit ENTER to exit ...

```

Open a browser and point it to [localhost:8080/](http://localhost:8080/). Log in with one of the following username/password combinations: arjen/arjen, bastian/bastian or claudio/claudio.

Enter a message in the text area on the left, click the "Gabble away" button and watch the message appear on the right.

Open a second browser window which needs to be a different application if you want to use a separate login (e.g. first Safari, second Chrome), enter another message and watch it appear in both browser windows.

Finally, hit the ENTER key in the sbt session to stop the Gabbler server.

A look under the hood
---------------------

TODO

Contribution policy
-------------------

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

License
-------

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html). Feel free to use it accordingly.
