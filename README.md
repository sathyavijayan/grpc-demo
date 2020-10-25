# gRPC Demo (translator)

Follow the steps below to create a gRPC service that translates text
using the AWS Translation service.

The overall structure of the code was originally modified from what you get when you run:

```shell
lein new protojure translator
```

This project usesm the following Clojure libraries
- Protojure - set of libraries to implement gRPC in Clojure.
- Pedestal  - set of libraries to implement server-side logic.
- Jetty2    - to implement gRPC client.
- Undertow  - HTTP/2 Server (undertow has support for HTTP/2 trailers which is essential for gRPC implementation).
- Amazonica - Clojure client for AWS Java library.

## Prerequisites

The [Protocol Buffer 'protoc' compiler](https://github.com/protocolbuffers/protobuf/releases)
and [Protojure protoc-plugin](https://github.com/protojure/protoc-plugin/releases) must be installed.

You can confirm these dependencies are installed by either using the `all` Makefile target
or manually running

```shell
protoc --clojure_out=grpc-client,grpc-server:src --proto_path=resources resources/sample.proto
```

## Unary Streaming RPC
Unary RPCs where the client sends a single request to the server and gets a single response back, just like a normal function call.

### Create proto messages and service
Create a new proto file: `resources/translation.proto`.

```clojure
syntax = "proto3";
package translator.proto.translation;

message TranslationRequest {
    string text = 1;
    string target_language = 2;
}

message TranslationResponse {
    string language = 1;
    string src_text = 2;
    string translation = 3;
}

service Translation {
    rpc translate(TranslationRequest) returns (TranslationResponse);
}
```

Run `gmake all` and inspect the files generated in `src/translation/proto`

### Implement the service

Implement the "Translation" service interface.  The compiler generates
a defprotocol (translation/Service, in this case), and it is our job
to define an implementation of every function within it.

```clojure
;; require the server ns
(:require [translator.proto.translation.Translation.server :as translation]
          [protojure.grpc.status :as status]
          [translator.core :as core])

;; implement the service
(deftype TranslationService []
  translation/Service

  ;; GRPC parameters are associated with the request-map as
  ;; :grpc-params, similar to how the pedestal body-param module
  ;; injects other types, like :json-params, :edn-params, etc.
  (translate
    [this {{:keys [text target-language] :as req} :grpc-params :as request}]
    (if-let [_ (core/supported-languages (keyword target-language))]
      {:status 200
       :body (core/translate req)}
      {:status 200
       :body (status/error :invalid-argument
               (format "language not supported - %s" target-language))})))
```

Add translation routes to `grpc-routes`.

```clojure
(proutes/->tablesyntax
            {:rpc-metadata translation/rpc-metadata
             :interceptors common-interceptors
             :callback-context (TranslationService.)})
```

### Try the service using grpc-ui

```clojure
grpcui -plaintext -proto ~/tmp/translation/resources/protos/translation.proto -import-path ~/tmp/translation/resources/protos localhost:8080
```

### Clojure client

```clojure
;; require client ns
(:require [protojure.grpc.client.providers.http2 :as grpc.http2]
          [translator.proto.translation.Translation.client :as translation.client]
          [translator.proto.translation :as translation])

;; create a client
(def client @(grpc.http2/connect {:uri "http://localhost:8080" :idle-timeout -1}))

;; translate !
@(translation.client/translate client
   {:target-language "de"
    :text "Hello, World"})
```

## Server Streaming RPC
A server-streaming RPC is similar to a unary RPC, except that the server returns a stream of messages in response to a client’s request. After sending all its messages, the server’s status details (status code and optional status message) and optional trailing metadata are sent to the client. This completes processing on the server side. The client completes once it has all the server’s messages.

### Add streaming rpc to the proto file

```clojure
message StreamingTranslationsRequest {
    string text = 1;
}

service Translation {
    rpc translations(StreamingTranslationsRequest) returns (stream TranslationResponse);
}
```

run `gmake all`.

### Implement the service

In the service namespace do the following:

```clojure
;; require core.async ns
(:require [clojure.core.async :as async])

;; Add to TranslationService
(translations
    [this {{:keys [text] :as req} :grpc-params :as request}]
    (let [resp-chan (:grpc-out request)]
      (async/thread
        (doseq [lang core/supported-languages]
          (async/>!! resp-chan
            (core/translate {:text text :target-language (name lang)})))
        (async/close! resp-chan))
      {:status 200
       :body resp-chan}))
```

### Clojure Client

```clojure
;; require core.async
(:require [clojure.core.async :as async])

;; create a new core.async channel
(def out-chan (async/chan 1))

;; Go loop to print responses
(async/go-loop []
  (if-let [resp (async/<! out-chan)]
    (do
      (println "Got resp:" resp)
      (recur))
    (println "Exiting go-loop")))


;; Call the gRPC client.
(def stream (translation.client/translations client
              {:text "Hello, World"}
              out-chan))
```

## bidi - Streaming

### Add streaming rpc to the proto file

``` protocol-buffer
service Translation {
    rpc translate_stream(stream TranslationRequest) returns (stream TranslationResponse);
}
```

run `gmake all`

### Implement the service

In service namespace do the following:

``` clojure
(translate_stream
    [this {req-chan :grpc-params :as request}]
    (let [resp-chan (:grpc-out request)]
      (async/thread
        (loop [timeout-chan (async/timeout 10000)]
          (let [[v c] (async/alts!! [timeout-chan req-chan])]
            (if (or (= c timeout-chan) (nil? v))
              (log/info "exiting - timed out")
              (do
                (async/>!! resp-chan (core/translate v))
                (recur (async/timeout 10000))))))
        (async/close! resp-chan))
      {:status 200
       :body resp-chan}))
```


### Clojure client

``` clojure
  ;; input and output channels
  (def in-chan (async/chan 1))
  (def out-chan (async/chan 1))

  ;; go loop to print responses
  (async/go-loop []
    (if-let [resp (async/<! out-chan)]
      (do
        (println "Got resp:" resp)
        (recur))
      (println "Exiting go-loop")))

  ;; make the call
  (let [client @(grpc.http2/connect {:uri "http://localhost:8080" :idle-timeout -1})]
    (translation.client/translate_stream
      client
      in-chan
      out-chan))

  ;; send requests
  (async/go
    (async/>! in-chan {:text "Hello" :target-language "ta"}))

```

## License
This project is licensed under the Apache License 2.0.
