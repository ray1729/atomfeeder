# atomfeeder

A Clojure command-line tool to send tweets about new blog posts.

This utility downloads and parses one or more atom feeds, and sends a
tweet for each new post.

## Installation

Clone this git repository.

## Usage

This utility requires credentials for Bitly and Twitter. You will have
to register the application with each service and generate the
appropriate access tokens; see
[http://dev.bitly.com/my_apps.html](Bitly) and
[https://dev.twitter.com/docs/auth/tokens-devtwittercom](Twitter).

Once you have the required OAuth tokens, take a copy of the example
configuration (resources/example-config.edn) and edit appropriately.

You can either run with Leiningen:

    lein run -c my-config.edn

or (recommended) build an uber-jar and run directly:

    lein uberjar

    java -jar target/atomfeeder-0.1.0-standalone.jar --conf my-config.edn

## License

Copyright © 2013 Ray Miller <ray@1729.org.uk>.

Distributed under the Eclipse Public License, the same as Clojure.
