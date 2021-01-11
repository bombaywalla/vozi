(ns bombaywalla.vozi
  (:require [clojure.string :as string]
            )
  )

;;; ----------------------------------------------------------------
;;; Functions that work on plots.
;;; They are earlier in the file so that specialized plots can use them later.
;;; ----------------------------------------------------------------

(defn config
  "Add a config `conf` to the plot `p`."
  [p conf]
  (assoc p :config conf))

(defn wrap-layer
  "Wrap a layer around the plot `p`."
  [p]
  {:layer [ p ]})

(defn add-layer
  "Add another layer `l` at the end the list of layers of the existing plot`p`."
  [p l]
  (update p :layer (fnil conj [p]) l))

(defn width
  "Set the `width` of the plot `p`. In pixels."
  [p width]
  (assoc p :width width))

(defn height
  "Set the `height` of the plot `p`. In pixels."
  [p height]
  (assoc p :height height))

(defn add-data
  "Add `data` to the top-level of plot `p`.
  If `data` is a string, it is assumed to be a URL.
  If `data` is a seq of maps, then it is used as is.
  Otherwise `data` is expected to be a seq of pairs.
  The first element of the pair is the x value and the second is the y value."
  [p data]
  (assoc p :data
         (if (string? data)
           {:url data}
           {:values (if (map? (first data)) ;data has named fields
                      data
                      (mapv (fn [[x y]] {"x" x "y" y}) data))})))

(defn title
  "Add `title-text` as a title to the plot `p`.
  `title-opts`, if specified, is a map of title-specific options."
  ([p title-text title-opts]
   (let [title-base {:text title-text}
         title (merge title-base title-opts)]
     (assoc p :title title)))
  ([p title-text] (title p title-text nil)))

(defn subtitle
  "Add a `subtitle` to the plot `p`."
  [p subtitle]
  (assoc-in p [:title :subtitle] subtitle))

(defn x-axis-title
  "Add a `title` to the x-axis of the plot `p`.
  To the first layer, if any. Otherwise to the top-level."
  [p title]
  (assoc-in p
            (if (:layer p) [:layer 0 :encoding :x :title] [:encoding :x :title])
            title))

(defn y-axis-title
  "Add a `title` to the y-axis of the plot `p`.
  To the first layer, if any. Otherwise to the top-level."
  [p title]
  (assoc-in p
            (if (:layer p) [:layer 0 :encoding :y :title] [:encoding :y :title])
            title))

(defn hrule
  "Add a horizontal rule at a y value of `y-val` to the plot `p`."
  [p y-val mark-opts]
  (update-in p [:layer] (fnil conj [])
             {
              :data {:values [{}]}
              :mark (merge {:type "rule"} mark-opts)
              :encoding {:y {:datum y-val}}
              }))
(defn vrule
  "Add a vertical rule at a x value of `x-val` to the plot `p`."
  [p x-val mark-opts]
  (update-in p [:layer] (fnil conj [])
             {
              :data {:values [{}]}
              :mark (merge {:type "rule"} mark-opts)
              :encoding {:x {:datum x-val}}
              }))

(defn drule
  "Add a diagonal rule from [`x`, `y1`] to [`x2`, `y2`]
  to the plot `p`."
  [p x1 y1 x2 y2  mark-opts]
  (update-in p [:layer] (fnil conj [])
             {
              :mark (merge {:type "rule"} mark-opts)
              :encoding {:x {:datum x1}
                         :y {:datum y1}
                         :x2 {:datum x2}
                         :y2 {:datum y2}
                         }
              }))

(defn x-axis-format
  "Specify the x-axis format."
  [p x-format]
  (assoc-in p [:encoding :x :axis :format] x-format))

(defn y-axis-format
  "Specify the y-axis format."
  [p y-format]
  (assoc-in p [:encoding :y :axis :format] y-format))

(defn x-domain
  "Specify the x-axis domain range."
  [p x-dom]
  (assoc-in p [:encoding :x :scale :domain] x-dom))

(defn y-domain
  "Specify the y-axis domain range."
  [p y-dom]
  (assoc-in p [:encoding :y :scale :domain] y-dom))

(defn x-scale-log10
  "Set the x scale to be a log scale."
  [p]
  (assoc-in p [:encoding :x :scale :type] "log"))

