#include <stdio.h>
#include <string.h>
#include <winsock2.h>
#pragma comment(lib, "ws2_32.lib")

/* code testing*/
/*
gcc main.c -o client.exe -lws2_32   # gc compiler turn file to binary
./client.exe               # run the program
*/


char read_operator() {
    char op;

    while (1) { // the 1 means return forever until the loops return or break 
        printf("Enter operator (+, -, *, /): ");
        scanf(" %c", &op);

        if (op == '+' || op == '-' || op == '*' || op == '/') {
            return op;
        }

        printf("Invalid operator. Please try again.\n");
    }
}

int read_number(const char *prompt) { // prompt just let me use same function with diff messeges 
    int value;
    while (1) {
        printf("%s", prompt); // displaying message
        if (scanf("%d", &value) == 1) { // it means only when scanf gets a value then we return it 
            return value;
        }
        printf("Invalid number, try again.\n");
        int c; //since it's either a char ( represented by ASCII ) or EOF (end of file) = -1
        while ((c = getchar()) != '\n' && c != EOF) { } // to clear the input buffer
    }
}


int main() {
        printf("C TCP client starting...\n");

        WSADATA wsa; // Winsock data structure for info 
    if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0) { // Initialize Winsock version for windows
        printf("Failed to initialize Winsock.\n");
        return 1;
    }

    SOCKET sock = socket(AF_INET, SOCK_STREAM, 0); // Create a TCP/IPv4 socket
    if (sock == INVALID_SOCKET) {
        printf("Failed to create socket.\n");
        WSACleanup(); // shutdown Winsock
        return 1;
    }

    struct sockaddr_in server; // hold @ 
    server.sin_family = AF_INET;
    server.sin_port = htons(1026); // convert port num (5000) to the Net byte order    
    server.sin_addr.s_addr = inet_addr("172.20.10.2"); // server IP address

    if (connect(sock, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR) {
        printf("Connection failed.\n");
        closesocket(sock);
        WSACleanup();
        return 1;
    }

    while (1) {
        printf("\n------------------------------------\n");
        printf("New calculation\n");
        printf("------------------------------------\n");

        char choice;
        printf("Do you want to do a calculation? (y/n): ");
        scanf(" %c", &choice);
        
        if (choice == 'n' || choice == 'N') {
            break;  // exit the loop
        }

        int a = read_number("Enter first number: ");
        int b = read_number("Enter second number: "); // directly write the message and the scanf is hidden inside 
        char op = read_operator();
        printf("You entered: %d %c %d\n", a, op, b);
        
        char msg[64];
        int len; // num of bytes (characters) written in msg
        int sent;
        len = snprintf(msg, sizeof(msg), "NUMBER : %d\n", a); 
        if (len < 0 || len >= sizeof(msg)) { // verify formatting
            printf("Failed to format message.\n");
            closesocket(sock);
            WSACleanup();
            return 1;
        }
        sent = send(sock, msg, len, 0);
        if (sent == SOCKET_ERROR) { // verify sending
            printf("Failed to send first number.\n"); 
            closesocket(sock);
            WSACleanup();
            return 1;
        }

        len = snprintf(msg, sizeof(msg), "NUMBER : %d\n", b);
         if (len < 0 || len >= sizeof(msg)) { 
            printf("Failed to format message.\n");
            closesocket(sock);
            WSACleanup();
            return 1;
        }
        sent = send(sock, msg, len, 0);
        if (sent == SOCKET_ERROR) {
            printf("Failed to send second number.\n"); 
            closesocket(sock);
            WSACleanup();
            return 1;
        }

        len = snprintf(msg, sizeof(msg), "OPERATOR : %c\n", op);
         if (len < 0 || len >= sizeof(msg)) { 
            printf("Failed to format message.\n");
            closesocket(sock);
            WSACleanup();
            return 1;
        }
        sent = send(sock, msg, len, 0);
        if (sent == SOCKET_ERROR) { // verify sending
            printf("Failed to send operator.\n"); 
            closesocket(sock);
            WSACleanup();
            return 1;
        }

        char buffer[64];
        printf("Waiting for server response...\n");
        int received = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (received == SOCKET_ERROR) {
            printf("Failed to receive data.\n"); // verify receiving
            closesocket(sock);
            WSACleanup();
            return 1;
        }
        buffer[received] = '\0';  // null-terminate that's why we did sizeof(buffer) - 1 
        printf("Result from server: %s\n", buffer);
   }

    closesocket(sock);
    WSACleanup();
    return 0;

        return 0;
}
