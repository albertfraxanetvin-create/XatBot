import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Ho he fet amb ChatGPT i sincerament no entenc res de la part on s'incorpora l'api
// En el primer commit ho tenia incorporat el chatgpt seguint el vídeo que vas recomanar
// però com en dos dies no m'ha funcionat he acabat amb el ChatGPT. Al final el problema era l'api key que està obsoleta.

public class ConsultaChatGPT {

    private static final String API_KEY = System.getenv("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");
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
    // Historial de la conversa
    private static final List<String> conversationHistory = new ArrayList<>();
    // Paràmetres configurables
    private static double temperature = 0.7;
    private static String systemRole = "Ets un assistent expert en Java.";
    // Codis de colors ANSI
    private static final String COLOR_USER = "\u001B[32m";
    private static final String COLOR_BOT = "\u001B[34m";
    private static final String COLOR_RESET = "\u001B[0m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Missatge inicial del sistema
        conversationHistory.add("System: " + systemRole);
        System.out.println(COLOR_BOT + "Benvingut al xat amb ChatGPT! Escriu 'sortir' per finalitzar." + COLOR_RESET);
        System.out.println(COLOR_BOT + "Comandes disponibles: /rol [text], /creativitat [0.0-1.0]" + COLOR_RESET);

        while (true) {
            System.out.print(COLOR_USER + "\nTu: " + COLOR_RESET);
            String userInput = scanner.nextLine().trim();
            if (userInput.equalsIgnoreCase("sortir")) {
                System.out.println(COLOR_BOT + "Xat finalitzat." + COLOR_RESET);
                break;
            }
            // Comandes especials
            if (userInput.startsWith("/rol ")) {
                systemRole = userInput.substring(5).trim();
                conversationHistory.set(0, "System: " + systemRole);
                System.out.println(COLOR_BOT + "Rol del sistema actualitzat a: " + systemRole + COLOR_RESET);
                continue;
            }
            if (userInput.startsWith("/creativitat ")) {
                try {
                    double t = Double.parseDouble(userInput.substring(12).trim());
                    if (t >= 0 && t <= 1) {
                        temperature = t;
                        System.out.println(COLOR_BOT + "Creativitat actualitzada a: " + temperature + COLOR_RESET);
                    } else {
                        System.out.println(COLOR_BOT + "Valor ha de ser entre 0.0 i 1.0" + COLOR_RESET);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(COLOR_BOT + "Valor invàlid" + COLOR_RESET);
                }
                continue;
            }
            // Afegir missatge de l'usuari
            conversationHistory.add("User: " + userInput);
            try {
                String response = getChatGPTResponse(buildPrompt());
                System.out.println(COLOR_BOT + "\nChatGPT: " + COLOR_RESET + response);
                // Afegir resposta del bot a l'historial
                conversationHistory.add("ChatGPT: " + response);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error de connexió amb l'API: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error inesperat: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static String buildPrompt() {
        StringBuilder sb = new StringBuilder();
        for (String msg : conversationHistory) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }

    private static String getChatGPTResponse(String prompt) throws IOException, InterruptedException {
        // Crear JSON de forma manual
        String jsonInput = "{"
                + "\"model\": \"gpt-3.5-turbo\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}],"
                + "\"temperature\": " + temperature + ","
                + "\"max_tokens\": 150"
                + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) throw new IOException("Clau API invàlida (401).");
        if (response.statusCode() != 200) throw new IOException("Resposta inesperada: " + response.body());

        // Parsing manual del JSON
        String body = response.body();
        int idx = body.indexOf("\"content\":\"");
        if (idx == -1) return "Resposta no trobada.";
        int start = idx + 11;
        int end = body.indexOf("\"", start);
        if (end == -1) end = body.length();
        String text = body.substring(start, end);
        text = text.replace("\\n", "\n").replace("\\\"", "\"");
        return text.trim();
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
