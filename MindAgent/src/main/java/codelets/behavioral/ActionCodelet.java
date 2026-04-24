package codelets.behavioral;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ActionCodelet extends Codelet {
    private Memory chairsContextMO, bedContextMO;
    private Map<String, String> lastChairStates = new HashMap<>();
    private String lastBedState = "";

    @Override
    public void accessMemoryObjects() {
        this.chairsContextMO = this.getInput("CHAIRS_CONTEXT_MO");
        this.bedContextMO = this.getInput("BED_CONTEXT_MO");
    }

    @Override
    public void calculateActivation() {
        this.activation = 1.0;
    }

    @Override
    public void proc() {
        Map<String, String> currentChairs = (Map<String, String>) chairsContextMO.getI();
        String currentBed = (String) bedContextMO.getI();

        // Acciones Sillas
        if (currentChairs != null) {
            for (String chairId : currentChairs.keySet()) {
                String status = currentChairs.get(chairId);
                String lastStatus = lastChairStates.getOrDefault(chairId, "");
                System.out.println(lastStatus);

                if (!status.equals(lastStatus)) {
                    if (status.equals("OCUPADA_CONFIRMADA")) {
                        triggerAction("luz_activa_" + chairId);
                    } else if (status.equals("VACIA_CONFIRMADA") || status.equals("VACIA_PRESENCIA_CERCANA")) {
                        triggerAction("luz_apaga_" + chairId);
                    }
                    lastChairStates.put(chairId, status);
                }
            }
        }

        // Acciones Cama
        if (currentBed != null && !currentBed.equals(lastBedState)) {
            if (currentBed.equals("DURMIENDO")) triggerAction("modo_nocturno_on");
            else if (currentBed.startsWith("SENTADO")) triggerAction("luz_cortesia_on");
            lastBedState = currentBed;
        }
    }

    private void triggerAction(String action) {
        try {
            // URL url = new URL("http://localhost:8000/execute_action/" + action);
            String apiUrl = System.getenv("API_URL");
            if (apiUrl == null || apiUrl.isEmpty()) {
                apiUrl = "http://localhost:8000";
            }
            URL url = new URL(apiUrl + "/execute_action/" + action);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
