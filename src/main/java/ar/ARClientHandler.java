package ar;

import shared.CertificadoEleitor;
import shared.NetworkUtils;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class ARClientHandler extends Thread {
    private final SSLSocket socket;
    private final AutoridadeRegisto ar;

    public ARClientHandler(SSLSocket socket, AutoridadeRegisto ar) {
        this.socket = socket;
        this.ar = ar;
    }

    @Override
    public void run() {
        try {
            CertificadoEleitor certificado = (CertificadoEleitor) NetworkUtils.receiveObject(socket);
            CertificadoEleitor certificadoAssinado = ar.registarEleitor(
                    certificado.getIdentificacao(),
                    certificado.getChavePublica()
            );
            NetworkUtils.sendObject(socket, certificadoAssinado);
        } catch (Exception e) {
            System.err.println("Erro no handler: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }
}