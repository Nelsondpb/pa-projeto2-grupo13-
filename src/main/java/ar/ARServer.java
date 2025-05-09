package ar;

import shared.CertificadoEleitor;
import shared.NetworkUtils;
import shared.SSLUtils;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ARServer {
    private static final int PORT = 9090;
    private final AutoridadeRegisto ar;

    public ARServer(AutoridadeRegisto ar) {
        this.ar = ar;
    }

    public void start() throws Exception {
        SSLServerSocket serverSocket = (SSLServerSocket)
                SSLUtils.criarContextoSSL().getServerSocketFactory().createServerSocket(PORT);

        System.out.println("🔒 AR Server SSL rodando na porta " + PORT);

        while (true) {
            new ARClientHandler((SSLSocket) serverSocket.accept(), ar).start();
        }
    }
}