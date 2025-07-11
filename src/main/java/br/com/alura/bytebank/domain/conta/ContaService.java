package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ContaService {

    private ConnectionFactory connection;

    public ContaService() {
        this.connection = new ConnectionFactory();
    }

    public Set<Conta> listarContasAbertas() {
        Connection conn = connection.recuperarConexao();
        return new ContaDAO(conn).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        if (Boolean.FALSE.equals(conta.getEstaAtiva())) {
            throw new RegraDeNegocioException("Conta não está ativa");
        }

        alterar(conta.getNumero(), conta.getSaldo().subtract(valor));
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }
        if (Boolean.FALSE.equals(conta.getEstaAtiva())) {
            throw new RegraDeNegocioException("Conta não está ativa");
        }
        alterar(conta.getNumero(), conta.getSaldo().add(valor));
    }

    public void realizarTransferencia(Integer numeroDaContaOrigem, Integer numeroDaContaDestino, BigDecimal valor) {
        this.buscarContaPorNumero(numeroDaContaDestino);
        this.realizarSaque(numeroDaContaOrigem, valor);
        this.realizarDeposito(numeroDaContaDestino, valor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).deletar(conta.getNumero());
    }

    public void encerrarLogico(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterarLogico(conta.getNumero());
    }

    private Conta buscarContaPorNumero(Integer numero) {
        Connection conn = connection.recuperarConexao();
        Conta conta = new ContaDAO(conn).listarPorNumero(numero);
        if (Objects.nonNull(conta)) {
            return conta;
        }
        throw new RegraDeNegocioException("Não existe conta cadastrada com o número " + numero);
    }

    private void alterar(Integer numero, BigDecimal valor) {
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).alterar(numero, valor);
    }
}
