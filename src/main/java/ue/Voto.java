package ue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

public class Voto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] votoEncriptado;
    private final UUID token;

    public Voto(byte[] votoEncriptado, UUID token) {
        this.votoEncriptado = Arrays.copyOf(votoEncriptado, votoEncriptado.length);
        this.token = token;
    }

    // Getters
    public byte[] getVotoEncriptado() {
        return Arrays.copyOf(votoEncriptado, votoEncriptado.length);
    }

    public UUID getToken() {
        return token;
    }
}