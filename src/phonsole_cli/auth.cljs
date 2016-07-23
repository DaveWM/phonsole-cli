(ns phonsole-cli.auth
  (:require [promesa.core :refer [promise then]]
            [cljs.nodejs :refer [require]]
            [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups :refer [html]]
                   [phonsole-cli.macros :refer [load-hiccup-script]]))

(def Express (require "express"))
(def app (Express.))
(def open (require "open"))

(def port 3000)


(defn get-token []
  (promise (fn [resolve reject]
             (println "Starting Express")

             (.get app "/" (fn [req res]
                             (.send res (html [:html               
                                               [:head
                                                [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.0/jquery.min.js"}]
                                                [:script {:src "//cdn.auth0.com/js/lock-9.1.min.js"}]
                                                [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]]
                                               [:body (load-hiccup-script "./src/js/auth.js")]]))))

             (.get app "/token/:token" (fn [req res]
                                         (.send res "Authentication complete, please close this tab")
                                         (resolve (-> (.-params req)
                                                      (aget "token")))))
             
             (.listen app port (fn []
                                 (println "Express started")
                                 (open (str "http://localhost:" port)))))))
