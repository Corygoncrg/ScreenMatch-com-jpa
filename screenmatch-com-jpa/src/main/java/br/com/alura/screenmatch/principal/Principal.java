package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=b9b5453b";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;


    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        int opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Buscar top 5
                    7 - Buscar séries por gênero
                    8 - Buscar por temporadas e avaliação
                    9 - Buscar episódio por trecho
                    10 - Buscar top 5 episódios de uma série
                    11 - Buscar episódios de uma série a partir de um ano
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarListaDeSeries();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorGenero();
                    break;
                case 8:
                    buscarPorTemporadasEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTop5EpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosPorAnoESerie();
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }



    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        //dadosSeries.add(dados);
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarListaDeSeries();
        System.out.println("Escolha uma série");
        String nomeSerie = leitura.nextLine();
        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            Serie serieEncontrada = serie.get();
//            DadosSerie dadosSerie = getDadosSerie();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                            .map(e -> new Episodio(t.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

        } else {
            System.out.println("Não foi possível encontrar essa série");
        }
    }
    private void listarListaDeSeries(){
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite o nome da série para busca");
        String nomeSerie = leitura.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da Série: " + serieBusca.get());
        } else {
            System.out.println("Nenhuma série encontrada.");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Digite o nome do ator");
        String nomeAtor = leitura.nextLine();

        System.out.println("A partir de qual avaliação?");
        Double avaliacao = leitura.nextDouble();
        List<Serie> serieEncontrada = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Series em que" + nomeAtor + " trabalhou: ");
        serieEncontrada.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriePorGenero() {
        try {
        System.out.println("Por qual gênero gostaria de buscar?");
            String nomeGenero = leitura.nextLine();
            Categoria categoria = Categoria.fromPortugues(nomeGenero);
            List<Serie> seriePorGenero = repositorio.findByGenero(categoria);
            seriePorGenero.forEach(System.out::println);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

    }
    private void buscarPorTemporadasEAvaliacao() {
        System.out.println("Até quantas temporadas quer que a série tenha?");
        Integer numeroTemporada = leitura.nextInt();
        System.out.println("Avaliações a partir de qual valor em avaliações?");
        Double avaliacao = leitura.nextDouble();
        List<Serie> seriePorTemporadasEAvaliacao = repositorio.seriePorTotalDeTemporadasEAvaliacao(numeroTemporada, avaliacao);
        System.out.println("Séries encontradas: ");
        seriePorTemporadasEAvaliacao.forEach(System.out::println);
    }

    private void buscarEpisodioPorTrecho () {
        System.out.println("Digite o trecho do episódio");
        String episodioTrecho = leitura.nextLine();
        List<Episodio> episodioPorTrecho = repositorio.episodioPorTrecho(episodioTrecho);
        episodioPorTrecho.forEach(e -> System.out.printf("Série: %s Temporada %s - Episodio %s - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarTop5EpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serieTop5 = serieBusca.get();
            List<Episodio> top5Episodios = repositorio.top5Episodio(serieTop5);
            top5Episodios.forEach(e -> System.out.printf("Série: %s Temporada %s - Episodio %s - %s\n",
                    e.getSerie().getTitulo(), e.getTemporada(),
                    e.getNumeroEpisodio(), e.getTitulo()));
        }

    }


    private void buscarEpisodiosPorAnoESerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            System.out.println("A partir de qual ano gostaria de buscar?");
            int anoLancamento = leitura.nextInt();
            leitura.nextLine();
            Serie serie = serieBusca.get();
            List<Episodio> episodioPorAnoeSerie = repositorio.episodioPorAnoESerie(serie, anoLancamento);
            episodioPorAnoeSerie.forEach(System.out::println);
        }
    }
}