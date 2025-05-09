package shared;

import java.security.*;
import java.util.Base64;
import java.util.UUID;

public class CertificadoEleitor {
    private final String numeroSerie;
    private final String identificacao;
    private final PublicKey chavePublica;
    private byte[] assinatura;
    private final String algoritmoAssinatura = "SHA256withRSA";

    public CertificadoEleitor(String identificacao, PublicKey chavePublica) {
        this.numeroSerie = UUID.randomUUID().toString();
        this.identificacao = identificacao;
        this.chavePublica = chavePublica;
    }

    public void assinar(PrivateKey chavePrivadaAR)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(algoritmoAssinatura);
        signature.initSign(chavePrivadaAR);
        signature.update(this.toByteArray());
        this.assinatura = signature.sign();
    }

    public boolean verificarAssinatura(PublicKey chavePublicaAR)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (assinatura == null) return false;

        Signature signature = Signature.getInstance(algoritmoAssinatura);
        signature.initVerify(chavePublicaAR);
        signature.update(this.toByteArray());
        return signature.verify(assinatura);
    }

    private byte[] toByteArray() {
        String dados = numeroSerie + identificacao + chavePublicaToString();
        return dados.getBytes();
    }

    private String chavePublicaToString() {
        byte[] encodedKey = chavePublica.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    // Getters
    public String getNumeroSerie() { return numeroSerie; }
    public String getIdentificacao() { return identificacao; }
    public PublicKey getChavePublica() { return chavePublica; }
    public byte[] getAssinatura() { return assinatura; }

    public String toPemFormat() {
        return "-----BEGIN CERTIFICATE-----\n" +
                "Numero Serie: " + numeroSerie + "\n" +
                "Identificacao: " + identificacao + "\n" +
                "Chave Publica: " + chavePublicaToString() + "\n" +
                (assinatura != null ? "Assinatura: " + Base64.getEncoder().encodeToString(assinatura) + "\n" : "") +
                "-----END CERTIFICATE-----";
    }
}