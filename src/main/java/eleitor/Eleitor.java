package eleitor;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.exceptions.AutenticacaoFalhouException;
import shared.exceptions.VotoInvalidoException;
import sv.ServidorVotacao;
import ue.UrnaEletronica;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.util.UUID;


public class Eleitor {
    private final String identificacao;
    private final KeyPair parChaves;
    private CertificadoEleitor certificado;
    private UUID tokenVoto;


    public Eleitor(String identificacao) throws NoSuchAlgorithmException {
        this.identificacao = identificacao;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.parChaves = keyGen.generateKeyPair();
    }


    public CertificadoEleitor registarNaAR() throws Exception {
        this.certificado = ARClient.enviarCertificadoParaAR(identificacao, parChaves.getPublic());
        return certificado;
    }


    public void autenticarNoSV(ServidorVotacao servidorVotacao) throws AutenticacaoFalhouException {
        if (certificado == null) {
            throw new AutenticacaoFalhouException("Eleitor não possui certificado válido");
        }

        this.tokenVoto = servidorVotacao.autenticarEleitor(certificado);
    }

    public void votar(String opcaoVoto, UrnaEletronica urna, PublicKey chavePublicaAA)
            throws Exception {
        if (tokenVoto == null) {
            throw new IllegalStateException("Eleitor não autenticado");
        }

        // Encripta o voto com a chave pública da AA
        byte[] votoEncriptado = encriptarVoto(opcaoVoto, chavePublicaAA);

        // Submete à urna
        urna.receberVoto(votoEncriptado, tokenVoto);
    }

    private byte[] encriptarVoto(String voto, PublicKey chavePublica) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, chavePublica);
        return cipher.doFinal(voto.getBytes());
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public PublicKey getChavePublica() {
        return parChaves.getPublic();
    }

    public CertificadoEleitor getCertificado() {
        return certificado;
    }
    public UUID getTokenVoto() {
        if (this.tokenVoto == null) {
            throw new IllegalStateException("Eleitor não possui token de voto (não autenticado no SV)");
        }
        return this.tokenVoto;
    }
}