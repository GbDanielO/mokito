package br.com.alura.leilao.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;

class FinalizarLeilaoServiceTest {

    private FinalizarLeilaoService service;

    // Toda propriedade com essa anotação o mockito vai mockar no inicializar
    @Mock
    private LeilaoDao leilaoDao;

    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    @BeforeEach
    public void inicializar() {
        // inicializa os mocks
        MockitoAnnotations.initMocks( this );
        this.service = new FinalizarLeilaoService( leilaoDao, enviadorDeEmails );
    }

    @Test
    void deveriaFinalizarUmLeilao() {
        List<Leilao> leiloes = leiloes();

        // Define um retorno para o método mockado
        Mockito.when( leilaoDao.buscarLeiloesExpirados() ).thenReturn( leiloes );

        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get( 0 );

        // testes
        Assert.assertTrue( leilao.isFechado() );
        Assert.assertEquals( new BigDecimal( "900" ), leilao.getLanceVencedor().getValor() );

        // verifica se determinado método foi chamado. Nesse caso temos o parametro.
        // Poderia passar Mockito.any() caso não tivesse o parametro.
        Mockito.verify( leilaoDao ).salvar( leilao );
    }

    @Test
    void deveriaEnviarEmailParaVencedorDoLeilao() {
        List<Leilao> leiloes = leiloes();

        // Define um retorno para o método mockado
        Mockito.when( leilaoDao.buscarLeiloesExpirados() ).thenReturn( leiloes );

        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get( 0 );
        Lance lanceVencedor = leilao.getLanceVencedor();

        // verifica se determinado método foi chamado. Nesse caso temos o parametro.
        // Poderia passar Mockito.any() caso não tivesse o parametro.
        Mockito.verify( enviadorDeEmails ).enviarEmailVencedorLeilao( lanceVencedor );
    }

    @Test
    void naoDeveriaEnviarEmailParaVencedorDoLeilaoEmCasoDeErroAoEncerrarOLeilao() {
        List<Leilao> leiloes = leiloes();

        Mockito.when( leilaoDao.buscarLeiloesExpirados() ).thenReturn( leiloes );

        // Forçando retornar um erro ao invés de um determinado objeto.
        Mockito.when( leilaoDao.salvar( Mockito.any() ) ).thenThrow( RuntimeException.class );

        try {
            service.finalizarLeiloesExpirados();
            Mockito.verifyNoInteractions( enviadorDeEmails );
        } catch ( Exception e ) {
        }
    }

    private List<Leilao> leiloes() {
        List<Leilao> lista = new ArrayList<>();

        Leilao leilao = new Leilao( "Celular", new BigDecimal( "500" ), new Usuario( "Fulano" ) );

        Lance primeiro = new Lance( new Usuario( "Beltrano" ), new BigDecimal( "600" ) );
        Lance segundo = new Lance( new Usuario( "Ciclano" ), new BigDecimal( "900" ) );

        leilao.propoe( primeiro );
        leilao.propoe( segundo );

        lista.add( leilao );

        return lista;
    }

}
