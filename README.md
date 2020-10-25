# gRPC Demo

Follow the steps below to create a gRPC service that translates text
using the AWS Translation service.

The overall structure of the code was originally modified from what you get when you run:

```
lein new protojure translation
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

```
protoc --clojure_out=grpc-client,grpc-server:src --proto_path=resources resources/sample.proto
```

## License
This project is licensed under the Apache License 2.0.
