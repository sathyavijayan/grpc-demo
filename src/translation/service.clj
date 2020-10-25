(ns translation.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [protojure.pedestal.core :as protojure.pedestal]
            [protojure.pedestal.routes :as proutes]

            [clojure.java.io :as io]))

(defn about-page
  [request]
  (ring-resp/response
    (format (-> "version-info.txt" io/resource slurp)
      (clojure-version)
      (route/url-for ::about-page)
      "sathyavijayan")))

(def common-interceptors [(body-params/body-params) http/html-body])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                                                                            ;;
;;                        ----==| R O U T E S |==----                         ;;
;;                                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def routes #{["/" :get (conj common-interceptors `about-page)]})

(def grpc-routes
  (-> routes)) ;;TODO: add gRPC service here!


(def service {:env :prod
              ::http/routes grpc-routes

              ;; -- PROTOC-GEN-CLOJURE --
              ;; We override the chain-provider with one provided by protojure.protobuf
              ;; and based on the Undertow webserver.  This provides the proper support
              ;; for HTTP/2 trailers, which GRPCs rely on.  A future version of pedestal
              ;; may provide this support, in which case we can go back to using
              ;; chain-providers from pedestal.
              ::http/type protojure.pedestal/config
              ::http/chain-provider protojure.pedestal/provider

              ;;::http/host "localhost"
              ::http/port 8080})
