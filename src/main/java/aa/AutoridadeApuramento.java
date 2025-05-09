package aa;

import shared.exceptions.DescriptografiaFalhouException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

public class AutoridadeApuramento {
    private final PrivateKey chavePrivadaAA;
    private final PublicKey chavePublicaAA;
    private Map<String, Integer> resultados;

    public AutoridadeApuramento(PrivateKey chavePrivadaAA, PublicKey chavePublicaAA) {
        this.chavePrivadaAA = chavePrivadaAA;
        this.chavePublicaAA = chavePublicaAA;
        this.resultados = new HashMap<>();
    }

    public List<String> desencriptarVotos(List<byte[]> votosEncriptados) throws DescriptografiaFalhouException {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, chavePrivadaAA);

            return votosEncriptados.stream()
                    .map(voto -> {
                        try {
                            return new String(cipher.doFinal(voto));
                        } catch (Exception e) {
                            throw new RuntimeException("Falha ao desencriptar voto", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new DescriptografiaFalhouException("Erro na configuração de desencriptação: " + e.getMessage());
        }
    }

    public Map<String, Integer> apurarVotos(List<String> votos) {
        resultados = votos.stream()
                .collect(Collectors.toMap(
                        voto -> voto,
                        voto -> 1,
                        Integer::sum
                ));
        return new HashMap<>(resultados);
    }

    public String gerarRelatorio() {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("=== RELATÓRIO DE APURAMENTO ===\n");
        relatorio.append("Total de votos apurados: ").append(resultados.values().stream().mapToInt(Integer::intValue).sum()).append("\n\n");
        relatorio.append("RESULTADOS:\n");

        resultados.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> relatorio.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" votos\n"));

        relatorio.append("\n=== FIM DO RELATÓRIO ===");
        return relatorio.toString();
    }

    public PublicKey getChavePublicaAA() {
        return chavePublicaAA;
    }
}