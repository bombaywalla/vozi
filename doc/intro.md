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
(require '[bombaywalla/vozi :as viz])

(defn sample-correlation-normal
  "Returns the `sample-size` sample correlation for two Gaussians
  with a population correlation of `rho`."
  [sample-size rho]
  ;;; to be filled in
  )

(defn plot-correlation
  "Plot the sample correlations of a normal distribution at different sample sizes,
  where the population correlation is zero."
  ([{:keys [num-observations low-sample-size high-sample-size population-correlation
            width height]
     :or {num-observations 10000
          low-sample-size 20
          high-sample-size 1000
          population-correlation 0.0
          width 400
          height 200}}]
   (let [samps-low (repeatedly num-observations
                               #(sample-correlation-normal low-sample-size
                                                           population-correlation))
         samps-high (repeatedly num-observations
                                #(sample-correlation-normal high-sample-size
                                                            population-correlation))
         data (into []
                    (mapcat (fn [n lo hi] [{:n n :corr lo :sample-size low-sample-size}
                                           {:n n :corr hi :sample-size high-sample-size}])
                            (range)
                            samps-low
                            samps-high))
         bar-plot (-> (viz/bar-plot {:x-field "n"
                                     :y-field "corr"
                                     :facet-field "sample-size"
                                     :facet-opts {:columns 2}
                                     })
                      (viz/title "Sample correlations between 2 independent Normals by sample size")
                      (viz/subtitle "Sample correlations converge quickly")
                      (viz/x-axis-title "simulation number")
                      (viz/y-axis-title "sample correlation")
                      (viz/width width)
                      (viz/height height)
                      )
         plot (viz/add-data bar-plot data)
         ]
     (oz/view! plot)))
  ([] (plot-correlation nil)))
```

