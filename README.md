# CCS System - README

## Project Description
This project is part of an academic assignment at the university. It implements the CCS (Client-Server Communication System) in Java, enabling communication between a client and a server.

## Repository Contents

- **CCS.java** - Server source code
- **CCSClient.java** - Client source code
- **CCS.jar** - Compiled JAR file for the server
- **CCSClient.jar** - Compiled JAR file for the client
- **run_server.bat** - Script to run the server
- **run_client.bat** - Script to run the client

## System Requirements
- Java Development Kit (JDK) version 8 or later
- Windows operating system (for BAT scripts) or a compatible platform supporting JAR execution

## Execution Instructions

### Running the Server
1. Ensure you have Java installed.
2. Open a terminal or command prompt in the project directory.
3. Start the server using the command:
   ```sh
   java -jar CCS.jar
   ```
   Or, if using Windows, run the script:
   ```sh
   run_server.bat
   ```

### Running the Client
1. Open a terminal or command prompt in the project directory.
2. Start the client using the command:
   ```sh
   java -jar CCSClient.jar
   ```
   Or, if using Windows, run the script:
   ```sh
   run_client.bat
   ```

## Code Structure
- **CCS.java**: Server implementation handling client connections.
- **CCSClient.java**: Client implementation connecting to the server.

## Notes
- This project was created as part of an academic assignment.
- It may require modifications to adapt to specific environments.
- If any issues arise, verify Java installation and network configuration.

## Author
Created by bddnn.

## License
This project is licensed under the MIT License.

