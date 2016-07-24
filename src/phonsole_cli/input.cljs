(ns phonsole-cli.input
  (:require [cljs.nodejs :refer [require process]]
            [clojure.string :as str]
            [cljs.core.async :as async :refer [>! chan close! sliding-buffer]])
  (:require-macros [cljs.core.async.macros :refer (go go-loop)]
                   [taoensso.timbre :refer [debug]]))

(def fs (require "fs"))
(def readline (require "readline"))

(defn stream-to-chan [input]
  "converts a nodejs stream into a channel"
  (debug input)
  (let [stdin-chan (chan (sliding-buffer 10000))
        rl (.createInterface readline (clj->js {:input input}))]
    (.on rl "line" (juxt println
                         #(go (>! stdin-chan %))))
    (.on rl "close" (juxt #(debug "stdin closed")
                          (fn [_] (go (>! stdin-chan false)))))
    stdin-chan))

(defn read-from-stdin []
  (debug "reading from stdin")
  (stream-to-chan (.-stdin process)))
