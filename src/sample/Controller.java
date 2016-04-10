package sample;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.management.StringValueExp;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class Controller implements Initializable{

    @FXML TableView <ImageProcessingJob> tableView;
    @FXML TableColumn<ImageProcessingJob, String> imageNameColumn;
    @FXML TableColumn<ImageProcessingJob, Double> progressColumn;
    @FXML TableColumn<ImageProcessingJob, String> statusColumn;
    @FXML Label timeLabel;
    @FXML Label outputLabel;
    @FXML ChoiceBox<String> kindsChoiceBox;
    @FXML TextField threadsField;
    private ObservableList<ImageProcessingJob> images = FXCollections.observableArrayList();
    private ObservableList<String> kinds = FXCollections.observableArrayList();
    private File outputDirectory;
    private int threads;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageNameColumn.setCellValueFactory(
                p->new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory(
                p->p.getValue().statusProperty());
        progressColumn.setCellValueFactory(
                p->p.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(
                ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        threads = 2;
        kinds.addAll("Sekwencyjnie", "Rownolegle");
        kindsChoiceBox.setItems(kinds);
        kindsChoiceBox.setValue("Sekwencyjnie");
        outputDirectory = new File("C:/");
        outputLabel.textProperty().setValue("Output directory: "+outputDirectory.getAbsolutePath());
    }

    @FXML
    public void loadImages() {
        images.clear();
        tableView.getItems().clear();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter =
                new FileChooser.ExtensionFilter("JPG images", "*.jpg");
        fileChooser.getExtensionFilters().add(filter);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null){
            for (File f:selectedFiles){
                images.add(new ImageProcessingJob(f, "Oczekiwanie", 0.0));
            }
            tableView.getItems().addAll(images);
        }
    }

    @FXML public void chooseOutputDirectory(){
        DirectoryChooser dc = new DirectoryChooser();
        outputDirectory = dc.showDialog(null);
        if (outputDirectory != null){
            outputLabel.textProperty().setValue("Output directory: "+outputDirectory.getAbsolutePath());
        }
    }


    @FXML
    public void process() {
        if (kindsChoiceBox.getValue() == "Sekwencyjnie")
            new Thread(this::backgroundJob).start();
        else if (kindsChoiceBox.getValue() == "Rownolegle")
        {
            if (threadsField .textProperty().getValue() != null)
                threads = Integer.parseInt(threadsField.textProperty().getValue());
            ForkJoinPool pool = new ForkJoinPool(threads);
            pool.submit(this::backgroundJobParallel);
        }
    }

    private void backgroundJob() {
        long start = System.currentTimeMillis();
        Stream<ImageProcessingJob> s;
        s = tableView.getItems().stream();
        s.forEach(ipj ->{
            if (ipj.getStatus() != "zakonczono")
                convertToGrayscale(ipj.getFile(), outputDirectory, ipj);
        });
        long stop = System.currentTimeMillis();
        long timeElapsed = (stop - start);
        long sec = TimeUnit.MILLISECONDS.toSeconds(timeElapsed);
        long mil = timeElapsed - TimeUnit.SECONDS.toMillis(sec);
        Platform.runLater(() ->timeLabel.setText("Time elapsed: "
                +String.valueOf(sec)+","+ String.valueOf(mil)+"s"));
    }


    private void backgroundJobParallel() {
        long start = System.currentTimeMillis();
        Stream<ImageProcessingJob> s;
        s = tableView.getItems().parallelStream();
        s.forEach(ipj ->{
            if (ipj.getStatus() != "zakonczono")
                convertToGrayscale(ipj.getFile(), outputDirectory, ipj);
        });
        long stop = System.currentTimeMillis();
        long timeElapsed = (stop - start);
        long sec = TimeUnit.MILLISECONDS.toSeconds(timeElapsed);
        long mil = timeElapsed - TimeUnit.SECONDS.toMillis(sec);
        Platform.runLater(() ->timeLabel.setText("Time elapsed: "
                +String.valueOf(sec)+","+ String.valueOf(mil)+"s"));
    }




    private void convertToGrayscale(File originalFile, File outputDir, ImageProcessingJob ipj){
        try{
            BufferedImage original = ImageIO.read(originalFile);
            BufferedImage grayscale = new BufferedImage(
                    original.getWidth(), original.getHeight(), original.getType());

            for (int i=0; i<original.getWidth(); i++){
                for (int j=0;j<original.getHeight(); j++){
                    int red = new Color(original.getRGB(i,j)).getRed();
                    int green = new Color(original.getRGB(i,j)).getGreen();
                    int blue = new Color(original.getRGB(i,j)).getBlue();

                    int luminosity = (int) (0.21*red+0.71*green+0.07*blue);

                    int newPixel =
                            new Color(luminosity, luminosity, luminosity).getRGB();

                    grayscale.setRGB(i,j,newPixel);
                }
                double progress = (1.0+i)/original.getWidth();
                Platform.runLater(()->ipj.setProgress(progress));
            }

            Path outputPath =
                    Paths.get(outputDir.getAbsolutePath(), "gray_scale"+originalFile.getName());
            ImageIO.write(grayscale, "jpg", outputPath.toFile());

        }
        catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @FXML
    public void clearProgress(){
        for (ImageProcessingJob image: images) {
            image.setProgress(0.0);
            image.setStatus("Oczekiwanie");
        }
        tableView.getItems().clear();
        tableView.getItems().addAll(images);

    }


}
