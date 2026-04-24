import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.Mind;
import codelets.behavioral.ActionCodelet;
import codelets.perceptual.BedCodelet;
import codelets.perceptual.TableCodelet;
import codelets.sensory.SensoryCodelet;

import java.util.HashMap;

public class AgentMind extends Mind {
    public AgentMind(Environment env) {
        super();

        // Create Codelets Groups
        createCodeletGroup("Sensory");
        createCodeletGroup("Perceptual");
        createCodeletGroup("Behavioral");

        // Create Memory Groups
        createMemoryGroup("Sensory");
        createMemoryGroup("Working");

        // Declare y Register Memory Objects

        // 1. Crear Memory Objects (Workspace)
        Memory chairsPressureMO = createMemoryObject("CHAIRS_PRESSURE_MO", new HashMap<String, String>());
        registerMemory(chairsPressureMO, "Sensory");
        Memory tableCamsMO = createMemoryObject("TABLE_CAMS_MO", new HashMap<String, String>());
        registerMemory(tableCamsMO, "Sensory");
        Memory bedPressureMO = createMemoryObject("BED_PRESSURE_MO", "v");
        registerMemory(bedPressureMO, "Sensory");
        Memory bedCamMO = createMemoryObject("BED_CAM_MO", "vacia");
        registerMemory(bedCamMO, "Sensory");
        Memory chairsContextMO = createMemoryObject("CHAIRS_CONTEXT_MO", new HashMap<String, String>());
        registerMemory(chairsContextMO, "Working");
        Memory bedContextMO = createMemoryObject("BED_CONTEXT_MO", "");
        registerMemory(bedContextMO, "Working");

        // 2. Insertar Sensory Codelet
        // SensoryCodelet sensory = new SensoryCodelet("ws://localhost:8000/ws/cst_mind");
        String wsUrl = System.getenv("WEBSOCKET_URL");
        if (wsUrl == null || wsUrl.isEmpty()) {
            wsUrl = "ws://localhost:8000/ws/cst_mind"; // Ruta por defecto
        }
        SensoryCodelet sensory = new SensoryCodelet(wsUrl);
        sensory.addOutput(chairsPressureMO);
        sensory.addOutput(tableCamsMO);
        sensory.addOutput(bedPressureMO);
        sensory.addOutput(bedCamMO);
        insertCodelet(sensory);
        registerCodelet(sensory, "Sensory");

        // 3. Insertar Perceptual Codelets
        TableCodelet table = new TableCodelet();
        table.addInput(chairsPressureMO);
        table.addInput(tableCamsMO);
        table.addOutput(chairsContextMO);
        insertCodelet(table);
        registerCodelet(sensory, "Perceptual");

        BedCodelet bed = new BedCodelet();
        bed.addInput(bedPressureMO);
        bed.addInput(bedCamMO);
        bed.addOutput(bedContextMO);
        insertCodelet(bed);
        registerCodelet(sensory, "Perceptual");

        // 4. Insertar Behavioral Codelet
        ActionCodelet action = new ActionCodelet();
        action.addInput(chairsContextMO);
        action.addInput(bedContextMO);
        insertCodelet(action);
        registerCodelet(sensory, "Behavioral");

        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(200);

        // Start Cognitive Cycle
        start();
    }
}
