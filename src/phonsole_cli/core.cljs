(ns phonsole-cli.core
  (:require [cljs.nodejs :as nodejs :refer [require process]]
            [cljs.core.async :refer (<! >! put! close!)]
            [clojure.string :as str]
            [phonsole-cli.auth :as auth]
            [phonsole-cli.relay :as relay]
            [phonsole-cli.input :as input]
            [promesa.core :as promise :refer [then promise]]
            [taoensso.timbre :as timbre])
  (:require-macros [cljs.core.async.macros :refer (go go-loop)]
                   [taoensso.timbre :as timbre :refer [debug]]))

(nodejs/enable-util-print!)

(def commandLineArgs (js/require "command-line-args"))
(def cl-options (clj->js [{:name "verbose" :alias "v"}
                          {:name "id"}
                          {:name "no-ssl"}]))
(def args (js->clj (commandLineArgs cl-options) :keywordize-keys true))

(timbre/set-level! (if (:verbose args)
                     :debug
                     :error))


(defn -main []
  (debug "Starting Phonsole CLI")
  (debug "args:" args)

  (.on process "SIGINT" (fn []
                          (debug "SIGINT")
                          (.exit process)))

  (let [input-chan (input/read-from-stdin)
        server-domain (or (-> (.-env process)
                              (aget "PHONSOLE_SERVER"))
                          "phonsole-server.herokuapp.com")
        server-url (str "http://" server-domain)
        use-ssl (not (:no-ssl args))]
    (debug "server url:" server-url)
    (if use-ssl
      (debug "Using SSL")
      (debug "** SSL disabled **"))
    (-> (auth/get-token server-url)
        (then #(relay/start! % (:id args) server-domain (not (:no-ssl args))))
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
