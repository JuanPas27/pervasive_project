package codelets.perceptual;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;

import java.util.Map;
import java.util.HashMap;

public class TableCodelet extends Codelet {
    private Memory chairsPressureMO, tableCamsMO, chairsContextMO;

    @Override
    public void accessMemoryObjects() {
        this.chairsPressureMO = this.getInput("CHAIRS_PRESSURE_MO");
        this.tableCamsMO = this.getInput("TABLE_CAMS_MO");
        this.chairsContextMO = this.getOutput("CHAIRS_CONTEXT_MO");
    }

    @Override
    public void calculateActivation() {
        this.activation = 1.0;
    }

    @Override
    public void proc() {
        Map<String, String> pressureMap = (Map<String, String>) chairsPressureMO.getI();
        Map<String, String> camsMap = (Map<String, String>) tableCamsMO.getI();
        if (pressureMap == null || camsMap == null) return;

        // Mapa donde guardaremos la conclusión de cada silla
        Map<String, String> finalChairsStatus = new HashMap<>();

        // 1. Unificar la visión de las cámaras (Redundancia)
        // Si cualquiera de las dos cámaras ve a alguien sentado o de pie, entonces válido
        boolean visionDetectaPersona = camsMap.containsValue("sentada") || camsMap.containsValue("de pie");

        // 2. Evaluar sillas
        for (String chairId : pressureMap.keySet()) {
            String pressureStatus = pressureMap.get(chairId); // "o" u "v"

            if (pressureStatus.equals("o")) { // Presión es "o" (ocupada)
                finalChairsStatus.put(chairId, visionDetectaPersona ? "OCUPADA_CONFIRMADA" : "ALERTA_FALSA_PRESION");
            } else { // Presión es "v" (vacío)
                // Las cámaras ven a alguien pero el sensor de la silla no marca peso
                // Podría ser alguien de pie muy cerca o sentado incorrectamente
                finalChairsStatus.put(chairId, visionDetectaPersona ? "VACIA_PRESENCIA_CERCANA" : "VACIA_CONFIRMADA");
            }
        }
        // 3. Enviar el map a la memoria de la mente
        chairsContextMO.setI(finalChairsStatus);

        System.out.println("Mente - Estado detallado: " + finalChairsStatus);
    }
}
