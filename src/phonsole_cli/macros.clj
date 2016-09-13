(ns phonsole-cli.macros
  (:require [promesa.core :refer [promise]]))

(defmacro load-hiccup-script [path]
  "loads a javascript file into a hiccup script tag"
  [:script (slurp path)])

(defmacro js-promise [body]
  `(promise (fn [resolve# reject#]
              (-> ~body
                  (.then resolve#)
                  (.catch reject#)))))
