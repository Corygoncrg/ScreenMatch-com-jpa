package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String atores, Double avaliacao);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    @Query("SELECT s FROM Serie s " +
            "JOIN s.episodios e " +
            "GROUP BY s " +
            "ORDER BY MAX(e.dataLancamento) DESC LIMIT 5")
    List<Serie> encontrarLancamentosMaisRecentes();

    List<Serie> findTop5ByOrderByEpisodiosDataLancamentoDesc();

    List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(Integer temporadas, Double avaliacao);

    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :temporadas AND s.avaliacao >= :avaliacao")
    List<Serie> seriePorTotalDeTemporadasEAvaliacao(Integer temporadas, Double avaliacao);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:episodioTrecho%")
    List<Episodio> episodioPorTrecho(String episodioTrecho);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.avaliacao DESC LIMIT 5")
    List<Episodio> top5Episodio(Serie serie);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie AND YEAR(e.dataLancamento) >= :anoLancamento")
    List<Episodio> episodioPorAnoESerie(Serie serie, int anoLancamento);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s.id = :id AND e.temporada = :numero")
    List<Episodio> obterEpisodiosPorTemporada(Long id, Long numero);

}
