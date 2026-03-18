# WAKFULIB

**WakfuLib** is a powerful Java-based framework designed to simplify the development of tools, proxies, and custom clients for the Wakfu game. It provides a comprehensive set of abstractions for handling network protocols, game data models, and encryption.

## Features

-   **Version-Aware Protocol Handling:** Support for multiple game versions with easy initialization.
-   **Event-Driven Networking:** Register custom handlers for network messages and internal events using an intuitive annotation-based system.
-   **Comprehensive Data Models:** Pre-built beans representing game concepts like `Direction8`, items, and more.
-   **Built-in Encryption:** Managers for RSA and custom encryption used in Wakfu's client-server communication.
-   **Flexible Connection Builders:** Easily setup client or server connections with SSL support, custom logging, and session management.
-   **Utilities:** A rich set of helper classes for buffer manipulation, reflection, and mathematical operations.

## Installation

### Prerequisites

-   Java 17 or higher
-   Maven

### Building from Source

1.  Place your `.protos` files into the `src/main/protobuf` directory (if applicable).
2.  Compile the project using Maven:

```bash
mvn clean install
```

## Usage

### Initializing the Library

Before using any protocol-dependent features, initialize the library with the target game version:

```java
WakfuLib wakfuLib = new WakfuLib();
wakfuLib.init(Version.v1_68_0);
```

By default, the library looks for game resources in `%LOCALAPPDATA%\Ankama\zaap\wakfu\contents`. You can provide a custom path via the constructor:

```java
WakfuLib wakfuLib = new WakfuLib(new File("path/to/wakfu/contents"));
```

### Setting up a Client Connection

```java
EventManager eventManager = new EventManager.EventManagerBuilder()
    .register(new MyMessageHandler())
    .build();

new WakfuClientConnectionBuilder(eventManager)
    .bind("auth.wakfu.com", 5558)
    .withSSL(true)
    .start();
```

### Setting up a Server Listener

```java
WakfuLib.server(5558, new MyServerHandler(), true);
```

## Project Structure

-   `wakfulib`: Root package containing the main entry point.
-   `wakfulib.logic`: Core networking, session management, and packet handling.
-   `wakfulib.logic.event`: Event-driven message dispatching system.
-   `wakfulib.beans`: Data structures and game models.
-   `wakfulib.crypto`: Encryption and security services.
-   `wakfulib.utils`: Generic helper and utility classes.

## Acknowledgments

-   Built on top of [Netty](https://netty.io/) for high-performance networking.
-   UI components enhanced with [FlatLaf](https://www.formdev.com/flatlaf/).
