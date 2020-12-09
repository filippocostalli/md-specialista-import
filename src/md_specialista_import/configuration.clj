(ns md-specialista-import.configuration
  (:require
    [cprop.core :refer [load-config]]
    [cprop.source :as source]))

(def configuration (load-config))
