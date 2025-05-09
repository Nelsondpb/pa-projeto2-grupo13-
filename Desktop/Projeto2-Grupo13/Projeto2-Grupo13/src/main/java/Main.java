import aa.AutoridadeApuramento;
import ar.AutoridadeRegisto;
import ar.ARServer;
import eleitor.Eleitor;
import shared.CertificadoEleitor;
import sv.ServidorVotacao;
import ue.UrnaEletronica;
import shared.exceptions.*;

import java.io.IOException;
import java.security.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final int SSL_PORT = 9090;
    private static final int SERVER_START_DELAY_MS = 1500;

    public static void main(String[] args) {
        try {
            // 1. Configuração de segurança
            configurarSSL();

            // 2. Geração de chaves
            KeyPair parChavesAR = gerarParChavesRSA();
            KeyPair parChavesAA = gerarParChavesRSA();

            // 3. Inicialização das entidades
            AutoridadeRegisto ar = new AutoridadeRegisto(parChavesAR.getPrivate(), parChavesAR.getPublic());
            ARServer arServer = new ARServer(ar);
            AutoridadeApuramento aa = new AutoridadeApuramento(parChavesAA.getPrivate(), parChavesAA.getPublic());
            ServidorVotacao sv = new ServidorVotacao(ar, aa.getChavePublicaAA());
            UrnaEletronica ue = new UrnaEletronica(sv);

            // 4. Iniciar servidor AR
            iniciarServidorAR(arServer);

            // 5. Fluxo de votação
            executarFluxoVotacaoCompleto(ar, sv, ue, aa);

        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ Erro de criptografia: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erro crítico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void configurarSSL() {
    }

    private static KeyPair gerarParChavesRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private static void iniciarServidorAR(ARServer server) {
        new Thread(() -> {
            try {
                System.out.println("🔒 Servidor AR iniciando na porta " + SSL_PORT + "...");
                server.start();
            } catch (Exception e) {
                System.err.println("❌ Falha no servidor AR: " + e.getMessage());
                System.exit(1);
            }
        }).start();
    }

    private static void executarFluxoVotacaoCompleto(AutoridadeRegisto ar, ServidorVotacao sv,
                                                     UrnaEletronica ue, AutoridadeApuramento aa) {
        try {
            // Espera o servidor iniciar
            Thread.sleep(SERVER_START_DELAY_MS);
            System.out.println("\n✅ Sistema inicializado. Iniciando processo de votação...");

            // 1. Registro do Eleitor
            Eleitor eleitor = registrarEleitor(ar);

            // 2. Autenticação
            autenticarEleitor(eleitor, sv);

            // 3. Votação
            submeterVoto(eleitor, ue, aa);

            // 4. Aguardar encerramento manual
            aguardarEncerramentoManual(ue);

            // 5. Apuramento
            executarApuramento(ue, aa);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Thread interrompida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erro no fluxo de votação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void aguardarEncerramentoManual(UrnaEletronica ue) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n⏳ Digite 'FIM' para encerrar a votação...");
        while (!scanner.nextLine().equalsIgnoreCase("FIM")) {
            System.out.println("Comando inválido. Digite 'FIM' para encerrar.");
        }
        ue.encerrarVotacao();
        scanner.close();
    }

    private static Eleitor registrarEleitor(AutoridadeRegisto ar) throws Exception {
        System.out.println("\n=== FASE 1: REGISTO NA AR ===");
        Eleitor eleitor = new Eleitor("Eleitor_Teste");

        try {
            CertificadoEleitor certificado = eleitor.registarNaAR();
            System.out.println("📄 Certificado obtido:\n" + certificado.toPemFormat());
            System.out.println("ℹ️ Eleitores registrados: " + ar.getEleitoresRegistados().size());
            return eleitor;
        } catch (Exception e) {
            System.err.println("❌ Falha no registro: " + e.getMessage());
            throw e;
        }
    }

    private static void autenticarEleitor(Eleitor eleitor, ServidorVotacao sv) throws Exception {
        System.out.println("\n=== FASE 2: AUTENTICAÇÃO NO SV ===");
        eleitor.autenticarNoSV(sv);
        System.out.println("🔑 Token de voto gerado: " + eleitor.getTokenVoto());
    }

    private static void submeterVoto(Eleitor eleitor, UrnaEletronica ue, AutoridadeApuramento aa) throws Exception {
        System.out.println("\n=== FASE 3: VOTAÇÃO ===");
        String candidato = "CandidatoA";
        System.out.println("🗳️ Enviando voto para: " + candidato);

        eleitor.votar(candidato, ue, aa.getChavePublicaAA());
        System.out.println("✅ Voto registado com sucesso!");
    }

    private static void executarApuramento(UrnaEletronica ue, AutoridadeApuramento aa) {
        try {
            System.out.println("\n=== FASE 4: APURAMENTO ===");

            // 1. Transferir votos para AA
            List<byte[]> votosEncriptados = ue.getVotosEncriptados();
            System.out.println("📨 Transferindo " + votosEncriptados.size() + " votos para a AA...");

            // 2. Desencriptar e contar votos
            List<String> votosDesencriptados = aa.desencriptarVotos(votosEncriptados);
            aa.apurarVotos(votosDesencriptados);

            // 3. Exibir resultados
            System.out.println("\n" + aa.gerarRelatorio());
        } catch (Exception e) {
            System.err.println("❌ Erro no processo de apuramento: " + e.getMessage());
            e.printStackTrace();
        }
    }
}