(defn y-scale-log10
  "Set the y scale to be a log scale."
  [p]
  (assoc-in p [:encoding :y :scale :type] "log"))

(defn independent-scale
  "Set the specified channels (as keywords) to have an independent scale."
  [p channels]
  (assoc-in p [:resolve :scale] (zipmap channels (repeat "independent"))))

(defn independent-axis
  "Set the specified channels (as keywords) to have an independent axis."
  [p channels]
  (let [allowed-channels #{:x :y}]
    (assert (every? allowed-channels channels) (str "Each channel must be one of: " (string/join ", " allowed-channels) "."))
    (assoc-in p [:resolve :axis] (zipmap channels (repeat "independent")))))

(defn independent-legend
  "Set the specified channels (as keywords) to have an independent legend."
  [p channels]
  (let [allowed-channels #{:color :opacity :shape :size}]
    (assert (every? allowed-channels channels) (str "Each channel must be one of: " (string/join ", " allowed-channels) "."))
    (assoc-in p [:resolve :legend] (zipmap channels (repeat "independent")))))

(defn ^:private base-aggregate-transform
  "Helper function to abstract out the common aspects of
  the aggregate and joinaggregate transforms.
  The `:aggregate-type` must be specified.
  Not a part of the published API."
  [p op field-name as-field {:keys [groupby-field aggregate-type]}]
  (if-let [tr (:transform p)]
    (let [[found acc] (reduce (fn [[found acc] e]
                                (if found
                                  ;; assumes there is only one aggregate transform
                                  ;; so just append the rest if found = true
                                  [found (conj acc e)]
                                  (if (aggregate-type e)
                                    ;; transform and aggregate
                                    (let [new-agg (update e aggregate-type conj
                                                          {:op op :field field-name :as as-field})]
                                      (if (:groupby e)
                                        ;; groupby already present
                                        (if groupby-field
                                          [true (conj acc (update new-agg :groupby conj
                                                                  groupby-field))]
                                          [true (conj acc new-agg)])
                                        ;; groupby absent
                                        (if groupby-field
                                          [true (conj acc (assoc new-agg :groupby [groupby-field]))]
                                          [true (conj acc new-agg)])))
                                    ;; transform and no aggregate, continue to next element
                                    [false (conj acc e)])))
                              ;; init (for reduce)
                              [false []]
                              tr)]
      (if found
        ;; transform and aggregate (that was updated)
        (assoc p :transform acc)
        ;; transform but no aggregate
        (update p :transform conj
                (merge {aggregate-type [ {:op op :field field-name :as as-field} ]}
                       (when groupby-field
                         {:groupby [groupby-field]})))))
    ;; no transform (and hence no aggregate)
    (assoc p :transform [
                         (merge {aggregate-type [ {:op op :field field-name :as as-field} ]}
                                (when groupby-field
                                  {:groupby [groupby-field]}))
                         ])))

(defn aggregate-transform
  "Add an aggregate transform to the plot `p`."
  ([p op field-name as-field {:keys [groupby-field]}]
   (base-aggregate-transform p op field-name as-field (merge {:aggregate-type :aggregate}
                                                             (when groupby-field
                                                               {:groupby-field groupby-field}))))
  ([p op field-name as-field]
   (aggregate-transform p op field-name as-field nil)))

(defn joinaggregate-transform
  "Add a joinaggregate transform to the plot `p`."
  ([p op field-name as-field {:keys [groupby-field]}]
   (base-aggregate-transform p op field-name as-field (merge {:aggregate-type :joinaggregate}
                                                             (when groupby-field
                                                               {:groupby-field groupby-field}))))
  ([p op field-name as-field]
   (joinaggregate-transform p op field-name as-field nil)))

 (defn calculate-transform
  "Add a calculate transform to the plot `p`."
  [p expression as-field-name]
  (update p :transform
          (fnil conj [])
          {:calculate expression
           :as as-field-name}))

 (defn density-transform
  "Add a density transform to the plot `p`."
  [p density-field-name d-opts]
  (update p :transform
          (fnil conj [])
          (merge {:density density-field-name} d-opts)))

(defn filter-transform
  "Add a filter transform to the plot `p`."
  [p pred]
  (update p :transform
          (fnil conj [])
          {:filter pred}))

(defn quantile-transform
  "Add a quantile transform to the plot `p`."
  [p quantile-field-name q-opts]
  (update p :transform
          (fnil conj [])
          (merge {:quantile quantile-field-name}
                 q-opts)))

(defn regression-transform
  "Add a regression transform to the plot `p`."
  [p dep-field indep-field reg-opts]
  (update p :transform
          (fnil conj [])
          (merge {:regression dep-field
                  :on indep-field}
                 reg-opts)))

(defn facet
  "Wrap a facet around the plot `p`."
  [p {:keys [row-field row-type row-opts
             column-field column-type column-opts
             facet-field facet-type facet-opts
             columns]
      :or {row-type "nominal"
           column-type "nominal"
           facet-type "nominal"
           }
      }]
  (assert (or facet-field row-field column-field) "Must specify facet-field, or at least one of row-field or column-field")
  (assert (not (and facet-field (or row-field column-field))) "Cannot specify facet-field and either row-field or column-field")
  (merge {:spec p
          :facet (merge (when row-field
                          {:row (merge {:field row-field
                                        :type row-type}
                                       row-opts)
                           })
                        (when column-field
                          {:column (merge {:field column-field
                                           :type column-type}
                                          column-opts)
                           })
                        (when facet-field
                          (merge {:field facet-field
                                  :type facet-type}
                                 facet-opts))
                        ) }
         (when columns
           {:columns columns})
         ))

;;; ----------------------------------------------------------------
;;; Plot constructors.
;;; ----------------------------------------------------------------

(defn ^:private base-plot
  "Helper function that generates a base Oz plot.
  Not a part of the published API."
  [{:keys [mark-type mark-opts
           x-field x-type x-opts
           y-field y-type y-opts
           x2-field x2-type x2-opts
           y2-field y2-type y2-opts
           color-value color-field color-type color-opts
           theta-field theta-type theta-opts
           shape-field shape-type shape-opts
           facet-field facet-type facet-opts
           encoding-opts
           view-opts
           plot-opts]
    :or {x-type "quantitative"
         y-type "quantitative"
         x2-type "quantitative"
         y2-type "quantitative"
         theta-type "quantitative"
         color-type "nominal"
         shape-type "nominal"
         facet-type "nominal"}
    }]
  (assert mark-type "mark-type must be specified.")
  (merge {:mark (merge {:type mark-type} mark-opts)}
         (when (or x-field y-field x2-field y2-field color-field color-value
                   theta-field shape-field facet-field
                   (not-empty encoding-opts))
           {:encoding (merge {}
                             (when (or x-field (not-empty x-opts))
                               {:x (merge {:field x-field :type x-type} x-opts) })
                             (when (or y-field (not-empty y-opts))
                               {:y (merge {:field y-field :type y-type} y-opts) })
                             (when (or x2-field (not-empty x2-opts))
                               {:x2 (merge {:field x2-field :type x2-type} x2-opts) })
                             (when (or y2-field (not-empty y2-opts))
                               {:y2 (merge {:field y2-field :type y2-type} y2-opts) })
                             (when (or color-value color-field (not-empty color-opts))
                               {:color (merge (if color-value
                                                {:value color-value}
                                                {:field color-field :type color-type})
                                              color-opts) })
                             (when (or theta-field (not-empty theta-opts))
                               {:theta (merge {:field theta-field :type theta-type} theta-opts) })
                             (when (or shape-field (not-empty shape-opts))
                               {:shape (merge {:field shape-field :type shape-type} shape-opts) })
                             (when (or facet-field (not-empty facet-opts))
                               {:facet (merge {:field facet-field :type facet-type} facet-opts) })
                             encoding-opts
                             )})
         (when view-opts {:view view-opts})
         plot-opts))

(defn line-plot
  "Returns a line plot.
  `opts` are specified in the docs for `base-plot`."
  ([opts] (base-plot (merge {:mark-type "line"} opts)))
  ([] (line-plot nil)))

(defn scatter-plot
  "Returns a scatter plot.
  `opts` are specified in the docs for `base-plot`."
  ([opts] (base-plot (merge {:mark-type "point"} opts)))
  ([] (scatter-plot nil)))

(defn bar-plot
  "Returns a bar plot.
  `opts` are specified in the docs for `base-plot`."
  ([opts] (base-plot (merge {:mark-type "bar"} opts)))
  ([] (bar-plot nil)))

(defn area-plot
  "Returns a bar plot.
  `opts` are specified in the docs for `base-plot`."
  ([opts] (base-plot (merge {:mark-type "area"} opts)))
  ([] (bar-plot nil)))

(defn arc-plot
  "Returns a arc plot.
  `opts` are specified in the docs for `base-plot`."
  ([opts] (base-plot (merge {:mark-type "arc"} opts)))
  ([] (arc-plot nil)))

(defn pie-plot
  "Returns a pie plot.
  `opts` are specified in the docs for `base-plot`."
  ([{:keys [inner-radius outer-radius] :as opts}]
   (-> (base-plot (merge {:mark-type "arc"} opts))
       (update-in [:mark]
                  merge
                  (when inner-radius {:innerRadius inner-radius})
                  (when outer-radius {:outerRadius outer-radius}))))
  ([] (pie-plot nil)))

(defn donut-plot
  "Returns a donut plot.
  `opts` are specified in the docs for `base-plot`."
  ([{:keys [inner-radius outer-radius] :as opts}]
   (-> (base-plot (merge {:mark-type "arc"} opts))
       (update-in [:mark]
                  merge
                  (when inner-radius {:innerRadius inner-radius})
                  (when outer-radius {:outerRadius outer-radius}))))
  ([] (donut-plot nil)))

;; TODO: Should x-field be histogram-field? Consistent with density-field
;; TODO: Consider not having a specific max-bins default and using that of Vega-lite.
(defn histogram-plot
  "Returns a histogram plot.
  `opts` are specified in the docs for `base-plot`."
  ([{:keys [x-field max-bins x-opts y-opts]
     :or {x-field "x" max-bins 100}
     :as opts}]
   (-> (bar-plot opts)
       (update-in [:encoding :x] ; TODO: Should the  path be [:encoding :x :bin]
                  ;; TODO: We are forcing type = quantitative.  That is probably good.
                  ;; Can be overridden by x-opts y-opts if needed.
                  merge
                  {:field x-field :type "quantitative" :bin {:maxbins max-bins}}
                  x-opts)
       (update-in [:encoding :y]
                  merge
                  {:type "quantitative" :aggregate "count"}
                  y-opts)))
  ([] (histogram-plot nil)))

(defn density-plot
  "Returns a density plot.
  `opts` are specified in the docs for `base-plot`."
  ([{:keys [density-field density-opts groupby-field groupby-field-type]
     :or {density-field "x" groupby-field-type "nominal"}
     :as opts}]
   (-> (area-plot opts)
       (update-in [:transform]
                  (fnil conj [])
                  (merge {:density density-field}
                         (when groupby-field {:groupby [groupby-field]})
                         density-opts))
       (update-in [:encoding :x]
                  assoc
                  :field "value" :type "quantitative")
       (update-in [:encoding :y]
                  assoc
                  :field "density" :type "quantitative")
       (update-in [:encoding]
                  merge
                  (when groupby-field
                    {:color {:field groupby-field :type groupby-field-type}}))))
  ([] (density-plot nil)))

(defn faceted-density-plot
  "Returns a set of vertically-stacked density plots.
  `opts` are specified in the docs for `base-plot`.
  Requires `faceted-field` (the field name to facet by).
  `groupby-field`, `fold-fields` (a vector) and `fold-as-fields` (a vector) are optional.
  `fold-as-fields` defaults to [\"key\" \"value\"] if not specified.
  `faceted-field-type` and `groupby-field-type` default to \"nominal\" if not specified."
  [{:keys [density-field
           faceted-field faceted-field-type
           groupby-field groupby-field-type
           fold-fields fold-as-fields]
    :or {density-field "x" faceted-field-type "nominal" groupby-field-type "nominal"}
    :as opts}]
  (assert faceted-field "faceted-field must be specified.")
  (-> (area-plot opts)
      (update-in [:encoding :x]
                 assoc
                 :field "value" :type "quantitative")
      (update-in [:encoding :y]
                 assoc
                 :field "density" :type "quantitative")
      (update-in [:encoding]
                 assoc
                 :row
                 {:field faceted-field :type faceted-field-type})
      (update-in [:encoding]
                 merge
                 (when groupby-field
                   {:color {:field groupby-field :type groupby-field-type}}))
      ;; ordering of the transforms is relevant
      (cond-> fold-fields
        (update-in [:transform]
                   (fnil conj [])
                   (merge {:fold fold-fields} (when fold-as-fields {:as fold-as-fields}))))
      (update-in [:transform]
                 (fnil conj [])
                 (merge {:density density-field :groupby [faceted-field]}
                        (when groupby-field
                          {:groupby [faceted-field groupby-field]})))
      ))

(defn qq-plot
  "Returns a QQ plot.
  `opts` are specified in the docs for `base-plot`.
  The `theoretical-distribution` must be one of:
  \"Normal\" (default), \"Uniform\", or \"LogNormal\"."
  ([{:keys [quantile-field quantile-step
            groupby-field
            theoretical-distribution]
     :or {quantile-field "x" quantile-step 0.01
          theoretical-distribution "Normal"}
     :as opts}]
   (let [allowed-theo-dists #{"Normal" "Uniform" "LogNormal"}
         _ (assert (allowed-theo-dists theoretical-distribution)
                   (str "The theoretical distribution must be one of: " (string/join ", " allowed-theo-dists) "."))
         new-opts (assoc opts
                         :x-field "theo" :x-type "quantitative"
                         :y-field "value" :y-type "quantitative")
         base-plot (scatter-plot new-opts)
         transforms [(merge {:quantile quantile-field :step quantile-step}
                            (when groupby-field {:groupby [groupby-field]}))
                     {:calculate (str "quantile" theoretical-distribution "(datum.prob)")
                      :as "theo"}]
         plot (assoc-in base-plot [:transform] transforms)
         ]
     plot))
  ([] (qq-plot nil)))

;;; TODO: Need to allow for parameters of the theoretical distribution.
;;; TODO: Later, provide other theoretical distributions.
(defn qq-line
  "Returns a QQ Line plot.
  The `theoretical-distribution` must be one of:
  \"Normal\" (default), \"Uniform\", or \"LogNormal\"."
  ([{:keys [quantile-field
            groupby-field
            theoretical-distribution]
     :or {quantile-field "x"
          theoretical-distribution "Normal"}
     :as opts}]
   (let [allowed-theo-dists #{"Normal" "Uniform" "LogNormal"}
         _ (assert (allowed-theo-dists theoretical-distribution)
                   (str "The theoretical distribution must be one of: " (string/join ", " allowed-theo-dists) "."))
         new-opts (assoc opts
                         :mark-type "rule"
                         :quantile-field quantile-field
                         :groupby-field groupby-field
                         :x-field "minx" :x-type "quantitative"
                         :y-field "miny" :y-type "quantitative"
                         :x2-field "maxx" :x-type "quantitative"
                         :y2-field "maxy" :y-type "quantitative"
                         )
         plot (-> (base-plot new-opts)
                  (aggregate-transform "q1" "z-score" "q1samp" nil)
                  (aggregate-transform "q3" "z-score" "q3samp" nil)
                  (calculate-transform (str "quantile" theoretical-distribution "(0.25)") "q1theo")
                  (calculate-transform (str "quantile" theoretical-distribution "(0.75)") "q3theo")
                  (calculate-transform (str "quantile" theoretical-distribution "(0.005)") "minx")
                  (calculate-transform (str "quantile" theoretical-distribution "(0.995)") "maxx")
                  (calculate-transform (str "datum.q1samp - "
                                            "((datum.q3samp - datum.q1samp) * "
                                            "(datum.q1theo - datum.minx) / "
                                            "(datum.q3theo - datum.q1theo))")
                                       "miny")
                  (calculate-transform (str "datum.q3samp + "
                                            "((datum.q3samp - datum.q1samp) * "
                                            "(datum.maxx - datum.q3theo) / "
                                            "(datum.q3theo - datum.q1theo))")
                                       "maxy")
                  )
         ]
     plot))
  ([] (qq-line nil)))

(defn zipf-plot
  "Returns a Zipf (log-log scatter) plot.
  `opts` are specified in the docs for `base-plot`."
  ([opts]
   (-> (scatter-plot opts)
       (x-scale-log10)
       (y-scale-log10)))
  ([] (zipf-plot nil)))
