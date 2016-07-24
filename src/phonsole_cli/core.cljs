(ns phonsole-cli.core
  (:require [cljs.nodejs :as nodejs :refer [require process]]
            [cljs.core.async :refer (<! >! put! close!)]
            [clojure.string :as str]
            [phonsole-cli.auth :as auth]
            [phonsole-cli.relay :as relay]
            [phonsole-cli.input :as input]
            [promesa.core :refer [then promise]]
            [taoensso.timbre :as timbre])
  (:require-macros [cljs.core.async.macros :refer (go go-loop)]
                   [taoensso.timbre :as timbre :refer [debug]]))

(nodejs/enable-util-print!)

(def fs (require "fs"))

(def commandLineArgs (require "command-line-args"))
(def cl-options (clj->js [{:name "verbose" :alias "v"}
                          {:name "id"}]))
(def args (js->clj (commandLineArgs cl-options) :keywordize-keys true))

(timbre/set-level! (if (:verbose args)
                     :debug
                     :error))


(defn -main []
  (debug "Starting Phonsole CLI")
  (debug "args:" args)
  (let [creds-path (str (-> process .-env .-HOME) "/phonsole-credentials")
        input-chan (input/read-from-stdin)
        ?host (or (-> (.-env process)
                      (aget "PHONSOLE_SERVER"))
                  "phonsole-server.herokuapp.com")]
    (debug "host:" ?host)
    (-> (promise (fn [resolve reject] (.readFile fs creds-path "utf8" (fn [err token]
                                                                        (resolve token)))))
        (then (fn [?token]
                (if ?token
                  ?token
                  (-> (auth/get-token)
                      (then (fn [new-token]
                              (.writeFile fs creds-path new-token)
                              new-token))))))
        (then #(relay/start! % (:id args) ?host))
        (then (fn [server-chan]
                (debug "server connection complete")
                (go-loop []
                  (when-let [input-line (<! input-chan)]
                    (debug "input-line:" input-line)
                    (>! server-chan input-line)
                    (recur))
                  (close! input-chan)
                  (close! server-chan)
                  (debug "finished")))))))

(set! *main-cli-fn* -main)
