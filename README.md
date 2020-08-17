# vozi

Visualization library around Oz

[![Clojars Project](https://img.shields.io/clojars/v/bombaywalla/vozi.svg)](https://clojars.org/bombaywalla/vozi)
[![cljdoc badge](https://cljdoc.org/badge/bombaywalla/vozi)](https://cljdoc.org/d/bombaywalla/vozi/CURRENT)

## Usage

### Coordinates in deps.edn

	metasourous/oz {:mvn/version "RELEASE"}
	bombaywalla/vozi {:mvn/version "0.1.2"}

### Quickstart example

Make sure a browser is running. And there is nothing listening on the
default Oz port of 10666.

Then

```clojure
	(require '[oz.core :as oz])
	(require '[bombaywalla.vozi :as vozi])

	(def data [{:x 1 :y 1} {:x 5 :y 10} {:x 10 :y 1}])
	(def plot (-> (vozi/line-plot)
	              (vozi/add-data data)))

	(oz/view! plot)
```

You should now see a line plot in your browser.

### Tests

Run the project's tests (they currently fail):

    $ clojure -A:test:runner

### Create a jar

Build a deployable jar of this library:

    $ clojure -A:jar

### Install the jar locally

Install it locally:

    $ clojure -A:install

### Deploy the jar to clojars

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -A:deploy

## License

Copyright © 2020 Dorab Patel

Distributed under the MIT License.
