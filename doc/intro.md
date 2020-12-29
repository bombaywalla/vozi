# Introduction to vozi

`vozi` is just a thin wrapper around `oz` (which, in turn, is a
wrapper around `vega` and `vaga-lite`).

The idea was to create an API similar to `ggplot2` that would allow for
the incremental creation of a plot.

It is not clear that `vozi` provides enough benefit at the moment,
since one still has to understand the semantics of `vaga-lite` to use
it effectively.

## Example usage

Here are some examples of `vozi` usage.

```clojure

(require '[oz.core :as oz])
(require '[bombaywalla.vozi :as vozi])

(def dataset-url "https://raw.githubusercontent.com/vega/vega-datasets/master/data/")

(defn plot-example-bar-chart
  "Plot the example bar chart from https://vega.github.io/vega-lite/"
  []
  (let [url (str dataset-url "seattle-weather.csv")
        base-plot (vozi/bar-plot {:x-field "date"
		                          :x-type "ordinal"
								  :x-opts {:timeUnit "month"}
								  :y-field "precipitation"
								  :y-opts {:aggregate "mean"}})
        plot (vozi/add-data base-plot url)]
	(oz/view! plot)))

(def stop-server (oz/start-server!))

(plot-example-bar-chart)

```
