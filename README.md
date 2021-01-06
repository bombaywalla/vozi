# vozi

[![Clojars Project](https://img.shields.io/clojars/v/bombaywalla/vozi.svg)](https://clojars.org/bombaywalla/vozi)
[![cljdoc badge](https://cljdoc.org/badge/bombaywalla/vozi)](https://cljdoc.org/d/bombaywalla/vozi/CURRENT)

Visualization library around Oz. API inspired by `ggplot2`.

It is not clear that `vozi` provides enough benefit at the moment,
since one still has to understand the semantics of `vaga-lite` to use
it effectively.  Work in progress. All APIs subject to change.

## Usage

### Coordinates in deps.edn

	metasourous/oz {:mvn/version "RELEASE"}
	bombaywalla/vozi {:mvn/version "0.1.6"}

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

Run the project's tests:

    $ clojure -M:test:runner

### Create a jar

Build a deployable jar of this library:

    $ clojure -X:jar

### Install the jar locally

Install it locally:

    $ clojure -M:install

### Deploy the jar to clojars

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -M:deploy

## License

Copyright © 2020-2021 Dorab Patel

Distributed under the MIT License.
