package sample;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

public class ImageProcessingJob {
    File file; //graphic file
    SimpleStringProperty status;    //bar status
    DoubleProperty progress;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public PropertyChangeSupport getProgressHandler(){
        return pcs;
    }
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.pcs.removePropertyChangeListener(listener);
    }

    public ImageProcessingJob(File f, String s, Double p)
    {
        this.file = f;
        this.status = new SimpleStringProperty(s);
        this.progress = new SimpleDoubleProperty();
        pcs.addPropertyChangeListener("progress", e->{
            if ((double) e.getNewValue() > 0.0 && (double)e.getNewValue() <1.0)
                setStatus("Przetwarzanie..");
            else if ((double)e.getNewValue() >= 1.0)
                setStatus("Zakonczono");
        });
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        double old = this.progress.doubleValue();
        this.progress.set(progress);
        this.pcs.firePropertyChange("progress", old, progress);
    }


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
