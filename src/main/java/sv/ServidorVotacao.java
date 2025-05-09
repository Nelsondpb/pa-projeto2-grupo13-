package sv;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.exceptions.AutenticacaoFalhouException;
import java.security.PublicKey;
import java.util.UUID;

public class ServidorVotacao {
    private final AutoridadeRegisto ar;
    private final PublicKey chavePublicaAA;
    private final TokenService tokenService;

    public ServidorVotacao(AutoridadeRegisto ar, PublicKey chavePublicaAA) {
        this.ar = ar;
        this.chavePublicaAA = chavePublicaAA;
        this.tokenService = new TokenService();
    }

    public UUID autenticarEleitor(CertificadoEleitor certificado) throws AutenticacaoFalhouException {
        try {
            if (!ar.validarCertificado(certificado)) {
                throw new AutenticacaoFalhouException("Certificado inválido");
            }
            return tokenService.emitirToken();
        } catch (Exception e) {
            throw new AutenticacaoFalhouException(e.getMessage());
        }
    }

    public boolean validarToken(UUID token) {
        return tokenService.validarToken(token);
    }
}