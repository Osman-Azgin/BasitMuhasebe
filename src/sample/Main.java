package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main extends Application {
    private static String[] arguments;
    Parent root;
    public Rectangle2D bounds;
    public static ArrayList<Record> data;
    public static int selectedIndex=-1;
    public static String today=new java.text.SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
    public static String fileNAme="";
    public static boolean notSaved=false;
    public static Stage PStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Screen screen = Screen.getPrimary();
        bounds = screen.getVisualBounds();
        root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        data=new ArrayList<Record>();
        primaryStage.setTitle("Basit Muassebe");
        primaryStage.setScene(new Scene(root, bounds.getWidth(), bounds.getHeight()));
        primaryStage.show();
        BorderPane border = (BorderPane) root.lookup("#BP");
        HBox hBox=initHeader();
        border.setTop(hBox);
        TableView dataTable=initDataTable();
        border.setCenter(dataTable);
        border.setBottom(initFooter());

        PStage=primaryStage;

        if (arguments.length>0){
            fileNAme=arguments[0];
        }

        if(fileNAme.length()>0){
            primaryStage.setTitle(fileNAme+" Basit Muassebe");
            initRecords();
        }

        initSaved();
        initFilters();
    }


    public static void main(String[] args) {
        arguments=args;
        launch(args);
    }

    public HBox initHeader() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color:linear-gradient(#FFF 0%, #FBFBFB 10%, #D9D9D9 100%);");

        Button buttonYeniHesapDefteri = new Button("New Ledger");
        buttonYeniHesapDefteri.setPrefSize(150, 20);
        buttonYeniHesapDefteri.setId("yeni-hesap-defteri");

        Button buttonHesapDefteriAc = new Button("Open a Ledger");
        buttonHesapDefteriAc.setPrefSize(150, 20);
        buttonHesapDefteriAc.setId("hesap-defteri-ac");

        Button buttonKaydet = new Button("Save");
        buttonKaydet.setPrefSize(100, 20);
        buttonKaydet.setId("kaydet");
        buttonKaydet.setDisable(true);

        Button buttonGirdiEkle = new Button("Add Gain");
        buttonGirdiEkle.setPrefSize(100, 20);
        buttonGirdiEkle.setId("girdi-ekle");

        Button buttonCiktiEkle = new Button("Add Spend");
        buttonCiktiEkle.setPrefSize(100, 20);
        buttonCiktiEkle.setId("cikti-ekle");

        Button buttonKayitSil = new Button("Delete Record");
        buttonKayitSil.setPrefSize(100, 20);
        buttonKayitSil.setId("kayit-sil");

        Button buttonAnalizEt = new Button("Spending Analysis");
        buttonAnalizEt.setPrefSize(150, 20);
        buttonAnalizEt.setId("analiz-et");

        Button buttonTasarrufAnalizi = new Button("Saving Analysis");
        buttonTasarrufAnalizi.setPrefSize(150, 20);
        buttonTasarrufAnalizi.setId("tasarruf-analizi-yap");


        Label label = new Label("|");
        label.setStyle("-fx-color-label-visible: #D9D9D9;-fx-padding: 3px;");

        Label label2 = new Label("|");
        label2.setStyle("-fx-color-label-visible: #D9D9D9;-fx-padding: 3px;");

        buttonYeniHesapDefteri.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Runtime.getRuntime().exec("java -jar BM.jar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonKaydet.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    saveRecords();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonGirdiEkle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addRecord("Gain",null,null,null);
            }
        });

        buttonCiktiEkle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addRecord("Spend",null,null,null);
            }
        });

        buttonKayitSil.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteRecord(selectedIndex);
                selectedIndex=-1;
            }
        });

        buttonAnalizEt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Analysis();
            }
        });

        buttonTasarrufAnalizi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                savingAnalysis();
            }
        });

        buttonHesapDefteriAc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select a Ledger");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Text Files", "*.bm"));
                File selectedFile = fileChooser.showOpenDialog(PStage);

                if (selectedFile != null) {
                    fileNAme=selectedFile.getAbsolutePath();
                    initRecords();
                    notSaved=false;
                    initSaved();
                }
            }
        });

        hbox.getChildren().addAll(buttonYeniHesapDefteri, buttonHesapDefteriAc, buttonKaydet, label, buttonGirdiEkle, buttonCiktiEkle, buttonKayitSil, label2, buttonAnalizEt, buttonTasarrufAnalizi);

        return hbox;
    }

    public TableView initDataTable() {
        TableView dataTable = new TableView();
        dataTable.setPrefWidth(bounds.getWidth());

        TableColumn title=new TableColumn("Definition");
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        title.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
        title.setCellFactory(TextFieldTableCell.forTableColumn());
        title.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent t) {
                ((Record) t.getTableView().getItems().get(t.getTablePosition().getRow())).setTitle((String) t.getNewValue());
                SortData();
                updateFooter();
                selectedIndex=t.getTablePosition().getRow();
                notSaved=true;
                initSaved();
            }
        });

        TableColumn desc=new TableColumn("Description");
        desc.setCellValueFactory(new PropertyValueFactory<>("desc"));
        desc.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.3));
        desc.setCellFactory(TextFieldTableCell.forTableColumn());
        desc.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent t) {
                ((Record) t.getTableView().getItems().get(t.getTablePosition().getRow())).setDesc((String) t.getNewValue());
                SortData();
                updateFooter();
                selectedIndex=t.getTablePosition().getRow();
                notSaved=true;
                initSaved();
            }
        });

        TableColumn amount=new TableColumn("Amount");
        amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amount.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
        amount.setCellFactory(TextFieldTableCell.forTableColumn());
        amount.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent t) {
                ((Record) t.getTableView().getItems().get(t.getTablePosition().getRow())).setAmount((isNumeric((String) t.getNewValue()))?(String) t.getNewValue():"0");
                SortData();
                updateFooter();
                selectedIndex=t.getTablePosition().getRow();
                notSaved=true;
                initSaved();
            }
        });

        TableColumn type=new TableColumn("Gain/Spend");
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        type.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.1));

        TableColumn date=new TableColumn("Record Date");
        date.setCellValueFactory(new PropertyValueFactory<>("recordDate"));
        date.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
        date.setCellFactory(TextFieldTableCell.forTableColumn());
        date.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent t) {
                try {
                    ((Record) t.getTableView().getItems().get(t.getTablePosition().getRow())).setRecordDate(new SimpleDateFormat("dd/MM/yyyy").parse((String) t.getNewValue()) );
                    SortData();
                    updateFooter();
                    selectedIndex=t.getTablePosition().getRow();
                    notSaved=true;
                    initSaved();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        dataTable.setRowFactory( tv -> {
            TableRow<Record> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                selectedIndex=row.getIndex();
                root.lookup("#kayit-sil").setDisable(false);
            });
            return row ;
        });

        dataTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode()==KeyCode.DELETE) {
                    deleteRecord(selectedIndex);
                    selectedIndex = -1;
                }
            }
        });

        dataTable.setId("kayit-tablosu");
        dataTable.getColumns().addAll(type, title, desc, amount, date);
        dataTable.setEditable(true);
        return dataTable;
    }

    public HBox initFooter() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color:linear-gradient(#FFF 0%, #FBFBFB 10%, #D9D9D9 100%);");

        TextField Bilgi=new TextField("Thera are bla bla bla days end of mount. Total Gain ,Total Spend. far gain/spend.");
        Bilgi.setPrefWidth(((bounds.getWidth())/3)*2);
        Bilgi.setId("bilgi");

        Label label = new Label("|");
        label.setStyle("-fx-color-label-visible: #D9D9D9;-fx-padding: 3px;");

        Label label2 = new Label("Filter:");
        label2.setStyle("-fx-color-label-visible: #D9D9D9;-fx-padding: 3px;-fx-min-width: 70px;");

        ArrayList<String> titles = getTitiles();
        titles.add("All Definitions");


        ComboBox titleComboBox = new ComboBox(FXCollections.observableArrayList(titles));
        titleComboBox.setStyle("-fx-min-width: 150px");
        titleComboBox.setId("filter-titles");

        titleComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                applyFilters();
            }
        });


        ArrayList<String> mounts = getMounts();
        mounts.add("All Mounts");

        ComboBox mountComboBox = new ComboBox(FXCollections.observableArrayList(mounts));
        mountComboBox.setStyle("-fx-min-width: 150px");
        mountComboBox.setId("filter-mounts");
        mountComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                applyFilters();
            }
        });

        hbox.getChildren().addAll(Bilgi,label,label2,titleComboBox,mountComboBox);

        return hbox;
    }

    public void updateFooter() {
        try {
            int topAvard = 0, topSpend = 0, saving;
            String Today=today.substring(0,2);
            int dInM = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
            int day = dInM - new Integer(Today);

            for (Record record : data) {
                if (record.getType().equals("Gain")) {
                    topAvard += Double.parseDouble(record.getAmount());
                } else {
                    topSpend += Double.parseDouble(record.getAmount());
                }
            }
            saving = topAvard - topSpend;
            TextField bilig = (TextField) root.lookup("#bilgi");
            bilig.setText("There Are " + day + " days end of mount. Total Gain : " + topAvard + ", Total Spend : " + topSpend + ", Remaining : " + saving);

            ArrayList<String> titles = getTitiles();
            titles.add("All Definitions");

            ArrayList<String> mounts = getMounts();
            mounts.add("All Mounts");

            ComboBox title=(ComboBox) root.lookup("#filter-titles");
            ComboBox mount=(ComboBox) root.lookup("#filter-mounts");

            title.getItems().removeAll();
            title.setItems(FXCollections.observableArrayList(titles));

            mount.getItems().removeAll();
            mount.setItems(FXCollections.observableArrayList(mounts));

            applyFilters();

        }catch (Exception E){}
    }

    public void initRecords(){
        try {
            data.removeAll(data);
            byte[] encoded = Files.readAllBytes(Paths.get(fileNAme));
            String content=new String(encoded);
            int syc=0;
            for (int i = -1; (i = content.indexOf("<record>", i + 1)) != -1; ) {
                int start=i+8,length=content.indexOf("</record>",i);
                String rec=content.substring(start,length);
                String type=rec.substring(rec.indexOf("<type>")+6,rec.indexOf("</type>"));
                String title=rec.substring(rec.indexOf("<title>")+7,rec.indexOf("</title>"));
                String desc=rec.substring(rec.indexOf("<desc>")+6,rec.indexOf("</desc>"));
                String amount=rec.substring(rec.indexOf("<amount>")+8,rec.indexOf("</amount>"));
                String recordDate=rec.substring(rec.indexOf("<record-date>")+13,rec.indexOf("</record-date>"));
                type=(type.equals("Çıktı") || type.equals("Spend")) ? "Spend" : type;
                type=(type.equals("Girdi") || type.equals("Gain")) ? "Gain" : type;
                data.add(new Record(type,title,desc,amount,new Date(recordDate)));
                syc++;
            }
            TableView dataTable= (TableView) root.lookup("#kayit-tablosu");
            SortData();
            dataTable.setItems(FXCollections.observableList(data));
            updateFooter();
            initFilters();
            dataTable.setEditable(true);
            System.out.print(syc+" Records Found");
        }catch (IOException E){
            System.out.print("The File is Unable to Open");
        }
    }

    public void initSaved(){
        Button bt=(Button) root.lookup("#kaydet");
        if(notSaved==true){
            PStage.setTitle("*"+fileNAme+" Basit Muassebe");
            bt.setDisable(false);
        }else{
            PStage.setTitle(fileNAme+" Basit Muassebe");
            bt.setDisable(true);
        }
    }

    public void saveRecords() throws FileNotFoundException, UnsupportedEncodingException {
        if(fileNAme.length()<1){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Hesap Defterini Kaydet");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.bm"));
            File selectedFile = fileChooser.showSaveDialog(PStage);
            if (selectedFile != null) {
                fileNAme=selectedFile.getAbsolutePath();
            }
        }
        if(fileNAme.length()>0) {
            PrintWriter writer = new PrintWriter(fileNAme, "UTF-8");
            writer.println("<?xml version=\"1.0\"?>");
            writer.println("<records>");
            for (Record record : data) {
                writer.println("        <record>");
                writer.println("                <type>" + record.getType() + "</type>");
                writer.println("                <title>" + record.getTitle() + "</title>");
                writer.println("                <desc>" + record.getDesc() + "</desc>");
                writer.println("                <amount>" + record.getAmount() + "</amount>");
                writer.println("                <record-date>" + record.getRecordDateSaveFormat() + "</record-date>");
                writer.println("        </record>");
            }
            writer.println("</records>");
            writer.close();
            notSaved = false;
            initSaved();
        }
    }

    public void addRecord(String Type, String Title, String Desc, String Amount){
        String title = (Title != null) ? Title : "Tanım";
        String desc = (Desc != null) ? Desc : "Açıklama";
        String amount = (Amount != null) ? Amount : "0";
        TableView dataTable= (TableView) root.lookup("#kayit-tablosu");
        data.add(new Record(Type, title, desc, amount, Calendar.getInstance().getTime()));
        selectedIndex=data.size()-1;
        SortData();
        dataTable.setItems(FXCollections.observableList(data));
        updateFooter();
        dataTable.setEditable(true);
        notSaved=true;
        initSaved();
    }

    public void deleteRecord(int index){
        if(new Integer(-1).equals(index)==false) {
            TableView dataTable = (TableView) root.lookup("#kayit-tablosu");
             data.remove(dataTable.getItems().get(index));
            dataTable.setItems(FXCollections.observableList(data));
            root.lookup("#kayit-sil").setDisable(true);
            SortData();
            updateFooter();
            notSaved=true;
            initSaved();
        }
    }

    public void initFilters(){
        ComboBox titles=(ComboBox) root.lookup("#filter-titles");
        ComboBox mounts=(ComboBox) root.lookup("#filter-mounts");
        if(titles.getValue()==null){
            titles.getSelectionModel().select("All Definitions");
        }
        if(mounts.getValue()==null) {
            mounts.getSelectionModel().select("All Mounts");
        }
    }

    public void applyFilters(){
        ArrayList<Record> newData=filter(data, new filterHandler() {
            @Override
            public boolean handle(Record fil) {
                ComboBox titles=(ComboBox) root.lookup("#filter-titles");
                ComboBox mounts=(ComboBox) root.lookup("#filter-mounts");
                String title=(titles.getValue()!=null && titles.getValue().equals("All Definitions")!=true) ? (String) titles.getValue() : null;
                String mount=(mounts.getValue()!=null && mounts.getValue().equals("All Mounts")!=true) ? (String) mounts.getValue(): null;
                boolean retval=true;
                if((title!=null && fil.getTitle().equals(title)!=true) || (mount!=null && fil.getRecordDate().substring(3,fil.getRecordDate().length()).equals(mount)!=true)){
                    retval=false;
                }
                return retval;
            }
        });
        TableView dataTable=(TableView) root.lookup("#kayit-tablosu");
        dataTable.setItems(FXCollections.observableList(newData));
    }

    public void Analysis(){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Analysis Results");
        dialog.setHeaderText("Results Of Spending Analysis");
        ButtonType cancelButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        grid.setPrefWidth(bounds.getWidth()/2);

        TableView resultTable = new TableView();
        resultTable.setPrefWidth(grid.getPrefWidth());
        TableColumn title=new TableColumn("Definition");
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        title.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

        TableColumn thisMount =new TableColumn("This Mount");
        thisMount.setCellValueFactory(new PropertyValueFactory<>("thisMount"));
        thisMount.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

        TableColumn total =new TableColumn("All Mounts");
        total.setCellValueFactory(new PropertyValueFactory<>("total"));
        total.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

        TableColumn average =new TableColumn("Average");
        average.setCellValueFactory(new PropertyValueFactory<>("average"));
        average.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

        resultTable.getColumns().addAll(title, thisMount, total, average);

        resultTable.setItems(FXCollections.observableList(doAnalysis()));

        grid.add(resultTable, 0, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.show();
    }

    public void savingAnalysis(){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Analysis Results");
        dialog.setHeaderText("Results Of Saving Analysis");
        ButtonType cancelButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        grid.setPrefWidth(bounds.getWidth()/2);

        TableView resultTable = new TableView();
        resultTable.setPrefWidth(grid.getPrefWidth());

        TableColumn mount=new TableColumn("Mounth");
        mount.setCellValueFactory(new PropertyValueFactory<>("mount"));
        mount.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

        TableColumn avard=new TableColumn("Total Gain");
        avard.setCellValueFactory(new PropertyValueFactory<>("avard"));
        avard.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

        TableColumn spend=new TableColumn("Total Spend");
        spend.setCellValueFactory(new PropertyValueFactory<>("spend"));
        spend.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));

            TableColumn saving =new TableColumn("Saving");
        saving.setCellValueFactory(new PropertyValueFactory<>("saving"));
        saving.prefWidthProperty().bind(resultTable.widthProperty().multiply(0.25));


        resultTable.getColumns().addAll(mount, avard, spend, saving);

        resultTable.setItems(FXCollections.observableList(doSavingAnalysis()));

        grid.add(resultTable, 0, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.show();
    }


    public static void SortData(){
        Collections.sort(data, new Comparator<Record>(){
            public int compare(Record o1, Record o2){
                if(o1.getDate() == o2.getDate())
                    return 0;
                return o2.getDate().after(o1.getDate()) ? -1 : 1;
            }
        });
    }

    public static ArrayList getMounts(){
        ArrayList<String> mounts=new ArrayList<>();
        try{
            for(int i=0;i<=data.size();i++){
                if(mounts.contains(data.get(i).getRecordDate().substring(3,data.get(i).getRecordDate().length()))==false){
                    mounts.add(data.get(i).getRecordDate().substring(3,data.get(i).getRecordDate().length()));
                }
            }
        }catch (Exception E){}
        return mounts;
    }

    public static ArrayList getTitiles(){
        ArrayList<String> titles=new ArrayList<>();
        try{
            for(int i=0;i<=data.size();i++){
                if(titles.contains(data.get(i).getTitle())==false){
                    titles.add(data.get(i).getTitle());
                }
            }
        }catch (Exception E){}
        return titles;
    }

    public static ArrayList getTitilesInDada(ArrayList<Record> dat){
        ArrayList<String> titles=new ArrayList<>();
        try{
            for(int i=0;i<=dat.size();i++){
                if(titles.contains(dat.get(i).getTitle())==false){
                    titles.add(dat.get(i).getTitle());
                }
            }
        }catch (Exception E){}
        return titles;
    }

    public static ArrayList<Record> filter(ArrayList<Record> filt,filterHandler handler){
        ArrayList Result = new ArrayList();
        for (Record fil:filt) {
            if(handler.handle(fil)==true){
                Result.add(fil);
            }
        }
        return Result;
    }


    public static ArrayList doAnalysis(){
        ArrayList<analyzeResult> Results=new ArrayList<>();
        ArrayList<String> mounts=getMounts();
        ArrayList<String> titles=(ArrayList<String>) getTitilesInDada(filter(data, new filterHandler() {
            @Override
            public boolean handle(Record fil) {
                if(fil.getType().equals("Çıktı") || fil.getType().equals("Spend")){
                    return true;
                }else {
                    return false;
                }
            }
        }));
        for (String title : titles) {
            double total=0;
            double thisMount=0;
            double Average=0;

            String tM=today.substring(3,today.length());

            ArrayList<Record> titleistitle=filter(data, new filterHandler(){
                @Override
                public boolean handle(Record fil) {
                    if(fil.getTitle().equals(title) && (fil.getType().equals("Çıktı")==true || fil.getType().equals("Spend")==true)){
                        return true;
                    }else {
                        return false;
                    }
                }
            });
            for(Record rec: titleistitle){
                total+=Double.parseDouble(rec.getAmount());
            }
            ArrayList<Record> tMount=filter(data, new filterHandler(){
                @Override
                public boolean handle(Record fil) {
                    if(fil.getRecordDate().substring(3,fil.getRecordDate().length()).equals(tM) && fil.getTitle().equals(title) && (fil.getType().equals("Çıktı")==true || fil.getType().equals("Spend")==true)){
                        return true;
                    }else {
                        return false;
                    }
                }
            });

            for (Record rec: tMount){
                thisMount+=Double.parseDouble(rec.getAmount());
            }

            Average=total/mounts.size();

            Results.add(new analyzeResult(title,Double.toString(thisMount),Double.toString(total),Double.toString(Average)));
        }
        return Results;
    }

    public ArrayList doSavingAnalysis(){
        ArrayList<savingAnalysisResult> Results=new ArrayList<>();
        ArrayList<String> mounts=getMounts();
        int savingFromLastMont=0;
        for(String mount:mounts){
            int avard=0,spend=0,saving=0;
            ArrayList<Record> recordsOfMount = filter(data, new filterHandler() {
                @Override
                public boolean handle(Record fil) {
                    if(fil.getRecordDate().substring(3,fil.getRecordDate().length()).equals(mount)){
                        return true;
                    }else{
                        return false;
                    }
                }
            });

            for(Record record:recordsOfMount){
                if(record.getType().equals("Girdi") || record.getType().equals("Gain")){
                    avard+=Double.parseDouble(record.getAmount());
                }else if(record.getType().equals("Çıktı") || record.getType().equals("Spend")){
                    spend+=Double.parseDouble(record.getAmount());
                }
            }

            saving=avard-spend;

            saving+=savingFromLastMont;

            savingFromLastMont=saving;

            Results.add(new savingAnalysisResult(mount,String.valueOf(saving),String.valueOf(avard),String.valueOf(spend)));
        }
        return Results;
    }


    public class Record{
        private String Type,Title,Desc,Amount;
        private Date recordDate;

        public Record(String Type, String Title, String Desc, String Amount, Date date){
            this.Type=Type;
            this.Title=Title;
            this.Desc=Desc;
            this.Amount=Amount;
            this.recordDate=date;
        }

        public String getType() {
            return Type;
        }

        public String getTitle() {
            return Title;
        }

        public String getDesc() {
            return Desc;
        }

        public String getAmount() { return Amount; }

        public String getRecordDate() { return new java.text.SimpleDateFormat("dd/MM/yyyy").format(recordDate); }

        public String getRecordDateSaveFormat() { return new java.text.SimpleDateFormat("MM/dd/yyyy").format(recordDate); }

        public Date getDate() { return recordDate; }

        public void setType(String type) {
            Type = type;
        }

        public void setTitle(String title) {
            Title = title;
        }

        public void setDesc(String desc) {
            Desc = desc;
        }

        public void setAmount(String amount) {
            Amount = amount;
        }

        public void setRecordDate(Date date){ recordDate=date; }
    }

    public static class analyzeResult{
        private String Title,ThisMount,Total,Average;

        public analyzeResult(String title, String thisMount,String total, String average){
            Title=title;
            ThisMount=thisMount;
            Total=total;
            Average=average;
        }

        public String getTitle(){
            return Title;
        }

        public String getThisMount(){
            return ThisMount;
        }

        public String getTotal(){
            return Total;
        }

        public String getAverage(){
            return Average;
        }

        public void setTitle(String title){
            Title=title;
        }

        public void setThisMount(String thisMount){
            ThisMount=thisMount;
        }

        public void setTotal(String total){
            Total=total;
        }

        public void setAverage(String average){
            Average=average;
        }

    }

    public class savingAnalysisResult{
        private String Mount,Saving,Avard,Spend;

        public savingAnalysisResult(String mount,String saving,String avard,String spend){
            Mount=mount;
            Saving=saving;
            Avard=avard;
            Spend=spend;
        }

        public String getMount(){
            return Mount;
        }

        public String getSaving(){
            return Saving;
        }

        public String getAvard(){
            return Avard;
        }

        public String getSpend(){
            return Spend;
        }

        public void setMount(String mount){
            Mount=mount;
        }

        public void setSaving(String saving){
            Saving=saving;
        }
    }

    public static abstract class filterHandler{

        public boolean handle(Record fil){
            return false;
        }
    }

    public static boolean isNumeric(String str){
        try {
            double d=Double.parseDouble(str);
        }catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }
}
