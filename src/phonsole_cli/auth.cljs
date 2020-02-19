(ns phonsole-cli.auth
  (:require [promesa.core :as promise :refer [promise then rejected]]
            [cljs.nodejs :refer [require process]]
            [hiccups.runtime :as hiccupsrt]
            [promesa.core :refer [then]])
  (:require-macros [hiccups.core :as hiccups :refer [html]]
                   [phonsole-cli.macros :refer [load-hiccup-script js-promise]]
                   [taoensso.timbre :as timbre :refer [debug]]))

(def Express (js/require "express"))
(def app (Express.))
(def open (js/require "open"))
(def fs (js/require "fs"))
(def fetch (js/require "request-promise"))

(def port 3000)

(def server (atom nil))

(def creds-path (str (-> process .-env .-HOME) "/phonsole-credentials"))

(defn log-in [server-url]
  (promise (fn [resolve reject]
             (debug "Starting Express")

             (.get app "/" (fn [req res]
                             (.send res (html [:html               
                                               [:head
                                                [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.0/jquery.min.js"}]
                                                [:script {:src "//cdn.auth0.com/js/lock-9.1.min.js"}]
                                                [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]]
                                               [:body (load-hiccup-script "./src/js/auth.js")]]))))

             (.get app "/token/:token" (fn [req res]
                                         (.send res "Authentication complete, please close this tab")
                                         (when @server
                                           (.close @server))
                                         (resolve (-> (.-params req)
                                                      (aget "token")))))
             
             (reset! server (.listen app port (fn []
                                               (debug "Express started")
                                                (open (str "http://localhost:" port))))))))

(defn refresh-token [server-url]
  (-> (log-in server-url)
      (then (fn [new-token]
              (.writeFile fs creds-path new-token (constantly nil))
              new-token))))

(defn check-logged-in [server-url token]
  (if token
    (-> (js-promise (fetch #js {:uri (str server-url "/auth")
                                :simple false
                                :resolveWithFullResponse true
                                :headers #js {:Authorization (str "Bearer " token)}}))
        (then (fn [response]
                (debug "auth response" response)
                (when (= 401 (.-statusCode response))              
                  (throw (js/Error. "Authentication failed")))
                token)))
    (rejected nil)))

(defn get-token [server-url]
  (-> (promise (fn [resolve reject] (.readFile fs creds-path "utf8" (fn [err token]
                                                                      (resolve token)))))
      (then (partial check-logged-in server-url))
      (promise/catch #(refresh-token server-url))))
