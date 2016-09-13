(defproject phonsole-cli "0.0.1"
  :description "CLI for the phonsole application"

  :min-lein-version "2.5.3"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [hiccups "0.3.0"]
                 [com.taoensso/sente "1.11.0-alpha4"]
                 [funcool/promesa "1.4.0"]
                 [com.taoensso/timbre "4.7.0"]]

  :plugins [[lein-cljsbuild "1.1.1"]]

  :source-paths ["src"]

  :clean-targets ["dist/app.js"
                  "target"]

  :cljsbuild {
              :builds [{:id "prod"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "dist/app.js"
                                   :output-dir "target/server_prod"
                                   :target :nodejs
                                   :optimizations :simple}}]})
