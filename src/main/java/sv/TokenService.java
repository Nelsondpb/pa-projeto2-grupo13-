package sv;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TokenService {
    private final Set<UUID> tokensEmitidos = new HashSet<>();

    public UUID emitirToken() {
        UUID token = UUID.randomUUID();
        tokensEmitidos.add(token);
        return token;
    }

    public boolean validarToken(UUID token) {
        return tokensEmitidos.remove(token); // Remove se existir
    }
}