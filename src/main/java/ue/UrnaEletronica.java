package ue;

import sv.ServidorVotacao;
import shared.exceptions.TokenInvalidoException;
import shared.exceptions.VotoInvalidoException;
import shared.exceptions.VotacaoEncerradaException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UrnaEletronica {
    private final ServidorVotacao servidorVotacao;
    private final List<Voto> votos = new ArrayList<>();
    private boolean votacaoEncerrada = false;

    public UrnaEletronica(ServidorVotacao servidorVotacao) {
        this.servidorVotacao = servidorVotacao;
    }

    public void receberVoto(byte[] votoEncriptado, UUID token)
            throws TokenInvalidoException, VotoInvalidoException, VotacaoEncerradaException {
        if (votacaoEncerrada) {
            throw new VotacaoEncerradaException("Período de votação encerrado");
        }
        if (!servidorVotacao.validarToken(token)) {
            throw new TokenInvalidoException("Token inválido ou já utilizado");
        }
        if (votoEncriptado == null || votoEncriptado.length == 0) {
            throw new VotoInvalidoException("Voto encriptado é inválido");
        }
        votos.add(new Voto(votoEncriptado, token));
    }

    public List<byte[]> getVotosEncriptados() {
        return new ArrayList<>(votos.stream().map(Voto::getVotoEncriptado).toList());
    }

    public void encerrarVotacao() {
        this.votacaoEncerrada = true;
    }

    public boolean isVotacaoEncerrada() {
        return votacaoEncerrada;
    }

    public int getTotalVotos() {
        return votos.size();
    }
}