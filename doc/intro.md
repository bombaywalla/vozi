# Introduction to vozi

`vozi` is just a thin wrapper around `oz` (which, in turn, is a
wrapper around `vega` and `vaga-lite`).

The idea was to create an API similar to `ggplot2` that would allow for
the incremental creation of a plot.

It is not clear that `vozi` provides enough benefit at the moment,
since one still has to understand the semantics of `vaga-lite` to use
it effectively.  Work in progress. All APIs subject to change.

## Example usage

Here are some examples of `vozi` usage.

```clojure

(require '[oz.core :as oz])
(require '[bombaywalla.vozi :as vozi])

(defn vega-dataset
  "Returns the full Vega dataset URL, given the `short-name`."
  [short-name]
  (str "https://raw.githubusercontent.com/vega/vega-datasets/master/data/" short-name))

(defn bar-chart
  "Create the example bar chart
  from https://vega.github.io/vega-lite/examples/bar.html"
  []
  (-> (vozi/bar-plot {:x-field "date"
                      :x-type "ordinal"
                      :x-opts {:timeUnit "month"}
                      :y-field "precipitation"
                      :y-opts {:aggregate "mean"}})
      (vozi/add-data (vega-dataset "seattle-weather.csv"))
      ))

(defn normalized-stacked-bar-chart
  "Create the example normalized stacked bar chart
  from https://vega.github.io/vega-lite/examples/stacked_bar_normalize.html"
  []
  (-> (vozi/bar-plot {:x-field "age"
                      :x-type "ordinal"
                      :y-field "people"
                      :y-opts {:aggregate "sum"
                               :stack "normalize"}
                      :color-field "gender"
                      :color-opts {:scale {:range ["#675193" "#ca8861"]}}
                      :plot-opts {:width {:step 17}}})
      (vozi/filter-transform "datum.year == 2000")
      (vozi/calculate-transform "datum.sex == 2 ? 'Female' : 'Male'" "gender")
      (vozi/y-axis-title "population")
      (vozi/add-data (vega-dataset "population.json"))
      ))

(defn histogram-chart
  "Create the example histogram chart
  from https://vega.github.io/vega-lite/examples/histogram.html"
  []
  (-> (vozi/histogram-plot {:x-field "IMDB Rating" :max-bins nil})
      (vozi/add-data (vega-dataset "movies.json"))
      ))

(defn density-chart
  "Create the example density chart
  from https://vega.github.io/vega-lite/examples/area_density.html"
  []
  (-> (vozi/density-plot {:density-field "IMDB Rating" :density-opts {:bandwidth 0.3}})
      (vozi/x-axis-title "IMDB Rating")
      (vozi/width 400)
      (vozi/height 100)
      (vozi/add-data (vega-dataset "movies.json"))
      ))

(defn colored-scatter-chart
  "Create the example colored scatter chart
  from https://vega.github.io/vega-lite/examples/point_color_with_shape.html"
  []
  (-> (vozi/scatter-plot {:x-field "Flipper Length (mm)"
                          :x-opts {:scale {:zero false}}
                          :y-field "Body Mass (g)"
                          :y-opts {:scale {:zero false}}
                          :color-field "Species"
                          :shape-field "Species"
                          })
      (vozi/add-data (vega-dataset "penguins.json"))
      ))

(defn colored-line-chart
  "Create the example colored line chart
  from https://vega.github.io/vega-lite/examples/line_color.html"
  []
  (-> (vozi/line-plot {:x-field "date"
                       :x-type "temporal"
                       :y-field "price"
                       :color-field "symbol"
                       })
      (vozi/add-data (vega-dataset "stocks.csv"))
      ))

(defn faceted-density-chart
  "Create the example faceted density chart
  from https://vega.github.io/vega-lite/examples/area_density_facet.html"
  []
  (-> (-> (vozi/area-plot {:x-field "value"
                           :y-field "density"
                           :y-opts {:stack "zero"}
                           })
          (vozi/density-transform "Body Mass (g)" {:groupby ["Species"]
                                                   :extent [2500 6500]})
          (vozi/x-axis-title "Body Mass (g)")
          (vozi/width 400)
          (vozi/height 80)
          )
      (vozi/facet {:row-field "Species"})
      (vozi/title "Distribution of Body Mass of Penguins")
      (vozi/add-data (vega-dataset "penguins.json"))
      ))

(comment
  (require '[oz.core :as oz])
  (require '[bombaywalla.vozi :as vozi])

  (def stop-server (oz/start-server!))

  (def bar-chart (bar-chart))
  (oz/view! bar-chart)

  (def normalized-stacked-bar-chart (normalized-stacked-bar-chart))
  (oz/view! normalized-stacked-bar-chart)

  (def histogram-chart (histogram-chart))
  (oz/view! histogram-chart)

  (def density-chart (density-chart))
  (oz/view! density-chart)

  (def colored-scatter-chart (colored-scatter-chart))
  (oz/view! colored-scatter-chart)

  (def colored-line-chart (colored-line-chart))
  (oz/view! colored-line-chart)

  (def faceted-density-chart (faceted-density-chart))
  (oz/view! faceted-density-chart)


  (stop-server)
  )
```
