(ns phonsole-cli.relay
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]
   [taoensso.timbre :refer [debug]])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan close!)]
   [taoensso.sente  :as sente :refer (cb-success?)]
   [promesa.core :refer [then promise]]
   ))

(defn start! [token ?id]
  (debug "starting relay")
  (debug "token:" token)
  (let [opts (merge {:type :ws
                     :params {:Authorization token}
                     :protocol :http
                     :host "localhost:8080"}
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

    (promise (fn [resolve]
               (add-watch state :open-watcher
                          #(when (:open? @state)
                             (resolve input-chan)))))))
