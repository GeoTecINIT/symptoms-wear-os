package es.uji.geotec.symptomswearos.command;

import es.uji.geotec.wearossensors.sensor.WearSensor;

public interface CollectionCommand {
    void executeStart(WearSensor sensor);
    void executeStop(WearSensor sensor);
}
