import java.io.BufferedReader;
import java.io.IOException; // handles input/outputs execptions 
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;// create and listenng 
import java.net.Socket;// represent a client socket for communication between client and server 

public class CalculationServer {
    public static void main(String[] args) {
        System.out.println("TCP claclulation server is starting ...");
        /*
         * using try catch to aprevent the program
         * from cashing and handeling errors
         * try with resources do cleanup part
         */
        try (ServerSocket serverSocket = new ServerSocket(1026)) { // this is try with resources !! so it's include
                                                                   // closing automatic

            System.out.println("server started running on the port 1026 waiting for clients");

            serverSocket.setReuseAddress(true); // immediate restart of the server no port already in use to avoid
                                                // TIME_WAIT

            while (true) { // infinite for multiple client

                // waiting for clients to connect
                Socket clientSocket = serverSocket.accept();

                System.out.println("client is connected! IP :" + clientSocket.getInetAddress().getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(clientSocket);

                /*
                 * create a real new thread
                 * this runs clients in parallel
                 * this thread will handle the client separately
                 */

                new Thread(clientSock).start();
            }

        } catch (IOException e) {

            System.err.println("client connection error :" + e.getMessage()); // prints message to the server console
                                                                              // only so the admin can see it me in this
                                                                              // case for debugging
            e.printStackTrace();
            return;
        }

    }

    // add close clientsocket here inside client handler
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;

        }

        public void run() //
        {
            PrintWriter out = null;
            BufferedReader in = null; // has only readLine()
            try { // normal try catch handles only erros
                  // get the outputstream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // get the inputstream ofclient
                in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));

                clientSocket.setSoTimeout(300_000); // 5 min timeout so the server thread will wait up to 5 min for
                                                    // client input before throwing a socketTimeException

                // reading client in a loop
                while (true) {

                    // server read from client i should add timeout if the client just stop sending
                    // data th thread will just hang there
                    String firstLine = in.readLine(); // if the client diconnected

                    // this part of network safety
                    // client close the app,lost internet ,client crashed
                    if (firstLine == null) {
                        System.out.println("[SERVER ^_^] Client disconnected : "
                                + clientSocket.getInetAddress().getHostAddress());
                        return;
                    }
                    String secondLine = in.readLine();
                    if (secondLine == null)
                        return;
                    String operationLine = in.readLine();
                    if (operationLine == null)
                        return;

                    try {
                        // extract + convert the string wether the number real number or integer
                        double number1 = parseNumber(firstLine); // parsenumber throws null
                        double number2 = parseNumber(secondLine);
                        String operator = parseOperator(operationLine);

                        double result = calculate(number1, number2, operator);

                        out.println("^_^ Result: " + result);
                        System.out.println("[SERVER ^_^] Sent result to "
                                + clientSocket.getInetAddress().getHostAddress()
                                + " : " + result);
                    } catch (Exception e) {
                        out.println(">_< Error: " + e.getMessage() + " Please check your input!");
                        System.err.println("[SERVER >_<] Client "
                                + clientSocket.getInetAddress().getHostAddress()
                                + " caused error: " + e.getMessage());// this code or this out.println("Error invalid
                                                                      // number format (we allowing real numbers !) ");
                        continue;

                    }
                }
            } catch (IOException e) {
                System.err.println("[SERVER T_T] Connection error " + e.getMessage());
            } finally { // cleaning shutdown
                try {
                    if (out != null)
                        out.close(); // closing the output free memory
                    if (in != null)
                        in.close(); // stop reading from client
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close(); // clientsocket exist and not closed we close it
                        System.out.println("[SERVER -_-] Closed connection for client: "
                                + clientSocket.getInetAddress().getHostAddress());
                    }
                } catch (IOException e) {
                    System.err.println("[SERVER >_<] Error while closing client: " + e.getMessage()); // closing fail
                                                                                                      // showing it to
                                                                                                      // the server
                                                                                                      // consol
                }
            }
        }
        // format validation

        private double parseNumber(String line) throws Exception {
            if (line == null) {
                throw new Exception("client disconnected");
            }
            if (line.trim().isEmpty()) {
                throw new Exception("Number is empty ");
            }
            if (!line.contains(":")) {
                throw new Exception("invalid format use : NUMBER: value");
            }
            String value = line.split(":")[1].trim();
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new Exception("invalid number format");
            }
        }

        private String parseOperator(String line) throws Exception {
            if (line == null) {
                throw new Exception("client disconnected");
            }
            if (line.trim().isEmpty()) {
                throw new Exception("operator is empty ");
            }
            if (!line.contains(":")) {
                throw new Exception("invalid format use OPERATOR : + ");
            }
            String operator = line.split(":")[1].trim();
            // operator validation
            if (!operator.equals("+") &&
                    !operator.equals("-") &&
                    !operator.equals("*") &&
                    !operator.equals("/")) {

                throw new Exception("invalid operator use + - * / only ");
            }
            return operator;

        }

        private double calculate(double n1, double n2, String op) throws Exception {
            switch (op) {
                case "+":
                    return n1 + n2;
                case "-":
                    return n1 - n2;
                case "*":
                    return n1 * n2;
                case "/":
                    if (n2 == 0) {
                        throw new Exception("Division by zero ");

                    }
                    return n1 / n2;
                default:
                    throw new Exception("unknown operator");

            }
        }
    }

}
