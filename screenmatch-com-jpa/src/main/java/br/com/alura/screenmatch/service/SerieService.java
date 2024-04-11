package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository repositorio;

    public List<SerieDTO> obterTodasAsSeries() {
        return converteDados(repositorio.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return converteDados(repositorio.findTop5ByOrderByAvaliacaoDesc());
    }

    public List<SerieDTO> obterMaisRecentes() {
        return converteDados(repositorio.encontrarLancamentosMaisRecentes());
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> serie = repositorio.findById(id);
        if (serie.isPresent()) {
            Serie s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(),
                    s.getTotalTemporadas(), s.getAvaliacao(),
                    s.getGenero(), s.getDiretor(),
                    s.getAtores(), s.getPoster(),
                    s.getSinopse());
        }
        return null;

    }

    public List<EpisodioDTO> obterTodasAsTemporadas(Long id) {
        Optional<Serie> serie = repositorio.findById(id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return converteEpisodios(s.getEpisodios());
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporadaPorNumero(Long id, Long numero) {
        return converteEpisodios(repositorio.obterEpisodiosPorTemporada(id, numero));
    }

    public List<SerieDTO> obterSeriesPorGenero(String nomeGenero) {
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        return converteDados(repositorio.findByGenero(categoria));
    }

    public List<EpisodioDTO> obterTop5Episodios(Long id){
        Serie serie = repositorio.findById(id).get();
        return converteEpisodios(repositorio.top5Episodio(serie));
    }


    private List<SerieDTO> converteDados(List<Serie> series) {
        return series.stream()
                .map(s -> new SerieDTO(s.getId(), s.getTitulo(),
                        s.getTotalTemporadas(), s.getAvaliacao(),
                        s.getGenero(), s.getDiretor(),
                        s.getAtores(), s.getPoster(),
                        s.getSinopse()))
                .collect(Collectors.toList());
    }

    private List<EpisodioDTO> converteEpisodios(List<Episodio> episodios) {
        return episodios.stream()
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                .collect(Collectors.toList());
    }


}

