package app.controller;

import app.dao.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportsController {

    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        showReportSelection();
    }

    @FXML
    private void showReportSelection() {
        VBox vbox = createReportSelectionView();
        contentPane.getChildren().setAll(vbox);
    }

    private VBox createReportSelectionView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: #f5f7fa;");

        Label title = new Label("Report Generation");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Select a report type to generate");
        subtitle.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        // Report Cards - More Compact
        VBox card1 = createReportCard("📊 Health Service Issuance Report",
                "Total and average health services availed for a given month",
                "#3498db", this::showHealthServiceReport);

        VBox card2 = createReportCard("📦 Medical Supply Issuance Report",
                "Total and average transactions performed on the medical supply inventory",
                "#27ae60", this::showMedicalSupplyReport);

        VBox card3 = createReportCard("👨‍⚕️ Health Personnel Performance Report",
                "Total residents attended to by each health worker per Month/Year",
                "#f39c12", this::showPersonnelPerformanceReport);

        VBox card4 = createReportCard("🏥 Clinic Visits Report",
                "Total number of resident visits per month/year, broken down by service type",
                "#9b59b6", this::showClinicVisitsReport);

        vbox.getChildren().addAll(title, subtitle, card1, card2, card3, card4);
        return vbox;
    }

    private VBox createReportCard(String title, String description, String color, Runnable action) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1); " +
                "-fx-cursor: hand;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblDesc = new Label(description);
        lblDesc.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
        lblDesc.setWrapText(true);

        Button btnGenerate = new Button("Generate Report");
        btnGenerate.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 6; -fx-font-size: 11;");
        btnGenerate.setOnAction(e -> action.run());

        card.getChildren().addAll(lblTitle, lblDesc, btnGenerate);
        return card;
    }

    // Report 1: Health Service Issuance
    private void showHealthServiceReport() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label title = new Label("📊 Health Service Issuance Report");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        ComboBox<Integer> comboMonth = new ComboBox<>();
        comboMonth.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        comboMonth.setPromptText("Select Month");
        comboMonth.setValue(LocalDate.now().getMonthValue());

        ComboBox<Integer> comboYear = new ComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            comboYear.getItems().add(year);
        }
        comboYear.setValue(LocalDate.now().getYear());

        HBox filters = new HBox(10, new Label("Month:"), comboMonth, new Label("Year:"), comboYear);

        Button btnGenerate = new Button("Generate");
        btnGenerate.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        TableView<ReportData> table = new TableView<>();
        setupHealthServiceTable(table);

        TextArea txtSummary = new TextArea();
        txtSummary.setEditable(false);
        txtSummary.setPrefHeight(100);

        Button btnExport = new Button("📄 Export to CSV");
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 20;");
        btnExport.setOnAction(e -> exportToCSV(table, "Health_Service_Report"));

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> showReportSelection());

        btnGenerate.setOnAction(e -> {
            int month = comboMonth.getValue();
            int year = comboYear.getValue();
            generateHealthServiceReport(table, txtSummary, month, year);
        });

        vbox.getChildren().addAll(title, filters, btnGenerate, table,
                new Label("Summary:"), txtSummary,
                new HBox(10, btnExport, btnBack));
        contentPane.getChildren().setAll(vbox);
    }

    private void setupHealthServiceTable(TableView<ReportData> table) {
        TableColumn<ReportData, String> col1 = new TableColumn<>("Service Type");
        col1.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field1));
        col1.setPrefWidth(250);

        TableColumn<ReportData, String> col2 = new TableColumn<>("Total Services");
        col2.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field2));
        col2.setPrefWidth(150);

        TableColumn<ReportData, String> col3 = new TableColumn<>("Avg per Day");
        col3.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field3));
        col3.setPrefWidth(150);

        table.getColumns().addAll(col1, col2, col3);
    }

    private void generateHealthServiceReport(TableView<ReportData> table, TextArea summary, int month, int year) {
        ObservableList<ReportData> data = FXCollections.observableArrayList();

        String sql = """
            SELECT hs.service_type, 
                   COUNT(*) as total,
                   ROUND(COUNT(*) / DAY(LAST_DAY(DATE(?))), 2) as avg_per_day
            FROM ServiceTransactions st
            JOIN HealthServices hs ON st.service_id = hs.service_id
            WHERE MONTH(st.date_provided) = ? AND YEAR(st.date_provided) = ?
            GROUP BY hs.service_type
            ORDER BY total DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, year + "-" + month + "-01");
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();

            int totalServices = 0;
            while (rs.next()) {
                String serviceType = rs.getString("service_type");
                int total = rs.getInt("total");
                double avg = rs.getDouble("avg_per_day");

                data.add(new ReportData(serviceType, String.valueOf(total), String.format("%.2f", avg)));
                totalServices += total;
            }

            table.setItems(data);

            String monthName = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM"));
            summary.setText(String.format("Report Period: %s %d\n" +
                            "Total Services Availed: %d\n" +
                            "Average Services per Day: %.2f\n" +
                            "Number of Service Types: %d",
                    monthName, year, totalServices,
                    (double) totalServices / LocalDate.of(year, month, 1).lengthOfMonth(),
                    data.size()));

        } catch (SQLException e) {
            showError("Error generating report: " + e.getMessage());
        }
    }

    // Report 2: Medical Supply Issuance
    private void showMedicalSupplyReport() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label title = new Label("📦 Medical Supply Issuance Report");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        DatePicker dateFrom = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker dateTo = new DatePicker(LocalDate.now());

        HBox filters = new HBox(10, new Label("From:"), dateFrom, new Label("To:"), dateTo);

        Button btnGenerate = new Button("Generate");
        btnGenerate.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        TableView<ReportData> table = new TableView<>();
        setupMedicalSupplyTable(table);

        TextArea txtSummary = new TextArea();
        txtSummary.setEditable(false);
        txtSummary.setPrefHeight(100);

        Button btnExport = new Button("📄 Export to CSV");
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 20;");
        btnExport.setOnAction(e -> exportToCSV(table, "Medical_Supply_Report"));

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> showReportSelection());

        btnGenerate.setOnAction(e -> generateMedicalSupplyReport(table, txtSummary, dateFrom.getValue(), dateTo.getValue()));

        vbox.getChildren().addAll(title, filters, btnGenerate, table,
                new Label("Summary:"), txtSummary,
                new HBox(10, btnExport, btnBack));
        contentPane.getChildren().setAll(vbox);
    }

    private void setupMedicalSupplyTable(TableView<ReportData> table) {
        table.getColumns().clear();

        TableColumn<ReportData, String> col1 = new TableColumn<>("Item / Type");
        col1.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().field1));
        col1.setPrefWidth(250);

        TableColumn<ReportData, String> col2 = new TableColumn<>("Quantity");
        col2.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().field2));
        col2.setPrefWidth(150);

        TableColumn<ReportData, String> col3 = new TableColumn<>("Transactions");
        col3.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().field3));
        col3.setPrefWidth(150);

        table.getColumns().addAll(col1, col2, col3);
    }

    private void generateMedicalSupplyReport(TableView<ReportData> table, TextArea summary,
                                             LocalDate from, LocalDate to) {

        ObservableList<ReportData> data = FXCollections.observableArrayList();

    /* --------------------------------------------
       1. Get Issuance Summary (from InventoryMovement)
       -------------------------------------------- */
        String sqlIssuance = """
        SELECT ci.item_name,
               SUM(im.quantity) AS total_used,
               COUNT(*) AS num_transactions
        FROM inventorymovement im
        JOIN ClinicInventory ci ON im.item_id = ci.item_id
        WHERE im.movement_type IN ('ISSUE', 'SERVICE')
          AND DATE(im.movement_date) BETWEEN ? AND ?
        GROUP BY ci.item_name
    """;

        int totalIssued = 0;
        int totalIssuedTransactions = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlIssuance)) {

            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("item_name");
                int qty = rs.getInt("total_used");
                int trans = rs.getInt("num_transactions");

                data.add(new ReportData(item, String.valueOf(qty), String.valueOf(trans)));

                totalIssued += qty;
                totalIssuedTransactions += trans;
            }
        } catch (SQLException e) {
            showError("Error loading issuance records: " + e.getMessage());
        }

    /* --------------------------------------------
       2. Get Restock Summary (from InventoryMovement)
       -------------------------------------------- */
        String sqlRestock = """
        SELECT ci.item_name,
               SUM(im.quantity) AS total_added,
               COUNT(*) AS restock_count
        FROM inventorymovement im
        JOIN ClinicInventory ci ON im.item_id = ci.item_id
        WHERE im.movement_type = 'RESTOCK'
          AND DATE(im.movement_date) BETWEEN ? AND ?
        GROUP BY ci.item_name
    """;

        int totalRestocked = 0;
        int totalRestockTransactions = 0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlRestock)) {

            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("item_name");
                int qty = rs.getInt("total_added");
                int trans = rs.getInt("restock_count");

                data.add(new ReportData(item + " (Restock)",   // label clearly
                        String.valueOf(qty),
                        String.valueOf(trans)));

                totalRestocked += qty;
                totalRestockTransactions += trans;
            }
        } catch (SQLException e) {
            showError("Error loading restock records: " + e.getMessage());
        }

    /* --------------------------------------------
       3. Display in Table
       -------------------------------------------- */
        table.setItems(data);

    /* --------------------------------------------
       4. Summary Panel Output
       -------------------------------------------- */
        summary.setText(String.format("""
            Report Period: %s to %s

            TOTAL ISSUED: %d items in %d transactions
            TOTAL RESTOCKED: %d items in %d restock events

            NET MOVEMENT: %d items
            (Positive = Inventory Increased)
        """,
                from, to,
                totalIssued, totalIssuedTransactions,
                totalRestocked, totalRestockTransactions,
                totalRestocked - totalIssued
        ));
    }

    // Report 3: Personnel Performance
    private void showPersonnelPerformanceReport() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label title = new Label("👨‍⚕️ Health Personnel Performance Report");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        ComboBox<Integer> comboMonth = new ComboBox<>();
        comboMonth.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        comboMonth.setPromptText("Select Month");
        comboMonth.setValue(LocalDate.now().getMonthValue());

        ComboBox<Integer> comboYear = new ComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            comboYear.getItems().add(year);
        }
        comboYear.setValue(LocalDate.now().getYear());

        HBox filters = new HBox(10, new Label("Month:"), comboMonth, new Label("Year:"), comboYear);

        Button btnGenerate = new Button("Generate");
        btnGenerate.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        TableView<ReportData> table = new TableView<>();
        setupPersonnelTable(table);

        TextArea txtSummary = new TextArea();
        txtSummary.setEditable(false);
        txtSummary.setPrefHeight(100);

        Button btnExport = new Button("📄 Export to CSV");
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 20;");
        btnExport.setOnAction(e -> exportToCSV(table, "Personnel_Performance_Report"));

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> showReportSelection());

        btnGenerate.setOnAction(e -> generatePersonnelReport(table, txtSummary, comboMonth.getValue(), comboYear.getValue()));

        vbox.getChildren().addAll(title, filters, btnGenerate, table,
                new Label("Summary:"), txtSummary,
                new HBox(10, btnExport, btnBack));
        contentPane.getChildren().setAll(vbox);
    }

    private void setupPersonnelTable(TableView<ReportData> table) {
        TableColumn<ReportData, String> col1 = new TableColumn<>("Personnel Name");
        col1.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field1));
        col1.setPrefWidth(200);

        TableColumn<ReportData, String> col2 = new TableColumn<>("Role");
        col2.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field2));
        col2.setPrefWidth(150);

        TableColumn<ReportData, String> col3 = new TableColumn<>("Residents Attended");
        col3.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field3));
        col3.setPrefWidth(150);

        table.getColumns().addAll(col1, col2, col3);
    }

    private void generatePersonnelReport(TableView<ReportData> table, TextArea summary, int month, int year) {
        ObservableList<ReportData> data = FXCollections.observableArrayList();

        String sql = """
            SELECT hp.first_name, hp.last_name, hp.role,
                   COUNT(DISTINCT st.resident_id) as residents_attended
            FROM HealthPersonnel hp
            LEFT JOIN ServiceTransactions st ON hp.personnel_id = st.personnel_id
                AND MONTH(st.date_provided) = ? AND YEAR(st.date_provided) = ?
            GROUP BY hp.personnel_id, hp.first_name, hp.last_name, hp.role
            ORDER BY residents_attended DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();

            int totalResidents = 0;
            while (rs.next()) {
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                String role = rs.getString("role");
                int attended = rs.getInt("residents_attended");

                data.add(new ReportData(name, role, String.valueOf(attended)));
                totalResidents += attended;
            }

            table.setItems(data);

            String monthName = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM"));
            summary.setText(String.format("Report Period: %s %d\n" +
                            "Total Personnel: %d\n" +
                            "Total Unique Residents Attended: %d\n" +
                            "Average Residents per Personnel: %.2f",
                    monthName, year, data.size(), totalResidents,
                    data.size() > 0 ? (double) totalResidents / data.size() : 0));

        } catch (SQLException e) {
            showError("Error generating report: " + e.getMessage());
        }
    }

    // Report 4: Clinic Visits
    private void showClinicVisitsReport() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label title = new Label("🏥 Clinic Visits Report");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        ComboBox<Integer> comboMonth = new ComboBox<>();
        comboMonth.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        comboMonth.setPromptText("Select Month");
        comboMonth.setValue(LocalDate.now().getMonthValue());

        ComboBox<Integer> comboYear = new ComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            comboYear.getItems().add(year);
        }
        comboYear.setValue(LocalDate.now().getYear());

        HBox filters = new HBox(10, new Label("Month:"), comboMonth, new Label("Year:"), comboYear);

        Button btnGenerate = new Button("Generate");
        btnGenerate.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        TableView<ReportData> table = new TableView<>();
        setupClinicVisitsTable(table);

        TextArea txtSummary = new TextArea();
        txtSummary.setEditable(false);
        txtSummary.setPrefHeight(100);

        Button btnExport = new Button("📄 Export to CSV");
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 20;");
        btnExport.setOnAction(e -> exportToCSV(table, "Clinic_Visits_Report"));

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> showReportSelection());

        btnGenerate.setOnAction(e -> generateClinicVisitsReport(table, txtSummary, comboMonth.getValue(), comboYear.getValue()));

        vbox.getChildren().addAll(title, filters, btnGenerate, table,
                new Label("Summary:"), txtSummary,
                new HBox(10, btnExport, btnBack));
        contentPane.getChildren().setAll(vbox);
    }

    private void setupClinicVisitsTable(TableView<ReportData> table) {
        TableColumn<ReportData, String> col1 = new TableColumn<>("Service Type");
        col1.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field1));
        col1.setPrefWidth(250);

        TableColumn<ReportData, String> col2 = new TableColumn<>("Total Visits");
        col2.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field2));
        col2.setPrefWidth(150);

        TableColumn<ReportData, String> col3 = new TableColumn<>("Percentage");
        col3.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().field3));
        col3.setPrefWidth(150);

        table.getColumns().addAll(col1, col2, col3);
    }

    private void generateClinicVisitsReport(TableView<ReportData> table, TextArea summary, int month, int year) {
        ObservableList<ReportData> data = FXCollections.observableArrayList();

        String sql = """
            SELECT hs.service_type, COUNT(*) as visit_count
            FROM ServiceTransactions st
            JOIN HealthServices hs ON st.service_id = hs.service_id
            WHERE MONTH(st.date_provided) = ? AND YEAR(st.date_provided) = ?
            GROUP BY hs.service_type
            ORDER BY visit_count DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();

            int totalVisits = 0;
            ObservableList<ReportData> tempData = FXCollections.observableArrayList();

            while (rs.next()) {
                String serviceType = rs.getString("service_type");
                int visits = rs.getInt("visit_count");
                tempData.add(new ReportData(serviceType, String.valueOf(visits), ""));
                totalVisits += visits;
            }

            // Calculate percentages
            for (ReportData rd : tempData) {
                int visits = Integer.parseInt(rd.field2);
                double percentage = (double) visits / totalVisits * 100;
                rd.field3 = String.format("%.1f%%", percentage);
                data.add(rd);
            }

            table.setItems(data);

            String monthName = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM"));
            summary.setText(String.format("Report Period: %s %d\n" +
                            "Total Clinic Visits: %d\n" +
                            "Service Types Offered: %d\n" +
                            "Average Visits per Day: %.2f",
                    monthName, year, totalVisits, data.size(),
                    (double) totalVisits / LocalDate.of(year, month, 1).lengthOfMonth()));

        } catch (SQLException e) {
            showError("Error generating report: " + e.getMessage());
        }
    }

    private void exportToCSV(TableView<ReportData> table, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(defaultFileName + "_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(contentPane.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write headers
                for (TableColumn<ReportData, ?> column : table.getColumns()) {
                    writer.append(column.getText()).append(",");
                }
                writer.append("\n");

                // Write data
                for (ReportData row : table.getItems()) {
                    writer.append(row.field1).append(",")
                            .append(row.field2).append(",")
                            .append(row.field3).append("\n");
                }

                showSuccess("Report exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Error exporting report: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for report data
    public static class ReportData {
        public String field1, field2, field3;

        public ReportData(String field1, String field2, String field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
    }
}