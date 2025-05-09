package eleitor;

import shared.CertificadoEleitor;
import shared.NetworkUtils;
import shared.SSLUtils;
import javax.net.ssl.SSLSocket;
import java.security.PublicKey;

public class ARClient {
    private static final String AR_HOST = "localhost";
    private static final int AR_PORT = 9090;

    public static CertificadoEleitor enviarCertificadoParaAR(
            String identificacao, PublicKey chavePublica) throws Exception {

        SSLSocket socket = (SSLSocket) SSLUtils.criarContextoSSL()
                .getSocketFactory().createSocket(AR_HOST, AR_PORT);

        try {
            CertificadoEleitor certificado = new CertificadoEleitor(identificacao, chavePublica);
            NetworkUtils.sendObject(socket, certificado);
            return (CertificadoEleitor) NetworkUtils.receiveObject(socket);
        } finally {
            socket.close();
        }
    }
}