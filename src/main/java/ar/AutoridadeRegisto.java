package ar;

import shared.CertificadoEleitor;
import shared.exceptions.CertificadoInvalidoException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class AutoridadeRegisto {
    private final PrivateKey chavePrivadaAR;
    private final PublicKey chavePublicaAR;
    private final Map<String, CertificadoEleitor> eleitoresRegistados;

    public AutoridadeRegisto(PrivateKey chavePrivadaAR, PublicKey chavePublicaAR) {
        this.chavePrivadaAR = chavePrivadaAR;
        this.chavePublicaAR = chavePublicaAR;
        this.eleitoresRegistados = new HashMap<>();
    }

    public Map<String, CertificadoEleitor> getEleitoresRegistados() {
        return Collections.unmodifiableMap(eleitoresRegistados);
    }

    public CertificadoEleitor registarEleitor(String infoEleitor, PublicKey chavePublicaEleitor)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        CertificadoEleitor certificado = new CertificadoEleitor(infoEleitor, chavePublicaEleitor);
        certificado.assinar(chavePrivadaAR);
        eleitoresRegistados.put(certificado.getNumeroSerie(), certificado);
        return certificado;
    }

    public boolean validarCertificado(CertificadoEleitor certificado)
            throws CertificadoInvalidoException {
        try {
            if (!certificado.verificarAssinatura(chavePublicaAR)) {
                throw new CertificadoInvalidoException("Assinatura do certificado inválida");
            }

            if (!eleitoresRegistados.containsKey(certificado.getNumeroSerie())) {
                throw new CertificadoInvalidoException("Certificado não emitido por esta AR ou revogado");
            }

            return true;
        } catch (Exception e) {
            throw new CertificadoInvalidoException("Erro na validação do certificado: " + e.getMessage());
        }
    }

    public PublicKey getChavePublicaAR() {
        return chavePublicaAR;
    }
}