(ns user
  "The user namespace for development."
  ;; Ideally, require nothing here (see jit macro)
  )

;; Stolen from @plexus, with some mods.

(defmacro jit
  "Just in time loading of dependencies.
  Enables fast time to get a repl prompt.
  Also, makes sure you don't do much in this file,
  other than define some functions/macros."
  [sym]
  `(requiring-resolve '~sym))

(comment
  (require '[clojure.spec.alpha :as s])
  (require '[cambium.core :as log])
  (require '[bombaywalla.vozi.specs :as vozis])
  (require '[bombaywalla.vozi :as vozi])
  )
