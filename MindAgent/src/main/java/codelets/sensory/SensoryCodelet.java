package codelets.sensory;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SensoryCodelet extends Codelet {
    private Memory chairsPressureMO, tableCamsMO, bedPressureMO, bedCamMO;
    private Map<String, String> chairsMap = new HashMap<>();
    private Map<String, String> tableCamsMap = new HashMap<>();
    private String latestMessage = null;
    private WebSocketClient wsClient;

    public SensoryCodelet(String uri) {
        try {
            wsClient = new WebSocketClient(new URI(uri)) {
                @Override public void onOpen(ServerHandshake h) { System.out.println("CST: Conectado a FastAPI"); }
                @Override public void onMessage(String m) {
                    latestMessage = m;
                    System.out.println("CORTEX SENSORIAL RECIBIÓ: " + latestMessage);
                }
                @Override public void onClose(int c, String r, boolean re) { }
                @Override public void onError(Exception e) {
                    // Ocultamos el error gigante rojo para no ensuciar la consola
                }
            };

            // Hilo Guardián que insiste para conectarse
            new Thread(() -> {
                try {
                    wsClient.connectBlocking();
                } catch (InterruptedException e) {}
                while (true) {
                    try {
                        if (!wsClient.isOpen()) {
                            System.out.println("CST: Esperando a FastAPI... reintentando conexión.");
                            wsClient.reconnectBlocking();
                        }
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {}
                }
            }).start();

        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void accessMemoryObjects() {
        this.chairsPressureMO = this.getOutput("CHAIRS_PRESSURE_MO");
        this.tableCamsMO = this.getOutput("TABLE_CAMS_MO");
        this.bedPressureMO = this.getOutput("BED_PRESSURE_MO");
        this.bedCamMO = this.getOutput("BED_CAM_MO");

        // Inicializar sillas conocidas
        //chairsMap.put("silla_1", "v"); chairsMap.put("silla_2", "v");
        //chairsMap.put("silla_3", "v"); chairsMap.put("silla_4", "v");
        //chairsPressureMO.setI(new HashMap<>(chairsMap));
    }

    @Override
    public void calculateActivation() {
        this.activation = 1.0;
    }

    @Override
    public void proc() {
        if (latestMessage != null) {
            JSONObject json = new JSONObject(latestMessage);
            String type = json.getString("type");
            String id = json.getString("sensor_id");
            String status = json.getString("status");

            switch (type) {
                case "chair_pressure":
                    chairsMap.put(id, status);
                    chairsPressureMO.setI(new HashMap<>(chairsMap));
                    break;
                case "table_cam":
                    tableCamsMap.put(id, status);
                    tableCamsMO.setI(new HashMap<>(tableCamsMap));
                    break;
                case "bed_pressure":
                    bedPressureMO.setI(status);
                    break;
                case "bed_cam":
                    bedCamMO.setI(status);
                    break;
            }
            latestMessage = null;
        }
    }
}
