package org.example.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.ShoppingCartCalculator;
import org.example.model.Item;
import org.example.service.LocalizationService;
import org.example.service.CartService;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

public class MainController {

    @FXML private ComboBox<LangOption> languageBox;
    @FXML private Label lblLanguage;
    @FXML private Label lblNumItems;
    @FXML private TextField numItemsField;
    @FXML private Button btnGenerate;
    @FXML private VBox itemsBox;
    @FXML private Button btnCalculate;
    @FXML private Label lblOverallTotal;

    private StageAware stageAware = new StageAware();
    private Locale currentLocale = Locale.US;
    private final LocalizationService localizationService = new LocalizationService();
    private Map<String,String> i18n = new HashMap<>();
    private final CartService cartService = new CartService();

    void setStage(javafx.stage.Stage stage) { this.stageAware.stage = stage; }
    void setInitialLocale(Locale locale) {
        this.currentLocale = locale;
        this.i18n = localizationService.loadStrings(currentLocale);
        applyI18n();
    }

    @FXML
    public void initialize() {
        
        languageBox.setItems(FXCollections.observableArrayList(
                new LangOption("English", Locale.US),
                new LangOption("Suomi", new Locale("fi", "FI")),
                new LangOption("Svenska", new Locale("sv", "SE")),
                new LangOption("日本語", new Locale("ja", "JP"))
        ));
        languageBox.getSelectionModel().selectFirst();
        languageBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                switchLocale(newV.locale());
            }
        });

        btnGenerate.setOnAction(e -> generateItemRows());
        btnCalculate.setOnAction(e -> calculateTotals());

        applyI18n();
    }

    private void switchLocale(Locale locale) {
        this.currentLocale = locale;
        System.out.println(this.currentLocale);
        this.i18n = localizationService.loadStrings(currentLocale);
        applyI18n();
    }

    private void applyI18n() {
        lblLanguage.setText(s("gui.language", "Language"));
        lblNumItems.setText(s("gui.numItems", "Number of items"));
        btnGenerate.setText(s("gui.generate", "Generate"));
        btnCalculate.setText(s("gui.calculate", "Calculate"));
        if (stageAware.stage != null) {
            stageAware.stage.setTitle(s("gui.title", "Shopping Cart"));
        }
        
        refreshItemRowLabels();
        
        if (lblOverallTotal.getUserData() instanceof Number n) {
            setOverallTotal(n.doubleValue());
        } else {
            lblOverallTotal.setText(s("gui.total", "Total") + ": 0.00");
        }
    }

    private void generateItemRows() {
        int count = parseIntSafe(numItemsField.getText(), 0);
        itemsBox.getChildren().clear();
        for (int i = 1; i <= count; i++) {
            itemsBox.getChildren().add(buildItemRow(i));
        }
    }

    private Node buildItemRow(int index) {
        Label priceLbl = new Label(MessageFormat.format(s("prompt.itemPrice", "Price for item {0}"), index));
        TextField priceField = new TextField();
        priceField.setPromptText(s("gui.price", "Price"));
        Label qtyLbl = new Label(MessageFormat.format(s("prompt.itemQty", "Quantity for item {0}"), index));
        TextField qtyField = new TextField();
        qtyField.setPromptText(s("gui.quantity", "Quantity"));
        Label itemTotalLbl = new Label(s("gui.itemTotal", "Item total") + ": 0.00");
        itemTotalLbl.setUserData(0.0);

        
        HBox row = new HBox(8, priceLbl, priceField, qtyLbl, qtyField, itemTotalLbl);
        HBox.setHgrow(priceField, Priority.ALWAYS);
        HBox.setHgrow(qtyField, Priority.ALWAYS);

        
        Runnable recalc = () -> {
            double price = Math.max(0.0, parseDoubleSafe(priceField.getText(), 0.0));
            int qty = Math.max(0, parseIntSafe(qtyField.getText(), 0));
            double total = ShoppingCartCalculator.calculateItemTotal(new Item(price, qty));
            itemTotalLbl.setUserData(total);
            NumberFormat nf = NumberFormat.getNumberInstance(currentLocale);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            String msg = MessageFormat.format(s("output.total", "Total cost: {0}"), nf.format(total));
            itemTotalLbl.setText(msg);
            
        };
        priceField.textProperty().addListener((o, ov, nv) -> recalc.run());
        qtyField.textProperty().addListener((o, ov, nv) -> recalc.run());

        return row;
    }

    private void calculateTotals() {
        double overall = 0.0;
        List<Item> items = new ArrayList<>();
        for (Node node : itemsBox.getChildren()) {
            if (node instanceof HBox row) {
                
                TextField priceField = null;
                TextField qtyField = null;
                for (Node n : row.getChildren()) {
                    if (n instanceof TextField tf) {
                        if (priceField == null) priceField = tf; else qtyField = tf;
                    }
                }
                if (priceField != null && qtyField != null) {
                    double price = Math.max(0.0, parseDoubleSafe(priceField.getText(), 0.0));
                    int qty = Math.max(0, parseIntSafe(qtyField.getText(), 0));
                    Item item = new Item(price, qty);
                    items.add(item);
                    overall += ShoppingCartCalculator.calculateItemTotal(item);
                }
            }
        }
        setOverallTotal(overall);
        // Persist the calculation (best-effort)
        try {
            cartService.saveCart(items.size(), overall, currentLocale, items);
        } catch (Exception ignored) { }
    }

    private void setOverallTotal(double value) {
        lblOverallTotal.setUserData(value);
        NumberFormat nf = NumberFormat.getNumberInstance(currentLocale);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String msg = MessageFormat.format(s("output.total", "Total cost: {0}"), nf.format(value));
        lblOverallTotal.setText(msg);
    }

    private void refreshItemRowLabels() {
        int index = 0;
        for (Node node : itemsBox.getChildren()) {
            if (node instanceof HBox row) {
                int labelIdx = 0;
                int textFieldIdx = 0;
                for (Node child : row.getChildren()) {
                    if (child instanceof Label lbl) {
                        if (labelIdx == 0) {
                            lbl.setText(MessageFormat.format(s("prompt.itemPrice", "Price for item {0}"), index + 1));
                        } else if (labelIdx == 2) { 
                            lbl.setText(MessageFormat.format(s("prompt.itemQty", "Quantity for item {0}"), index + 1));
                        } else if (labelIdx == 4) {
                            
                            Object data = lbl.getUserData();
                            double val = (data instanceof Number n) ? n.doubleValue() : 0.0;
                            NumberFormat nf = NumberFormat.getNumberInstance(currentLocale);
                            nf.setMinimumFractionDigits(2);
                            nf.setMaximumFractionDigits(2);
                            String msg = MessageFormat.format(s("output.total", "Total cost: {0}"), nf.format(val));
                            lbl.setText(msg);
                        }
                        labelIdx++;
                    } else if (child instanceof TextField tf) {
                        if (textFieldIdx == 0) {
                            tf.setPromptText(s("gui.price", "Price"));
                        } else if (textFieldIdx == 1) {
                            tf.setPromptText(s("gui.quantity", "Quantity"));
                        }
                        textFieldIdx++;
                    }
                }
                index++;
            }
        }
    }

    private String s(String key, String deflt) {
        String v = i18n.get(key);
        if (v != null) return v;
        return deflt;
    }


    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static double parseDoubleSafe(String s, double def) {
        try {
            String t = (s == null ? "" : s.trim()).replace(',', '.');
            return Double.parseDouble(t);
        } catch (Exception e) {
            return def;
        }
    }

    private record LangOption(String display, Locale locale) {
        @Override public String toString() { return display; }
    }

    private static class StageAware { javafx.stage.Stage stage; }
}