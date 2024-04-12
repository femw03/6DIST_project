package origin.project.client;

public class NamingServerClientRunnable {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: provide parameter \"hostname server\" \"port server\"");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        new NamingServerClient(hostname, port);
//        new NamingServerClient("localhost", 8080);
    }

}