package codelets.perceptual;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;

public class BedCodelet extends Codelet {
    private Memory bedPressureMO, bedCamMO, bedContextMO;

    @Override
    public void accessMemoryObjects() {
        this.bedPressureMO = this.getInput("BED_PRESSURE_MO");
        this.bedCamMO = this.getInput("BED_CAM_MO");
        this.bedContextMO = this.getOutput("BED_CONTEXT_MO");
    }

    @Override
    public void calculateActivation() {
        this.activation = 1.0;
    }

    @Override
    public void proc() {
        String pressure = (String) bedPressureMO.getI();
        String vision = (String) bedCamMO.getI();
        if (pressure == null || vision == null) return;

        String contextoFinal;
        if (pressure.equals("a") && vision.equals("acostada")) contextoFinal = "DURMIENDO";
        else if ((pressure.equals("s1") || pressure.equals("s2")) && vision.equals("sentada")) contextoFinal = "SENTADO_CAMA_" + pressure;
        else if (pressure.equals("v") && vision.equals("acostada")) contextoFinal = "ERROR_SENSOR_CAMA";
        else contextoFinal = "CAMA_VACIA";

        bedContextMO.setI(contextoFinal);
    }
}
