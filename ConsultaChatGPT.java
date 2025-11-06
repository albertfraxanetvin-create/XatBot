package XatBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConsultaChatGPT {
    public static String consulta (String prompt) {
        String urlString = "https://api.openai.com/v1/chat/completions";
        String key = "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";
        String modelo ="gpt-3.5-turbo";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("POST");
            conexion.setRequestProperty("Authorization", "Bearer " + key);
            conexion.setRequestProperty("Content-Type", "application/json");

            String cuerpo = "{\"model\": \"" + modelo + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
            conexion.setDoOutput(true);
            OutputStreamWriter escritor = new OutputStreamWriter (conexion.getOutputStream());
            escritor.write(cuerpo);
            escritor.flush();
            escritor.close();

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));

            String linea;
            StringBuffer sr = new StringBuffer();

            while ((linea = lector.readLine()) != null) {
                sr.append(linea);   
            }
            lector.close();

            String respuestaFinal = extraerJSON(sr.toString());

            return respuestaFinal;
       
        } catch (IOException e) {
            
            e.printStackTrace();
            return null;
        }

    }

    private static String extraerJSON(String respuestaJSON) {
        int principio = respuestaJSON.indexOf("content") + 11;
        int end = respuestaJSON.indexOf("\"", principio);
        return respuestaJSON.substring(principio, end);
    }
    public static void main (String[] args) {

        System.out.println(ConsultaChatGPT.consulta("¿Sabes contar hasta 10?"));
    }
}
