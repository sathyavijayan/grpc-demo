(ns translation.main
  (:gen-class)
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [translation.service :as service]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]
            [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount]))

#_(log/set-config!
    {:level :debug
     :ns-whitelist  ["translation.*"]
     :appenders {:println (appenders/println-appender {:stream :auto})}})


(mount/defstate translations-server
  :start (-> service/service
           server/create-server
           server/start)
  :stop (server/stop translations-server))


(defn go []
  (mount/start)
  :ready)


(defn reset []
  (mount/stop)
  (tn/refresh :after 'go))


(comment


  (go)

  ;; -- REPL
  )
