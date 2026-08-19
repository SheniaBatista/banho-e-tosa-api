package com.portfolio.banhoetosa.cli;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class Console {

    private static final Charset SAIDA = charsetDeSaida();
    private static final boolean UNICODE = suporta("─│┌┐└┘·›✓✗●");
    private static final boolean COR = definirCor();

    private static final String H = escolher("─", "-");
    private static final String V = escolher("│", "|");
    private static final String CANTO_SE = escolher("┌", "+");
    private static final String CANTO_SD = escolher("┐", "+");
    private static final String CANTO_IE = escolher("└", "+");
    private static final String CANTO_ID = escolher("┘", "+");
    private static final String SETA = escolher("›", ">");
    private static final String PONTO = escolher("·", "-");
    private static final String BOLINHA = escolher("●", "*");
    private static final String CERTO = escolher("✓", "OK");
    private static final String ERRADO = escolher("✗", "X");
    private static final String ALERTA = "!";

    private static final String ESC = "\u001B";
    private static final String RESET = ESC + "[0m";
    private static final String NEGRITO = ESC + "[1m";
    private static final String FRACO = ESC + "[2m";
    private static final String CIANO = ESC + "[36m";
    private static final String VERDE = ESC + "[32m";
    private static final String AMARELO = ESC + "[33m";
    private static final String VERMELHO = ESC + "[31m";
    private static final String AZUL = ESC + "[94m";

    static final int LARGURA = 68;
    static final String MARGEM = "  ";

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner scanner;

    public Console(Scanner scanner) {
        this.scanner = scanner;
    }

    private static Charset charsetDeSaida() {
        for (String propriedade : new String[]{"stdout.encoding", "native.encoding"}) {
            String nome = System.getProperty(propriedade);
            if (nome != null) {
                try {
                    return Charset.forName(nome);
                } catch (RuntimeException ignorado) {
                    continue;
                }
            }
        }
        return Charset.defaultCharset();
    }

    private static boolean suporta(String amostra) {
        return SAIDA.canEncode() && SAIDA.newEncoder().canEncode(amostra);
    }

    private static boolean definirCor() {
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        return System.getenv("FORCE_COLOR") != null || System.console() != null;
    }

    private static String escolher(String unicode, String ascii) {
        return UNICODE ? unicode : ascii;
    }

    private static String pintar(String cor, String texto) {
        return COR ? cor + texto + RESET : texto;
    }

    public static String negrito(String texto) {
        return pintar(NEGRITO, texto);
    }

    public static String discreto(String texto) {
        return pintar(FRACO, texto);
    }

    public static String ciano(String texto) {
        return pintar(CIANO, texto);
    }

    public static String verde(String texto) {
        return pintar(VERDE, texto);
    }

    public static String amarelo(String texto) {
        return pintar(AMARELO, texto);
    }

    public static String vermelho(String texto) {
        return pintar(VERMELHO, texto);
    }

    public static String azul(String texto) {
        return pintar(AZUL, texto);
    }

    public void linhaEmBranco() {
        System.out.println();
    }

    public void escrever(String texto) {
        System.out.println(MARGEM + texto);
    }

    public void separador() {
        System.out.println(MARGEM + discreto(H.repeat(LARGURA)));
    }

    public void regua(int... larguras) {
        StringBuilder linha = new StringBuilder();
        for (int largura : larguras) {
            linha.append(H.repeat(Math.max(1, largura - 2))).append("  ");
        }
        System.out.println(MARGEM + discreto(linha.toString().stripTrailing()));
    }

    public void banner() {
        System.out.println();
        System.out.println(MARGEM + ciano(CANTO_SE + H.repeat(LARGURA) + CANTO_SD));
        System.out.println(MARGEM + ciano(V) + preencher("  BANHO & TOSA", true) + ciano(V));
        System.out.println(MARGEM + ciano(V)
                + preencher("  Gestão de clientes, pets e agendamentos", false) + ciano(V));
        System.out.println(MARGEM + ciano(CANTO_IE + H.repeat(LARGURA) + CANTO_ID));
    }

    private String preencher(String texto, boolean destacado) {
        String corpo = texto + " ".repeat(Math.max(0, LARGURA - texto.length()));
        return destacado ? negrito(corpo) : discreto(corpo);
    }

    public void rodapeDoBanner(String stack, String estado) {
        System.out.println(MARGEM + " " + discreto(stack)
                + "   " + verde(BOLINHA) + " " + discreto(estado));
    }

    public void secao(String texto) {
        System.out.println();
        System.out.println(MARGEM + ciano(negrito(SETA + " " + texto.toUpperCase(PT_BR))));
        separador();
    }

    public void cabecalho(String texto) {
        System.out.println();
        System.out.println(MARGEM + negrito(texto));
        separador();
    }

    public void sucesso(String texto) {
        System.out.println(MARGEM + verde(CERTO) + " " + texto);
    }

    public void aviso(String texto) {
        System.out.println(MARGEM + amarelo(ALERTA) + " " + texto);
    }

    public void erro(String texto) {
        System.out.println(MARGEM + vermelho(ERRADO) + " " + texto);
    }

    public void informacao(String texto) {
        System.out.println(MARGEM + discreto(PONTO + " " + texto));
    }

    public void detalhe(String rotulo, String valor) {
        System.out.println(MARGEM + "  " + discreto(coluna(rotulo, 14)) + valor);
    }

    private String perguntar(String rotulo) {
        System.out.print(MARGEM + rotulo + " " + ciano(SETA) + " ");
        return scanner.nextLine().trim();
    }

    public String lerTextoObrigatorio(String rotulo) {
        while (true) {
            String valor = perguntar(rotulo);
            if (!valor.isEmpty()) {
                return valor;
            }
            erro("Campo obrigatório. Informe um valor.");
        }
    }

    public String lerTextoOpcional(String rotulo) {
        String valor = perguntar(rotulo + " " + discreto("(opcional)"));
        return valor.isEmpty() ? null : valor;
    }

    public Long lerId(String rotulo) {
        while (true) {
            String valor = perguntar(rotulo);
            try {
                long id = Long.parseLong(valor);
                if (id > 0) {
                    return id;
                }
                erro("O id deve ser um número maior que zero.");
            } catch (NumberFormatException ex) {
                erro("Informe um número inteiro válido.");
            }
        }
    }

    public Double lerPeso(String rotulo) {
        while (true) {
            String valor = perguntar(rotulo).replace(',', '.');
            try {
                double peso = Double.parseDouble(valor);
                if (peso > 0) {
                    return peso;
                }
                erro("O peso deve ser maior que zero.");
            } catch (NumberFormatException ex) {
                erro("Informe um peso válido, por exemplo 7.5.");
            }
        }
    }

    public LocalDateTime lerDataHoraFutura(String rotulo) {
        while (true) {
            String valor = perguntar(rotulo + " " + discreto("(dd/MM/aaaa HH:mm)"));
            try {
                LocalDateTime dataHora = LocalDateTime.parse(valor, FORMATO_DATA);
                if (dataHora.isAfter(LocalDateTime.now())) {
                    return dataHora;
                }
                erro("O agendamento precisa ser em uma data futura.");
            } catch (DateTimeParseException ex) {
                erro("Data inválida. Use o formato 20/01/2027 14:00.");
            }
        }
    }

    public String lerOpcao(String rotulo) {
        return perguntar(rotulo);
    }

    public void aguardarEnter() {
        System.out.println();
        System.out.print(MARGEM + discreto("ENTER para voltar ao menu..."));
        scanner.nextLine();
    }

    public static String moeda(BigDecimal valor) {
        return valor == null ? "-" : String.format(PT_BR, "R$ %,.2f", valor);
    }

    public static String dataHora(LocalDateTime dataHora) {
        return dataHora == null ? "-" : dataHora.format(FORMATO_DATA);
    }

    public static String coluna(String texto, int largura) {
        String base = texto == null ? "-" : texto;
        if (base.length() > largura) {
            return largura > 3 ? base.substring(0, largura - 3) + "..." : base.substring(0, largura);
        }
        return String.format("%-" + largura + "s", base);
    }
}
