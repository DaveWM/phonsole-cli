(ns phonsole-cli.relay
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]
   [taoensso.timbre :refer [debug]])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan close!)]
   [taoensso.sente  :as sente :refer (cb-success?)]
   [promesa.core :refer [then promise]]
   [cljs.nodejs :as nodejs :refer [require process]]
   ))

(def Moniker (require "moniker"))
(def host-name (.hostname (require "os")))

(defn start!
  ([token ?id host]
   (start! token ?id host true))
  ([token ?id host use-ssl]
   (debug "starting relay")
   (debug "token:" token)
   (let [opts (merge {:type :ws
                      :params {:Authorization token}
                      :protocol (if use-ssl :https :http)
                      :host host
                      :client-id (or ?id (str host-name "-" (.choose Moniker)))}
                     (and ?id {:client-id ?id}))
         {:keys [chsk ch-recv send-fn state]} (sente/make-channel-socket-client!
                                               "/chsk"
                                               opts)
         input-chan (chan)]


     
     (go-loop []
       (when-let [message (<! input-chan)]
         (debug "sending:" message)
         (send-fn [:sender/output {:output message}])
         (recur))
       (sente/chsk-disconnect! chsk))

     (promise (fn [resolve reject]
                (add-watch state :open-watcher
                           #(when (:open? @state)
                              (resolve input-chan))))))))
